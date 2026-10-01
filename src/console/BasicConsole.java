package console;


import java.util.Scanner;

public abstract class BasicConsole implements Console {
    private boolean exitOption = false;
    private boolean helpOption = true;

    public void readInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String request = scanner.nextLine().trim();
            if (request.matches("\\w+\\s+.+")) {
                String[] split = request.splitWithDelimiters("\\s+", 2);
                if (parseCommand(split[0].toLowerCase(), split[2])) {
                    break;
                }
            } else if (request.matches("\\w+")) {
                if (parseCommand(request.toLowerCase(), "none")) {
                    break;
                }
            } else {
                System.out.println("Could not parse the command, please provide the following format: command option/none. Or type 'help'");
            }
        }
    }

    public int readInt(int lower, int upper) {
        Scanner scanner = new Scanner(System.in);
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
        Scanner scanner = new Scanner(System.in);
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

    public boolean parseCommand(String command, String query) {
        if (command.equals("exit")) {
            return exitOption = true;
        }
        if (command.equals("help")) {
            return helpOption = true;
        }
        System.out.println("Could not find any matching command! Type help to see more!");
        return false;
    }

    public void executeCommands() {
        if (helpOption) {
            printHelp();
        }
    }

    protected void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help - Show available commands");
        setHelpOption(false);
    }

    @Override
    public void run() {
        executeCommands();
        readInput();
    }

    public boolean getExitOption() {
        return exitOption;
    }

    public boolean getHelpOption() {
        return helpOption;
    }

    public void setHelpOption(boolean helpOption) {
        this.helpOption = helpOption;
    }
}

