package app.web.view;

import app.domain.User;
import app.exception.AuthenticationException;
import app.exception.ValidationException;
import app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserViewController {
    private final UserService userService;

    public UserViewController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("userId", user.getId());
            return "redirect:/tasks";
        } catch (AuthenticationException e) {
            model.addAttribute("loginError", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String username, @RequestParam String password, @RequestParam String passwordConfirm, RedirectAttributes redirectAttributes, Model model) {
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("passwordConfirmError", "Passwords do not match");
            model.addAttribute("username", username);
            return "register";
        }

        try {
            userService.register(username, password, User.Status.USER);
            redirectAttributes.addFlashAttribute("registeredUsername", username);
            redirectAttributes.addFlashAttribute("successMessage", "Account created! You can now log in.");
            return "redirect:/login";
        } catch (ValidationException e) {
            model.addAttribute(e.getField() + "Error", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
