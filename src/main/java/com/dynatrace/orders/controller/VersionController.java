package com.dynatrace.orders.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/version")
public class VersionController {
    @Value("${service.version}")
    private String svcVer;
    @Value("${service.date}")
    private String svcDate;

    @GetMapping("")
    public ResponseEntity<?> getVersion() {
        String verStr = "{\"ver\": \"" + this.svcVer + "\", \"date\": \"" + this.svcDate + "\"}";
        return ResponseEntity.ok().body(verStr);
    }
}
