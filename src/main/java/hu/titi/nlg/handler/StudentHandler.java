package hu.titi.nlg.handler;

import static spark.Spark.*;

import hu.titi.nlg.HelloWorld;
import hu.titi.nlg.repo.StudentRepo;
import spark.Request;
import spark.Response;

import java.util.Random;

public class StudentHandler {

    private StudentRepo repo = HelloWorld.studentRepo;

    public StudentHandler() {
        get("/students", (req, res) -> listStudents());
        post("/addStudent", this::saveStudent);
    }

    private String saveStudent(Request req, Response res) {
        boolean added = repo.saveStudent(req.queryParams("email"), "PASS" + new Random().nextInt());
        res.redirect("/students");
        return added ? "Student added" : "Failed to add student";
    }

    private String listStudents() {
        StringBuilder sb = new StringBuilder();
        repo.getAll().stream().forEach(s -> sb.append(s.getEmail()).append(" -> ").append(s.getCode()).append("<br />"));

        sb.append("<form action=\"/addStudent\" method=\"POST\">\n" +
                "  <br>Email:\n" +
                "  <input type=\"text\" name=\"email\" >\n" +
                "  <br>\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");
        return sb.toString();
    }

}