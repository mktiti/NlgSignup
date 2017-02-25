package hu.titi.nlg;

import java.time.LocalTime;
import static java.time.LocalTime.*;

public enum TimeFrame {
    FIRST(parse("08:00"), parse("10:00")),
    SECOND(parse("10:00"), parse("12:00")),
    THIRD(parse("12:00"), parse("14:00")),
    FOURTH(parse("14:00"), parse("16:00"));

    private final LocalTime start;
    private final LocalTime end;

    TimeFrame(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
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