package helloworld.validations;

import jakarta.validation.constraints.Size;

public record HelloWorldValidation(

    @Size(min = 1, message = "{validation_not_empty}")
    @Size(max = 100, message = "{validation_many_characters}")
    String message

) {
}