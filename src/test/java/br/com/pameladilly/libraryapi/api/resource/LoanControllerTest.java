package br.com.pameladilly.libraryapi.api.resource;

import br.com.pameladilly.libraryapi.api.dto.LoanDTO;
import br.com.pameladilly.libraryapi.api.dto.LoanFilterDTO;
import br.com.pameladilly.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.pameladilly.libraryapi.exception.BusinessException;
import br.com.pameladilly.libraryapi.model.entity.Book;
import br.com.pameladilly.libraryapi.model.entity.Loan;
import br.com.pameladilly.libraryapi.service.BookService;
import br.com.pameladilly.libraryapi.service.LoanService;
import br.com.pameladilly.libraryapi.service.LoanServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empréstimo")
    public void createLoanTest() throws Exception{

        LoanDTO loanDTO = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        Book bookSaved = (Book.builder().id(1L).isbn("123").build());

        BDDMockito.given( bookService.getBookByIsbn("123") )
                .willReturn(Optional.of(bookSaved));

        Loan loan = Loan.builder().id(1L).customer("Fulano").book(bookSaved).loanDate(LocalDate.now()).build();
        BDDMockito.given( loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform( request )
                .andExpect( status().isCreated())
                .andExpect( content().string("1"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de um livro inexistente")
    public void invalidIsbnCreateLoanTest() throws Exception{

        LoanDTO loanDTO = LoanDTO.builder().isbn("123").customer("Fulano").build();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given( bookService.getBookByIsbn("123") )
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform( request )
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( jsonPath( "errors[0]").value("Book not found for isbn"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de um livro emprestado")
    public void loanedBookErrorOnCreateLoanTest () throws Exception{

        LoanDTO loanDTO = LoanDTO.builder().isbn("123").customer("Fulano").build();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        Book bookSaved = (Book.builder().id(1L).isbn("123").build());
        BDDMockito.given( bookService.getBookByIsbn("123") )
                .willReturn(Optional.of(bookSaved));

      //  Loan loan = Loan.builder().id(1L).customer("Fulano").book(bookSaved).loanDate(LocalDate.now()).build();
        BDDMockito.given( loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));



        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform( request )
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( jsonPath( "errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception{

        ReturnedLoanDTO dto = ReturnedLoanDTO
                    .builder().returned(true).build();

        Loan loan = Loan.builder().id(1L).build();
        BDDMockito.given( loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(
                patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect( status().isOk() );

        Mockito.verify( loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
    public void returnInexistentBookTest() throws Exception{

        ReturnedLoanDTO dto = ReturnedLoanDTO
                .builder().returned(true).build();

        BDDMockito.given( loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        String json = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect( status().isNotFound() );

    }

    @Test
    @DisplayName("Deve filtrar empréstimos")
    public void findLoansTest() throws Exception {

        Long id = 1L;

        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        Book book = Book.builder().id(1L).isbn("321").build();
        loan.setBook(book);


        BDDMockito.given( loanService.find(Mockito.any(LoanFilterDTO.class) , Mockito.any(Pageable.class)))
                .willReturn( new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(),
                loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform( request )
                .andExpect( status().isOk())
                .andExpect( jsonPath("content", Matchers.hasSize(1)))
                .andExpect( jsonPath( "totalElements").value(1))
                .andExpect( jsonPath("pageable.pageSize").value(10))
                .andExpect( jsonPath("pageable.pageNumber").value(0));

    }
}
