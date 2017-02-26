package hu.titi.nlg.entity;

public class Event implements Comparable<Event> {

    private final int id;
    private final String name;
    private final String description;
    private final TimeFrame timeFrame;
    private final int maxStudents;

    public Event(int id, String name, String description, TimeFrame timeFrame, int maxStudents) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.timeFrame = timeFrame;
        this.maxStudents = maxStudents;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public String getDescription() {
        return description;
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

    public String toHtml() {
        return "<h2>" + name + " [" + maxStudents + "]</h2>" + description + "<br /><br />";
    }
}