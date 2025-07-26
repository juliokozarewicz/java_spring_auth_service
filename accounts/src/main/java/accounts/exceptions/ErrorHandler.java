package accounts.exceptions;

import accounts.utils.StandardResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

import org.springframework.context.MessageSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ErrorHandler {

    // logs
    private static final Logger logger = LoggerFactory.getLogger(
        ErrorHandler.class
    );

    // attributes
    private final MessageSource messageSource;

    // constructor
    public ErrorHandler ( MessageSource messageSource ) {
        this.messageSource = messageSource;
    }

    // error throw
    public void customErrorThrow (

        int errorCode,
        String message

    ) {

        // locale
        Locale locale = LocaleContextHolder.getLocale();

        // call error
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("errorCode", errorCode);
        errorDetails.put("message", message);
        throw new RuntimeException(errorDetails.toString());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleAllExceptions(

        Exception error

    ) {

        // locale
        Locale locale = LocaleContextHolder.getLocale();

        // validations error old
        /*
        if ( error instanceof ConstraintViolationException ) {

            var violation = ((ConstraintViolationException) error)
                .getConstraintViolations().iterator().next();

            // field name
            String fieldName = violation.getPropertyPath().toString();
            String[] fieldParts = fieldName.split("\\.");
            String lastFieldName = fieldParts[fieldParts.length - 1];

            // error validations message
            String errorValidationMessage = violation.getMessage();

            StandardResponse response = new StandardResponse.Builder()
                .statusCode(400)
                .statusMessage("error")
                .field(lastFieldName)
                .message(errorValidationMessage)
                .build();

            return ResponseEntity
                .status(response.getStatusCode())
                .body(response);

        }
        */

        // validations error new
        if (error instanceof ConstraintViolationException) {

            var violations = ((ConstraintViolationException) error)
                .getConstraintViolations();

            // Error list
            List<Map<String, String>> errors = new LinkedList<>();

            for (var violation : violations) {
                String path = violation.getPropertyPath().toString();
                String[] parts = path.split("\\.");
                String field = parts[parts.length - 1];

                Map<String, String> errorItem = new LinkedHashMap<>();
                errorItem.put("field", field);
                errorItem.put("message", violation.getMessage());

                errors.add(errorItem);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("statusCode", 422);
            response.put("statusMessage", "error");
            response.put("fieldErrors", errors);

            return ResponseEntity
                .status(422)
                .body(response);

        }

        // bad request
        if (

            error instanceof HttpMessageNotReadableException ||
            error instanceof HttpRequestMethodNotSupportedException ||
            error instanceof NoResourceFoundException

        ) {

            StandardResponse response = new StandardResponse.Builder()
                .statusCode(400)
                .statusMessage("error")
                .message(
                    messageSource.getMessage(
                        "response_bad_request", null, locale
                    )
                )
                .build();

            return ResponseEntity
                .status(response.getStatusCode())
                .body(response);

        }

        // get error itens
        String errorMessage = error.getMessage();
        Pattern pattern = Pattern.compile("errorCode=(\\d+), message=(.*)");
        Matcher matcher = pattern.matcher(errorMessage);

        if ( matcher.find() ) {

            int errorCode = Integer.parseInt(matcher.group(1));
            String errorMessageDetail = matcher.group(2).trim().replaceAll(
                "}$", ""
            );

            StandardResponse response = new StandardResponse.Builder()
                .statusCode(errorCode)
                .statusMessage("error")
                .message(errorMessageDetail)
                .build();

            return ResponseEntity
                .status(response.getStatusCode())
                .body(response);

        }

        // logs
        logger.error(error.toString());

        // Fallback response
        StandardResponse fallbackResponse = new StandardResponse.Builder()
            .statusCode(500)
            .statusMessage("error")
            .message(
                messageSource.getMessage(
                    "response_response_server_error", null, locale
                )
            )
            .build();

        return ResponseEntity
            .status(fallbackResponse.getStatusCode())
            .body(fallbackResponse);

    }

}