package accounts.services;

import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class AccountsAvatarCreateService {

    // constructor
    // ---------------------------------------------------------------------
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsProfileRepository accountsProfileRepository;
    private final Path uploadDir;
    private static final String DEFAULT_UPLOAD_DIR = "static/uploads/avatar";

    public AccountsAvatarCreateService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsProfileRepository accountsProfileRepository

    ) throws IOException {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsProfileRepository = accountsProfileRepository;

        this.uploadDir = Paths.get(DEFAULT_UPLOAD_DIR);
        Files.createDirectories(this.uploadDir);

    }
    // ---------------------------------------------------------------------

    // Main method
    @CacheEvict(value = "profileCache", key = "#credentialsData['id']")
    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        MultipartFile[] file

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Auth
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // Find user
        // ---------------------------------------------------------------------
        Optional<AccountsProfileEntity> findProfileUser =  accountsProfileRepository.findById(
            idUser
        );
        // ---------------------------------------------------------------------

        // Invalid user
        // ---------------------------------------------------------------------
        if (findProfileUser.isEmpty()) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // ##### If you call the endpoint with nothing, delete the existing image, if any.
        // ---------------------------------------------------------------------
        if ( file == null || file.length == 0 || file[0].isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                400, "##### Connot be empty"
            );

        }
        // ---------------------------------------------------------------------

        // Only one image
        // ---------------------------------------------------------------------
        if ( file.length >= 2 ) {

            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_avatar_one_image", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Images only
        // ---------------------------------------------------------------------
        String contentType = file[0].getContentType();

        if (
            contentType == null ||
            (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))
        ) {

            // call custom error
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_avatar_only_images", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Image too large
        // ---------------------------------------------------------------------
        long maxSizeInBytes = 1 * 1024 * 1024;

        if (file[0].getSize() > maxSizeInBytes) {
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_avatar_size", null, locale
                )
            );
        }
        // ---------------------------------------------------------------------

        // Malicious name
        // ---------------------------------------------------------------------
        String originalFilename = file[0].getOriginalFilename();

        if ( originalFilename == null || !originalFilename.contains(".") ) {

            // call custom error
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_avatar_upload_error", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Save image
        // ---------------------------------------------------------------------
        String extension = originalFilename.substring(
            originalFilename.lastIndexOf('.') + 1
        );
        String generatedName = UUID.randomUUID() + "." + extension;

        try (InputStream inputStream = file[0].getInputStream()) {

            Path targetPath = uploadDir.resolve(generatedName);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {

            // call custom error
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_avatar_upload_error", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // ##### Save the generated image ID to the user's profile, iIf you pass a new image, delete the existing one and save the new one.
        // ---------------------------------------------------------------------
        AccountsProfileEntity profileUpdated = findProfileUser.get();
        profileUpdated.setProfileImage(
            "/accounts/static/uploads/avatar/" + generatedName
        );

        accountsProfileRepository.save(profileUpdated);
        // ---------------------------------------------------------------------


        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/upload-avatar");
        customLinks.put("next", "/accounts/get-avatar");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_avatar_upload_success", null, locale
                )
            )
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);
    }

}
