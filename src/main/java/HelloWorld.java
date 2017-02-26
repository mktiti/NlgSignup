import hu.titi.nlg.EventRepo;
import hu.titi.nlg.repo.StudentRepoDB;
import spark.Request;
import spark.Response;

import java.util.Random;

import static spark.Spark.*;

public class HelloWorld {

    //private final StudentRepo studentRepo = new StudentRepo();
    private final StudentRepoDB studentRepo = new StudentRepoDB();
    private final EventRepo eventRepo = new EventRepo();

    public static void main(String[] args) {
        new HelloWorld();
    }

    private HelloWorld() {
        get("/hello", (req, res) -> "Hello World!");
        get("/shutdown", (req, res) -> {stop(); return "Shutting down";});
        get("/students", (req, res) -> listStudents());
        get("/events", (req, res) -> listEvents());
        post("/addStudent", this::saveStudent);

        /*
        try {
            org.apache.derby.tools.ij.main(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    private String saveStudent(Request req, Response res) {
        boolean added = studentRepo.saveStudent(req.queryParams("email"), "PASS" + new Random().nextInt());
        res.redirect("/students");
        return added ? "Student added" : "Failed to add student";
    }

    private String listStudents() {
        StringBuilder sb = new StringBuilder();
        //studentRepo.getAllStudents().stream().forEach(s -> sb.append(s.getEmail()).append("<br />"));
        studentRepo.getAllStudents().stream().forEach(s -> sb.append(s.getEmail()).append(" -> ").append(s.getCode()).append("<br />"));

        sb.append("<form action=\"/addStudent\" method=\"POST\">\n" +
                "  Email:<br>\n" +
                "  <input type=\"text\" name=\"email\" >\n" +
                "  <br>\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");
        return sb.toString();
    }

    private String listEvents() {
        StringBuilder sb = new StringBuilder();
     /*   for (TimeFrame tf : TimeFrame.values()) {
            sb.append("<h1>").append(tf).append("</h1>");
            eventRepo.getEventsByTime(tf).stream().forEach(e -> sb.append(e.toHtml()));
        }
      */  return sb.toString();
    }
}