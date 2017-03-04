package hu.titi.nlg.entity;

public class Event implements Comparable<Event> {

    private final int id;
    private final String name;
    private final int timeFrameId;
    private final int maxStudents;

    public Event(int id, String name, int timeFrameId, int maxStudents) {
        this.id = id;
        this.name = name;
        this.timeFrameId = timeFrameId;
        this.maxStudents = maxStudents;
    }

    public int getTimeFrameId() {
        return timeFrameId;
    }

    public String getName() {
        return name;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Event event) {
        if (event == null) {
            return 0;
        }

        return event.name.compareTo(name);
    }

    @Override
    public String toString() {
        return "Event [" + id + "] {" + name + " (" + maxStudents + ")}";
    }
}