package br.com.pameladilly.libraryapi.service;

import br.com.pameladilly.libraryapi.api.dto.LoanFilterDTO;
import br.com.pameladilly.libraryapi.exception.BusinessException;
import br.com.pameladilly.libraryapi.model.repository.LoanRepository;
import br.com.pameladilly.libraryapi.model.entity.Book;
import br.com.pameladilly.libraryapi.model.entity.Loan;
import br.com.pameladilly.libraryapi.service.impl.LoanServiceImpl;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest  {


    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach()
    public void setUp(){
        this.service = new LoanServiceImpl(repository);

    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest(){
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder().book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
            .id(1L)
            .loanDate(LocalDate.now())
            .customer(customer)
            .book(book).build();

        Mockito.when( repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when( repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        Assertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        Assertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        Assertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());


    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com um livro já emprestado")
    public void loanedBookSaveTest(){
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder().book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Mockito.when( repository.existsByBookAndNotReturned(book)).thenReturn(true);


        Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));

        Assertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");


        Mockito.verify( repository, Mockito.never()).save(savingLoan);


    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetailsTest(){
        Loan loan = createLoan();
        Long id = 1L;

        loan.setId(id);

        Mockito.when( repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify( repository ).findById(id);


    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest(){
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        Mockito.when( repository.save( loan) ).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        Assertions.assertThat(updatedLoan.getReturned()).isTrue();
        Mockito.verify( repository ).save( loan );

    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest(){

        LoanFilterDTO loanFilterDTO = LoanFilterDTO
                .builder().customer("Fulano").isbn("123").build();

        Loan loan = createLoan();
        loan.setId(1L);
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, 1);
        when( repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(page);


        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    public static Loan createLoan(){
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        return Loan.builder().book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }
}
