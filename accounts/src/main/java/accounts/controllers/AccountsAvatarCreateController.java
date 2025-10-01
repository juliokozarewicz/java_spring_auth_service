package accounts.controllers;

import accounts.services.AccountsAvatarCreateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping()
public class AccountsAvatarCreateController {

    private final AccountsAvatarCreateService accountsAvatarCreateService;

    public AccountsAvatarCreateController (

        AccountsAvatarCreateService accountsAvatarCreateService

    ) {

        this.accountsAvatarCreateService = accountsAvatarCreateService;

    }

    @PostMapping("${ACCOUNTS_BASE_URL}/upload-avatar")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        HttpServletRequest request,
        @RequestParam(value = "avatarImage", required = false) MultipartFile[] file

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
            request.getAttribute("credentialsData");

        return accountsAvatarCreateService.execute(credentialsData, file);

    }

}