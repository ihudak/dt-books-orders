package com.dynatrace.orders.controller;

import com.dynatrace.orders.model.Version;
import com.dynatrace.orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/version")
public class VersionController {
    @Autowired
    private OrderRepository orderRepository;
    @Value("${service.version}")
    private String svcVer;
    @Value("${service.date}")
    private String svcDate;

    @GetMapping("")
    public Version getVersion() {
        return new Version("orders", svcVer, svcDate, "OK", "Count: " + orderRepository.count());
    }
}
