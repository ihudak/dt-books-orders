package com.dynatrace.orders.repository;

import com.dynatrace.orders.exception.PaymentException;
import com.dynatrace.orders.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Repository
public class PaymentRepository {
    @Value("${http.service.payment}")
    private String paymentBaseURL;
    private RestTemplate restTemplate;

    public PaymentRepository() {
        restTemplate = new RestTemplate();
    }



    public Payment submitPayment(@NonNull Payment payment) {
        String urlBuilder = paymentBaseURL;
        Payment paymentRes = null;
        try {
            paymentRes = restTemplate.postForObject(urlBuilder, payment, Payment.class);
        } catch (HttpClientErrorException exception) {
            throw new PaymentException("Payment rejected: " + exception.getMessage());
        }
        if (paymentRes == null) {
            throw new PaymentException("Purchase failed");
        }
        return paymentRes;
    }
}
