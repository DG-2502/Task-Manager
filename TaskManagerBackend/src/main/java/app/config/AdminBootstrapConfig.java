package app.config;

import app.domain.User;
import app.repo.UserRepository;
import app.repo.UserSpecifications;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!test")
public class AdminBootstrapConfig {

    @Bean
    public CommandLineRunner adminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment env) {
        return args -> {
            boolean anyAdmin = userRepository.exists(UserSpecifications.byStatus(User.Status.ADMIN));
            if (anyAdmin) return;

            String username = env.getProperty("APP_ADMIN_USERNAME");
            String password = env.getProperty("APP_ADMIN_PASSWORD");

            if (username == null || password == null) {
                throw new IllegalStateException(
                        "No admin user exists. Set APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD, then restart.");
            }

            if (userRepository.getByUsername(username).isPresent()) {
                throw new IllegalStateException("Username '" + username + "' is taken by a non-admin. Choose another.");
            }

            userRepository.save(new User(username, User.Status.ADMIN, passwordEncoder.encode(password)));
            System.out.println("--- Bootstrapped admin user: " + username);

        };
    }
}
