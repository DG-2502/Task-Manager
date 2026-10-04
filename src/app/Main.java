import console.AppConsole;
import app.BootStrapConsole;
import domain.User;
import exception.ValidationException;
import infra.*;
import org.postgresql.ds.PGSimpleDataSource;
import repo.*;
import service.*;

void main() {
    PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
    pgSimpleDataSource.setURL(System.getenv("DB_URL"));
    pgSimpleDataSource.setUser(System.getenv("DB_USER"));
    pgSimpleDataSource.setPassword(System.getenv("DB_PASSWORD"));

    TaskRepo taskRepo = new TaskRepoPSQL(pgSimpleDataSource);
    UserRepo userRepo = new UserRepoPSQL(pgSimpleDataSource);
    PasswordHasher passwordHasher = new PasswordHasherBCrypt();

    TaskService taskService = new TaskService(taskRepo, userRepo);
    UserService userService = new UserService(userRepo, passwordHasher);

    if (userService.isEmpty()) {
        System.out.println("Bootstrapping the user's repository");
        BootStrapConsole console = new BootStrapConsole();
        while (true) {
            try {
                userService.register(console.getUsername(), console.getPassword(), User.Status.ADMIN);
                break;
            } catch (ValidationException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("GENERATED THE ROOT ADMIN USER");
    }

    AppConsole app = new AppConsole(taskService, userService);
    app.run();
}
