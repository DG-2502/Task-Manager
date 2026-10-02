package app;

import java.util.Scanner;

public class BootStrapConsole {
    private Scanner scanner = new Scanner(System.in);

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

    public String getUsername() {
        System.out.println("Enter the username:");
        return read(false);
    }

    public String getPassword() {
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

        return password;
    }
}
