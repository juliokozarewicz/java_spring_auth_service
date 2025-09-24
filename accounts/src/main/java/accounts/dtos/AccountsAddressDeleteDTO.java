package accounts.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AccountsAddressDeleteDTO(

    @NotNull(message = "{validation_is_required}")
    UUID addressId

) {}