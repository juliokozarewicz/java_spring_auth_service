package helloworld.controllers;

import helloworld.dtos.HelloWorldDTO;
import helloworld.services.HelloWorldService;
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
    public HelloWorldController(
        HelloWorldService helloWorldService
    ) {
        this.helloWorldService = helloWorldService;
    }

    @GetMapping("${HELLOWORLD_BASE_URL}/helloworld")
    public ResponseEntity handle(

        // dtos errors
        @Valid HelloWorldDTO helloWorldDTO,
        BindingResult bindingResult

    ) {

        // message
        String message = helloWorldDTO.message() != null ?
            helloWorldDTO.message() : "Hello World!";

        return helloWorldService.execute(message);

    }

}