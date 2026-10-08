package app.web.view;

import app.exception.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "app.web.view")
public class ViewExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public String handleAuth(AuthenticationException e) {
        return "redirect:/login";
    }
}
