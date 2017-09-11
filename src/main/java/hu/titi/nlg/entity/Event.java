package hu.titi.nlg.entity;

public class Event implements Comparable<Event> {

    private final int id;
    private final String name;
    private final int maxStudents;

    public Event(int id, String name, int maxStudents) {
        this.id = id;
        this.name = name;
        this.maxStudents = maxStudents;
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
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && id == ((Event) o).id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Event [" + id + "] {" + name + " (" + maxStudents + ")}";
    }
}