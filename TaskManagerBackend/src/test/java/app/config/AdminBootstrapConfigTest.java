package app.config;

import app.domain.User;
import app.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapConfigTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment env;

    private AdminBootstrapConfig bootstrapConfig;

    @BeforeEach
    void setUp() {
        bootstrapConfig = new AdminBootstrapConfig();
    }

    @Test
    void adminBootstrap_ShouldDoNothing_WhenAdminAlreadyExists() throws Exception {
        CommandLineRunner runner = bootstrapConfig.adminBootstrap(userRepository, passwordEncoder, env);
        when(userRepository.exists(any(Specification.class))).thenReturn(true);

        runner.run();

        verify(userRepository, never()).save(any(User.class));
        verify(env, never()).getProperty(anyString());
    }

    @Test
    void adminBootstrap_ShouldThrowException_WhenNoAdminAndEnvVarsMissing() {
        CommandLineRunner runner = bootstrapConfig.adminBootstrap(userRepository, passwordEncoder, env);
        when(userRepository.exists(any(Specification.class))).thenReturn(false);
        when(env.getProperty("APP_ADMIN_USERNAME")).thenReturn(null);
        when(env.getProperty("APP_ADMIN_PASSWORD")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, runner::run);
        assertTrue(exception.getMessage().contains("No admin user exists"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminBootstrap_ShouldThrowException_WhenUsernameTakenByNonAdmin() {
        CommandLineRunner runner = bootstrapConfig.adminBootstrap(userRepository, passwordEncoder, env);
        when(userRepository.exists(any(Specification.class))).thenReturn(false);
        when(env.getProperty("APP_ADMIN_USERNAME")).thenReturn("admin");
        when(env.getProperty("APP_ADMIN_PASSWORD")).thenReturn("secret");

        when(userRepository.getByUsername("admin")).thenReturn(Optional.of(new User("admin", User.Status.USER, "hash")));

        IllegalStateException exception = assertThrows(IllegalStateException.class, runner::run);
        assertTrue(exception.getMessage().contains("is taken by a non-admin"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminBootstrap_ShouldSaveAdmin_WhenDatabaseIsEmptyAndEnvVarsPresent() throws Exception {
        CommandLineRunner runner = bootstrapConfig.adminBootstrap(userRepository, passwordEncoder, env);
        when(userRepository.exists(any(Specification.class))).thenReturn(false);
        when(env.getProperty("APP_ADMIN_USERNAME")).thenReturn("superadmin");
        when(env.getProperty("APP_ADMIN_PASSWORD")).thenReturn("supersecret");
        when(userRepository.getByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("supersecret")).thenReturn("hashed_secret");

        runner.run();

        verify(userRepository, times(1)).save(any(User.class));
    }
}
