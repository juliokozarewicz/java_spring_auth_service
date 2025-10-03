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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final AccountsManagementService accountsManagementService;

    public AccountsAvatarCreateService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsProfileRepository accountsProfileRepository,
        AccountsManagementService accountsManagementService

    ) throws IOException {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsProfileRepository = accountsProfileRepository;
        this.accountsManagementService = accountsManagementService;

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

        try {

            // Auth
            UUID idUser = UUID.fromString((String) credentialsData.get("id"));

            // Find user
            // ---------------------------------------------------------------------
            Optional<AccountsProfileEntity> findProfileUser = accountsProfileRepository.findById(
                idUser
            );
            // ---------------------------------------------------------------------

            // Invalid user
            // ---------------------------------------------------------------------
            if ( findProfileUser.isEmpty() ) {

                // call custom error
                errorHandler.customErrorThrow(
                    404,
                    messageSource.getMessage(
                        "response_invalid_credentials", null, locale
                    )
                );

            }
            // ---------------------------------------------------------------------

            // If user call the endpoint with nothing, delete the existing image
            // ---------------------------------------------------------------------
            AccountsProfileEntity profile = findProfileUser.get();
            String existingImagePath = profile.getProfileImage();

            if ( file == null || file.length == 0 || file[0].isEmpty() ) {

                if ( existingImagePath != null && !existingImagePath.isBlank() ) {

                    String[] parts = existingImagePath.split("/");
                    String existingImageFilename = parts[parts.length - 1];
                    Path existingImageFullPath = uploadDir.resolve(existingImageFilename);
                    Files.deleteIfExists(existingImageFullPath);
                    profile.setProfileImage(null);
                    accountsProfileRepository.save(profile);

                }

                // Return success response
                StandardResponseService response = new StandardResponseService.Builder()
                    .statusCode(200)
                    .statusMessage("success")
                    .message( messageSource.getMessage(
                        "response_avatar_removed_success", null, locale
                    ))
                    .build();

                return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response);

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

            if ( file[0].getSize() > maxSizeInBytes ) {

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

            // iIf user pass a new image, delete the existing one
            // ---------------------------------------------------------------------
            if ( existingImagePath != null && !existingImagePath.isBlank() ) {

                // Extract only the image filename (e.g., "dfce7a52-b66d-4313-a270-c0ef44396546.png")
                String[] parts = existingImagePath.split("/");
                String existingImageFilename = parts[parts.length - 1];

                // Build full path to the existing image file
                Path existingImageFullPath = uploadDir.resolve(existingImageFilename);

                Files.deleteIfExists(existingImageFullPath);

            }
            // ---------------------------------------------------------------------

            // Save image
            // ---------------------------------------------------------------------
            String generatedName = accountsManagementService.createUniqueId() + ".png";
            Path targetPath = uploadDir.resolve(generatedName);
            InputStream inputStream = file[0].getInputStream();
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            if ( bufferedImage == null ) {

                errorHandler.customErrorThrow(
                    400,
                    messageSource.getMessage(
                        "response_avatar_upload_error", null, locale
                    )
                );

            }

            ImageIO.write(bufferedImage, "png", targetPath.toFile());
            // ---------------------------------------------------------------------

            // Save the generated image ID to the user's profile
            // ---------------------------------------------------------------------
            AccountsProfileEntity profileUpdated = findProfileUser.get();
            profileUpdated.setProfileImage(
                "/accounts/static/uploads/avatar/" + generatedName
            );

            accountsProfileRepository.save(profileUpdated);
            // ---------------------------------------------------------------------

        } catch (IOException e) {

            // call custom error
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_avatar_upload_error", null, locale
                )
            );

        }

        // Response
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

        // ---------------------------------------------------------------------

    }

}
