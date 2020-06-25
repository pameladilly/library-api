package br.com.pameladilly.libraryapi.api.resource;

import br.com.pameladilly.libraryapi.api.dto.BookDTO;
import br.com.pameladilly.libraryapi.api.dto.LoanDTO;
import br.com.pameladilly.libraryapi.model.entity.Book;
import br.com.pameladilly.libraryapi.model.entity.Loan;
import br.com.pameladilly.libraryapi.service.BookService;
import br.com.pameladilly.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {


    private final BookService service;
    private final ModelMapper modelMapper;
    private final LoanService loanService;
/*
    public BookController(BookService service, ModelMapper modelMapper, LoanService loanService) {
        this.service = service;
        this.modelMapper = modelMapper;
        this.loanService = loanService;
    }*/

    /* public BookController(BookService service, ModelMapper mapper, LoanService loanService) {
        this.service = service;
        this.modelMapper = mapper;
        this.loanService = loanService;
    }*/

    @PostMapping
    @ResponseStatus(CREATED)
    @ApiOperation("Creates a book")
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO){

        log.info( " creating a book for isbn: {} ", bookDTO.getIsbn());

        Book entity = modelMapper.map(bookDTO, Book.class);

        entity = service.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obtains a book details by id")
    public BookDTO get(@PathVariable Long id){
        log.info( "obtains details for book id: {} ", id);

        return service.getById(id).map( book ->  modelMapper.map(book, BookDTO.class)

                ).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }



    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    @ApiOperation("Deletes a book by Id")
    public void delete(@PathVariable Long id) {
        log.info(" deleting book of id {} ", id);

        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        service.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Updates a book")
    public BookDTO update ( @PathVariable Long id, @RequestBody BookDTO bookDTO) {
        log.info( "updating book of id: {} ", id);

        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        book.setAuthor(bookDTO.getAuthor());
        book.setTitle(bookDTO.getTitle());
        book = service.update(book);
        return modelMapper.map(book, BookDTO.class);


    }

    @GetMapping
    @ApiOperation("Find books by params")
    public Page<BookDTO> find (BookDTO bookDTO, Pageable pageRequest) {

        Book filter = modelMapper.map(bookDTO, Book.class);

        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>( list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook( @PathVariable Long id, Pageable pageable){

        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>( list, pageable, result.getTotalElements());

    }


}
