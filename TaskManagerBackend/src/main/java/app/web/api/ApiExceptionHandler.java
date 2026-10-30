package app.web.api;

import app.exception.*;
import app.web.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(basePackages = "app.web.api")
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, String field, HttpServletRequest req) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                field,
                req.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), null, req);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getField(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, e.getMessage(), null, req);
    }

    @ExceptionHandler({TaskNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), null, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), e.getMessage(), e);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid parameter value: " + e.getMessage(), null, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid data type.", null, req);
    }
}
