package com.example.demo.controllers;

import com.example.demo.validations.HelloWorldValidation;
import com.example.demo.services.HelloWorldService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Validated
class HelloWorldController {

    // Service
    private final HelloWorldService helloWorldService;

    // constructor
    public HelloWorldController (
        HelloWorldService helloWorldService
    ) {
        this.helloWorldService = helloWorldService;
    }

    @GetMapping("${BASE_URL_ACCOUNTS}/accounts")
    public ResponseEntity handle(

        // validations errors
        @Valid HelloWorldValidation helloWorldValidation,
        BindingResult bindingResult

    ) {

        // message
        String message = helloWorldValidation.message() != null ?
            helloWorldValidation.message() : "Hello World!";

        return helloWorldService.execute(message);

    }

}