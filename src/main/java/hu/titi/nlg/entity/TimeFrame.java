package hu.titi.nlg.entity;

import java.time.LocalTime;

public class TimeFrame {

    private final int id;
    private final LocalTime start;
    private final LocalTime end;

    public TimeFrame(int id, LocalTime start, LocalTime end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public LocalTime getEnd() {
        return end;
    }

    public LocalTime getStart() {
        return start;
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }
}