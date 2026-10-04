package infra;

import domain.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

public class TaskRepoFile extends TaskRepoArr {
    private String path;

    public TaskRepoFile(String path) throws IOException {
        this.path = path;
        File file = checkFile(path);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            parseLine(scanner.nextLine());
        }

        scanner.close();
    }

    private File checkFile(String path) throws IOException {
        File file = new File(path);
        if (file.isDirectory()) {
            throw new RuntimeException(file + " is a directory, not a file!");
        }
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        if (file.createNewFile()) {
            write();
        }

        return file;
    }

    private void parseLine(String line) {
        String[] args = line.split("`SPLITTER`");
        int id = Integer.parseInt(args[0]);
        if (id > ID) {
            ID = id;
        }
        Task newTask = new Task(args[1], LocalDate.parse(args[2]), Task.State.valueOf(args[3]), Integer.parseInt(args[4]));
        newTask.setID(id);
        tasks.add(newTask);
    }

    private void write() {
        try {
            FileWriter fWriter = new FileWriter(path);
            String line;
            for (Task task : tasks) {
                line = task.getID() + "`SPLITTER`";
                line += task.getTitle() + "`SPLITTER`";
                line += task.getStartDate() + "`SPLITTER`";
                line += task.getState() + "`SPLITTER`";
                line += task.getCreatorID() + "\n";
                fWriter.write(line);
            }

            fWriter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void add(Task task) {
        super.add(task);
        write();
    }

    @Override
    public void update(Task updatedTask) {
        super.update(updatedTask);
        write();
    }

    @Override
    public void delete(int id) {
        super.delete(id);
        write();
    }

    @Override
    public void deleteByStateAndUser(Task.State state, int userID) {
        super.deleteByStateAndUser(state, userID);
        write();
    }

    @Override
    public void deleteByUserId(int userId) {
        super.deleteByUserId(userId);
        write();
    }
}
