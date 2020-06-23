package br.com.pameladilly.libraryapi.api.resource;

import br.com.pameladilly.libraryapi.api.dto.BookDTO;
import br.com.pameladilly.libraryapi.api.dto.LoanDTO;
import br.com.pameladilly.libraryapi.api.dto.LoanFilterDTO;
import br.com.pameladilly.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.pameladilly.libraryapi.model.entity.Book;
import br.com.pameladilly.libraryapi.model.entity.Loan;
import br.com.pameladilly.libraryapi.service.BookService;
import br.com.pameladilly.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO loanDTO){
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn()).orElseThrow(
                () -> new ResponseStatusException( HttpStatus.BAD_REQUEST, "Book not found for isbn")
        );
        Loan entity = Loan.builder().book(book)
                .customer(loanDTO.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);
        return entity.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(
                @PathVariable Long id,
                @RequestBody ReturnedLoanDTO returnedLoanDTO){

        Loan loan = service.getById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        loan.setReturned(returnedLoanDTO.getReturned());

        service.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO loanFilterDTO, Pageable pageable){

        Page<Loan> result = service.find(loanFilterDTO, pageable);
        List<LoanDTO> loans = result.getContent().stream().map(
                entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);

                    return loanDTO;

                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans, pageable, result.getTotalElements());
    }


}
