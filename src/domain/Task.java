package domain;

import java.time.LocalDate;

public class Task {
    private String title;
    private LocalDate startDate;
    private boolean active;
    private int creatorID;
    private int ID;

    public Task(String title, LocalDate startDate, boolean active, int creatorID) {
        this.title = title;
        this.startDate = startDate;
        this.active = active;
        this.creatorID = creatorID;
    }

    @Override
    public String toString() {
        return ID + " " + title + " | " + startDate + " " + (active ? "Active" : "Closed");
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(int creatorID) {
        this.creatorID = creatorID;
    }
}
