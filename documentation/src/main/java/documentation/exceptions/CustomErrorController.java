package documentation.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<helloworld.utils.StandardResponseService> handleError(HttpServletRequest request) {
        helloworld.utils.StandardResponseService response = new helloworld.utils.StandardResponseService.Builder()
            .statusCode(404)
            .statusMessage("error")
            .build();

        return ResponseEntity.status(400).body(response);
    }
}