package hu.titi.nlg.util;

import hu.titi.nlg.entity.Class;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.TimeFrame;

import java.util.Collection;
import java.util.List;

public class EventReportItem {

    private final Event event;
    private final int signups;
    private final List<TimeFrame> timeframes;
    private final Collection<Class> blacklist;

    public EventReportItem(Event first, int second, List<TimeFrame> third, Collection<Class> blacklist) {
        this.event = first;
        this.signups = second;
        this.timeframes = third;
        this.blacklist = blacklist;
    }

    public Event getEvent() {
        return event;
    }

    public int getSignups() {
        return signups;
    }

    public List<TimeFrame> getTimeframes() {
        return timeframes;
    }

    public Collection<Class> getBlacklist() {
        return blacklist;
    }

    public String getBlacklistString() {
        StringBuilder sb = new StringBuilder();
        for (Class c : blacklist) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(c.toString());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "{" + event + ", " + signups + ", " + timeframes + "}";
    }
}