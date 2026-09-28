package infra;

import domain.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

public class UserRepoFile extends UserRepoArr {
    private String path = "data/users.txt";

    public UserRepoFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));

        while (scanner.hasNextLine()) {
            parseLine(scanner.nextLine());
        }
    }

    private void parseLine(String line) {
        String[] args = line.split("`SPLITTER`");
        int id = Integer.parseInt(args[0]);
        if (id > ID) {
            ID = id;
        }
        User newUser = new User(args[1], Boolean.parseBoolean(args[2]));
        newUser.setID(id);
        users.add(newUser);
    }

    private void write() {
        try {
            FileWriter fWriter = new FileWriter(path);
            String line;
            for (User user : users) {
                line = user.getID() + "`SPLITTER`";
                line += user.getUsername() + "`SPLITTER`";
                line += user.isAdmin() + "\n";
                fWriter.write(line);
            }

            fWriter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void add(User user) {
        super.add(user);
        write();
    }

    @Override
    public void update(User updatedUser) {
        super.update(updatedUser);
        write();
    }

    @Override
    public void delete(int id) {
        super.delete(id);
        write();
    }
}
