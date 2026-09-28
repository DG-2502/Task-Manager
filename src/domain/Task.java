package domain;

import java.time.LocalDate;

public class Task {
    private String title;
    private LocalDate startDate;
    private boolean state;
    private int ID;

    public Task(String title, LocalDate startDate) {
        this.title = title;
        this.startDate = startDate;
        this.state = true;
    }

    @Override
    public String toString() {
        return ID + " " + title + " | " + startDate + " " + (state ? "Active" : "Closed");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

}
