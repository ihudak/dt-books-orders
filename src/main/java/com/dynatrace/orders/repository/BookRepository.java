package com.dynatrace.orders.repository;

import com.dynatrace.orders.exception.ResourceNotFoundException;
import com.dynatrace.orders.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class BookRepository {
    @Value("${http.service.books}")
    private String bookBaseURL;
    private RestTemplate restTemplate;

    public BookRepository() {
        restTemplate = new RestTemplate();
    }


    public Book getBookByISBN(String isbn) {
        String urlBuilder = bookBaseURL +
                "/find" +
                "?isbn=" +
                isbn;

        Book book = restTemplate.getForObject(urlBuilder, Book.class);
        if (null == book) {
            throw new ResourceNotFoundException("Book not found by isbn: " + isbn);
        }
        return book;
    }

    public Book[] getAllBooks() {
        return restTemplate.getForObject(bookBaseURL, Book[].class);
    }
}
