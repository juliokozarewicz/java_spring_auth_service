package com.example.demo.controllers;

import com.example.demo.validations.HelloWorldValidation;
import com.example.demo.services.HelloWorldService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private HelloWorldService helloWorldService;

    @GetMapping("${BASE_URL_HELLOWORLD}/helloworld")
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