package app;

import java.util.Scanner;

public class BootStrapConsole {
    private String username;
    private String password;
    private Scanner scanner = new Scanner(System.in);

    public BootStrapConsole() {
        System.out.println("Bootstrapping the user's repository");
        System.out.println("Enter the username:");
        username = read(false);
        readPassword();
        scanner.close();
    }

    private String read(boolean any) {
        String line = scanner.nextLine().trim();
        if (any) {
            return line;
        }
        while (true) {
            if (line.matches("[a-zA-Z]+")) {
                return line;
            }
            System.out.println("Please enter one word!");
            line = scanner.nextLine().trim();
        }
    }

    private void readPassword() {
        String password;
        String passwordRepeat;
        while (true) {
            System.out.println("Enter the password: ");
            password = read(true);
            System.out.println("Repeat the password: ");
            passwordRepeat = read(true);

            if (password.equals(passwordRepeat)) {
                break;
            }
            System.out.println("The passwords are different");
        }

        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
