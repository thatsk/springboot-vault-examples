package com.mycompany.bookservice.service;

import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book saveBook(Book book) {
        book.setId(UUID.randomUUID());
        return bookRepository.save(book);
    }

}
