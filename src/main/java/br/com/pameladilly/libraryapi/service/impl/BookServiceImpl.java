package br.com.pameladilly.libraryapi.service.impl;

import br.com.pameladilly.libraryapi.exception.BusinessException;
import br.com.pameladilly.libraryapi.model.entity.Book;
import br.com.pameladilly.libraryapi.model.repository.BookRepository;
import br.com.pameladilly.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("ISBN já cadastrado.");
        }

        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book.getId() == null || book == null) {
            throw new IllegalArgumentException("Book id can be null");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book.getId() == null || book == null) {
            throw new IllegalArgumentException("Book id can be null");
        }
        return this.repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter,
                ExampleMatcher.matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING)
        );
        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return Optional.empty();
    }
}
