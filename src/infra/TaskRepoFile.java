package infra;

import domain.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Scanner;

public class TaskRepoFile extends TaskRepoArr {
    private String path = "data/tasks.txt";

    public TaskRepoFile() throws FileNotFoundException {
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
        Task newTask = new Task(args[1], LocalDate.parse(args[2]), Boolean.parseBoolean(args[3]), Integer.parseInt(args[4]));
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
                line += task.isActive() + "`SPLITTER`";
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
    public void deleteByState(boolean state, int userID) {
        super.deleteByState(state, userID);
        write();
    }
}
