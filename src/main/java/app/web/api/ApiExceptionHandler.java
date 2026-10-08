package app.web.api;

import app.exception.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.web.api")
public class ApiExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }
}
