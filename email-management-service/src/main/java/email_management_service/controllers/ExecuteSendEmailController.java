package email_management_service.controllers;

import email_management_service.services.ExecuteSendEmailService;
import email_management_service.validations.ExecuteEmailValidation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Validated
class ExecuteSendEmailController {

    // Service
    private final ExecuteSendEmailService executeSendEmailService;

    // constructor
    public ExecuteSendEmailController(
        ExecuteSendEmailService executeSendEmailService
    ) {
        this.executeSendEmailService = executeSendEmailService;
    }

    @PostMapping("${BASE_URL_EMAIL_SERVICE}/execute-send-email")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody ExecuteEmailValidation executeEmailValidation,
        BindingResult bindingResult

    ) {

        return executeSendEmailService.execute(executeEmailValidation);

    }

}