package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Pair;
import hu.titi.nlg.entity.Student;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static hu.titi.nlg.handler.AdminHandler.UserRole;
import static hu.titi.nlg.util.Context.*;
import static spark.Spark.*;

public class StudentHandler {

    public StudentHandler() {

        redirect.any("/student/", "/student");

        before("/student", this::filterStudent);
        before("/student/*", this::filterStudent);

        get("/student", this::mainPage);
    }

    private String mainPage(Request request, Response response) {
        Map<String, Object> model = newModel(request);

        int studentID = request.session().attribute("studentID");

        Optional<Student> optStu = studentRepo.getStudentById(studentID);
        if (optStu.isPresent()) {
            model.put("table", timeframeRepo.getAll().stream()
                                       .map(tf -> new Pair<>(tf, eventRepo.getEventSignups(studentID, tf.getId())))
                                       .collect(Collectors.toList()));

            return render(model, "student-main-page.vts");
        }

        response.redirect("/logout");
        return "";
    }

    private void filterStudent(Request request, Response response) {
        Object oRole = request.session().attribute("role");
        Object oID = request.session().attribute("studentID");

        int id;
        if (!(oID instanceof Integer)) {
            request.session().removeAttribute("role");
            request.session().removeAttribute("studentID");
        } else {
            id = (Integer)oID;
        }

        UserRole role;
        if (!(oRole instanceof UserRole)) {
            request.session().removeAttribute("role");
            request.session().removeAttribute("studentID");
            role = null;
        } else {
            role = (UserRole)oRole;
        }

        if (role == null) {
            response.redirect("/login");
        } else if (role != UserRole.STUDENT) {
            response.redirect("/");
        }
    }

}