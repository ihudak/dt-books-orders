package com.dynatrace.orders.repository;

import com.dynatrace.orders.exception.PurchaseForbiddenException;
import com.dynatrace.orders.exception.ResourceNotFoundException;
import com.dynatrace.orders.model.Storage;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class StorageRepository {
    @Value("${http.service.storage}")
    private String storageBaseURL;

    private RestTemplate restTemplate;

    public StorageRepository() {
        restTemplate = new RestTemplate();
    }



    public Storage buyBook(@NotNull Storage storage) {
        String urlBuilder = storageBaseURL +
                "/sell-book";
        Storage storageNew = restTemplate.postForObject(urlBuilder, storage, Storage.class);
        if (storageNew == null || storageNew.getQuantity() < 0) {
            throw new PurchaseForbiddenException("Purchase was rejected, ISBN: " + storage.getIsbn());
        }
        return storageNew;
    }

    public Storage returnBook(@NotNull Storage storage) {
        String urlBuilder = storageBaseURL +
                "/ingest-book";

        Storage storageNew = restTemplate.postForObject(urlBuilder, storage, Storage.class);
        if (storageNew == null || storageNew.getQuantity() < 0) {
            throw new PurchaseForbiddenException("Return was rejected, ISBN: " + storage.getIsbn());
        }
        return storageNew;
    }

    public Storage getStorageByISBN(String isbn) {
        String urlBuilder = storageBaseURL +
                "/findByISBN" +
                "?isbn=" +
                isbn;

        Storage storage = restTemplate.getForObject(urlBuilder, Storage.class);
        if (null == storage) {
            throw new ResourceNotFoundException("Book in Storage is not found by isbn: " + isbn);
        }
        return storage;
    }

    public Storage[] getAllBooksInStorage() {
        return restTemplate.getForObject(storageBaseURL, Storage[].class);
    }
}
