package hu.titi.nlg.handler;

import hu.titi.nlg.util.Context;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.util.ErrorReport;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.Optional;

import static hu.titi.nlg.util.Context.newModel;
import static hu.titi.nlg.util.Context.render;
import static hu.titi.nlg.handler.AdminHandler.UserRole;
import static spark.Spark.*;

public class LoginHandler {

    public LoginHandler() {

        path("/login", () -> {
            before("", (req, res) -> {
                getHome(req).ifPresent(res::redirect);
            });

            get("", this::showLogin);
            post("", this::login);
        });

        get("/logout", this::logout);

    }

    private String login(Request request, Response response) {

        String email = request.queryParams("email");
        String pass = request.queryParams("password");

        if (email != null && pass != null && (email = email.trim()).length() > 0 && (pass = pass.trim()).length() > 0 && email.length() < 100 && pass.length() < 100) {
            if ("admin".equals(email)) {
                if ("adminpassword67".equals(pass)) {
                    request.session().attribute("role", AdminHandler.UserRole.ADMIN);
                    request.session().attribute("uname", "Admin");
                    response.redirect("/");
                } else {
                    request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.LOGIN, "hint: name pswd szám"));
                }
            } else {
                Optional<Student> student = Context.studentRepo.getStudentByEmail(email);

                if (student.isPresent() && pass.equals(student.get().getCode())) {
                    request.session().attribute("role", AdminHandler.UserRole.STUDENT);
                    request.session().attribute("studentID", student.get().getId());
                    request.session().attribute("uname", student.get().getName());
                    response.redirect("/");
                } else {
                    request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.LOGIN, null));
                }
            }
        } else {
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.LOGIN, null));
        }

        response.redirect("/login");
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
        request.session().removeAttribute("uname");

        response.redirect("/login");

        return "Kijelentkezve!";
    }

    private String showLogin(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        return render(model, "login.vts");
    }

}