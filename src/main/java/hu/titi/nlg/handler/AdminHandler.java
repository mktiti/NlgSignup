package hu.titi.nlg.handler;

import hu.titi.nlg.Context;
import hu.titi.nlg.repo.StudentRepo;
import spark.Request;
import spark.Response;

import java.util.Random;

import static spark.Spark.*;

public class AdminHandler {

    public enum UserRole { STUDENT, ADMIN }

    private StudentRepo repo = Context.studentRepo;

    public AdminHandler() {

        before("/admin", this::filterAdmin);
        before("/admin/*", this::filterAdmin);

        get("/admin", this::mainPage);
        get("/admin/students", (req, res) -> listStudents());
        post("/admin/students", this::saveStudent);

    }

    private void filterAdmin(Request request, Response response) {
        Object oRole = request.session().attribute("role");
        UserRole role;
        if (!(oRole instanceof UserRole)) {
            request.session().removeAttribute("role");
            role = null;
        } else {
            role = (UserRole)oRole;
        }

        if (role == null) {
            response.redirect("/login");
        } else if (role != UserRole.ADMIN) {
            halt(401, "Nem engedélyezett művelet!");
        }
    }

    private String mainPage(Request request, Response response) {
        StringBuilder sb = new StringBuilder();

        sb.append("<a href=\"admin/timeframes\">Idősávok</a><br>");
        sb.append("<a href=\"admin/events\">Események</a><br>");
        sb.append("<a href=\"admin/students\">Diákok</a><br>");
        sb.append("<a href=\"logout\">Kijelentkezés</a><br>");

        return sb.toString();
    }

    private String saveStudent(Request req, Response res) {
        boolean added = repo.saveStudent(req.queryParams("name"), req.queryParams("email"), "PASS" + new Random().nextInt());
        res.redirect("/students");
        return added ? "Student added" : "Failed to add student";
    }

    private String listStudents() {
        StringBuilder sb = new StringBuilder();
        repo.getAll().stream().forEach(s -> sb.append(s.getEmail()).append(" -> ").append(s.getCode()).append("<br />"));

        sb.append("<form action=\"/admin/students\" method=\"POST\">\n" +
                "  <br>Name:\n" +
                "  <input type=\"text\" name=\"name\" >\n" +
                "  <br>Email:\n" +
                "  <input type=\"text\" name=\"email\" >\n" +
                "  <br>\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");
        return sb.toString();
    }

}