package app.domain;

import java.time.LocalDate;

public class Task {
    private String title;
    private LocalDate startDate;
    private State state;
    private int creatorId;
    private int id;

    public enum State {
        CLOSED, ACTIVE
    }

    public Task(String title, LocalDate startDate, State state, int creatorId) {
        this.title = title;
        this.startDate = startDate;
        this.state = state;
        this.creatorId = creatorId;
    }

    @Override
    public String toString() {
        return id + " " + title + " | " + startDate + " " + state;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }
}
