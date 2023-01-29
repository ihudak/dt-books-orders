package com.dynatrace.orders.repository;

import com.dynatrace.orders.exception.ResourceNotFoundException;
import com.dynatrace.orders.model.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class ClientRepository {
    @Value("${http.service.clients}")
    private String clientBaseURL;

    public ClientRepository() {
        restTemplate = new RestTemplate();
    }

    private RestTemplate restTemplate;

    public Client getClientByEmail(String email) {
        String urlBuilder = clientBaseURL +
                "/find" +
                "?email=" +
                email;

        Client client = restTemplate.getForObject(urlBuilder, Client.class);
        if (null == client) {
            throw new ResourceNotFoundException("Client not found by email: " + email);
        }
        return client;
    }

    public Client[] getAllClients() {
        return restTemplate.getForObject(clientBaseURL, Client[].class);
    }
}
