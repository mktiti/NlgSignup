package hu.titi.nlg.handler;

import hu.titi.nlg.Context;
import hu.titi.nlg.entity.Student;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static spark.Spark.*;
import static hu.titi.nlg.handler.AdminHandler.UserRole;

public class LoginHandler {

    public LoginHandler() {

        path("/login", () -> {
            before("", (req, res) -> {
                getHome(req).ifPresent(res::redirect);
            });

            get("", (req, res) -> showLogin());
            post("", this::login);
        });

        get("/logout", this::logout);

    }

    private String login(Request request, Response response) {

        String email = request.queryParams("email");
        String pass = request.queryParams("password");

        if (email != null && pass != null && email.length() > 0 && pass.length() > 0 && email.length() < 100 && pass.length() < 100) {
            if ("admin".equals(email)) {
                if ("adminpassword67".equals(pass)) {
                    request.session().attribute("role", AdminHandler.UserRole.ADMIN);
                    response.redirect("/admin");
                } else {
                    response.redirect("/login");
                }
            } else {
                Optional<Student> student = Context.studentRepo.getStudentByEmail(email);

                if (student.isPresent() && pass.equals(student.get().getCode())) {
                    request.session().attribute("role", AdminHandler.UserRole.STUDENT);
                    request.session().attribute("studentID", student.get().getId());
                    response.redirect("/");
                } else {
                    response.redirect("/login");
                }
            }
        }

        return "";
    }

    private Optional<String> getHome(Request request) {
        Object oRole = request.session().attribute("role");

        if (oRole != null && oRole instanceof UserRole) {
            UserRole role = (UserRole)oRole;
            if (role == UserRole.ADMIN) {
                return Optional.of("/admin");
            } else if (role == UserRole.STUDENT) {
                Object oID = request.session().attribute("studentID");
                if (oID != null && oID instanceof Integer) {
                    return Optional.of("/student");
                }
            }
        }

        return Optional.empty();
    }

    private String logout(Request request, Response response) {
        request.session().removeAttribute("studentID");
        request.session().removeAttribute("role");

        response.redirect("/login");

        return "Kijelentkezve!";
    }

    private String showLogin() {
        StringBuilder sb = new StringBuilder();

        sb.append("<form action=\"/login\" method=\"POST\">\n" +
                "  <br>Email:\n" +
                "  <input type=\"text\" name=\"email\" >\n" +
                "  <br>Password:\n" +
                "  <input type=\"password\" name=\"password\" >\n" +
                "  <br>\n" +
                "  <input type=\"submit\" value=\"Login\">\n" +
                "</form> ");

        return sb.toString();
    }

}