package hu.titi.nlg;

import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.TimeFrame;

import java.util.*;
import java.util.stream.Collectors;

public class EventRepo {

    private final Set<Event> events = new HashSet<>();

    public Collection<Event> getEventsByTime(TimeFrame tf) {
        return events.stream().filter(e -> e.getTimeFrame() == tf).sorted().collect(Collectors.toList());
    }

    public Collection<Event> getAllEvents() {
        return Collections.unmodifiableCollection(events);
    }

    public EventRepo() {
     /*   Random rand = new Random();
        for (TimeFrame tf : TimeFrame.values()) {
            for (int i = 0; i < 4; i++) {
                events.add(new Event(id, "Event" + rand.nextInt(), "Blablabla", tf, rand.nextInt(30) + 10));
            }
        }
        */
    }

}