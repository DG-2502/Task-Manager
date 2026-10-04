package domain;

import java.time.LocalDate;

public class Task {
    private String title;
    private LocalDate startDate;
    private State state;
    private int creatorID;
    private int ID;

    public enum State {
        CLOSED, ACTIVE
    }

    public Task(String title, LocalDate startDate, State state, int creatorID) {
        this.title = title;
        this.startDate = startDate;
        this.state = state;
        this.creatorID = creatorID;
    }

    @Override
    public String toString() {
        return ID + " " + title + " | " + startDate + " " + state;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
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
