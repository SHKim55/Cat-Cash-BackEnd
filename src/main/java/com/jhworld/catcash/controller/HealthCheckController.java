package com.jhworld.catcash.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController {
    @GetMapping("/test")
    public String healthCheck() {
        return "The server is currently running";
    }
}