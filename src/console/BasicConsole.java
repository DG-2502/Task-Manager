package console;

import java.util.Scanner;

public abstract class BasicConsole {
    private final Scanner scanner = new Scanner(System.in);
    protected boolean exitOption = false;

    public void readInput() {
        String request = scanner.nextLine().trim();
        if (request.matches("\\w+\\s+.+")) {
            String[] split = request.splitWithDelimiters("\\s+", 2);
            parseCommand(split[0].toLowerCase(), split[2]);
        } else if (request.matches("\\w+")) {
            parseCommand(request.toLowerCase(), "none");
        } else {
            System.out.println("Could not parse the command, please provide the following format: command option/none. Or type 'help'");
        }
    }

    public int readInt(int lower, int upper) {
        while (true) {
            String request = scanner.nextLine().trim();
            if (request.matches("\\d+")) {
                int number = Integer.parseInt(request);
                if (number >= lower & number <= upper) {
                    return number;
                }
                System.out.println("Please type a number between " + lower + " and " + upper);
            } else {
                System.out.println("Please type a number!");
            }
        }
    }

    public String readName(boolean anyName) {
        String line = scanner.nextLine().trim();
        if (anyName) {
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

    public void parseCommand(String command, String query) {
        switch (command) {
            case "exit" -> exitOption = true;
            case "help" -> printHelp();
            default -> System.out.println("Could not find any matching command! Type help to see more!");
        }
    }

    protected void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help - Show available commands");
    }

    public void run() {
        while (!exitOption) {
            readInput();
        }
    }
}

