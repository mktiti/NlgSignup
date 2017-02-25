import hu.titi.nlg.Event;
import hu.titi.nlg.EventRepo;
import hu.titi.nlg.StudentRepo;
import hu.titi.nlg.TimeFrame;

import static spark.Spark.*;

public class HelloWorld {

    StudentRepo studentRepo = new StudentRepo();
    EventRepo eventRepo = new EventRepo();

    public static void main(String[] args) {
        new HelloWorld();
    }

    HelloWorld() {
        get("/hello", (req, res) -> "Hello World!");
     //   get("/shutdown", (req, res) -> {stop(); return "Shutting down";});
        get("/students", (req, res) -> listStudents());
        get("/events", (req, res) -> listEvents());
    }

    private String listStudents() {
        StringBuilder sb = new StringBuilder();
        studentRepo.getAllStudents().stream().forEach(s -> sb.append(s.getEmail()).append("<br />"));
        return sb.toString();
    }

    private String listEvents() {
        StringBuilder sb = new StringBuilder();
        for (TimeFrame tf : TimeFrame.values()) {
            sb.append("<h1>").append(tf).append("</h1>");
            eventRepo.getEventsByTime(tf).stream().forEach(e -> sb.append(e.toHtml()));
        }
        return sb.toString();
    }
}