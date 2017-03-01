package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.entity.TimeFrame;
import javafx.util.Pair;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static hu.titi.nlg.Context.eventRepo;
import static spark.Spark.*;
import static hu.titi.nlg.handler.AdminHandler.UserRole;
import static hu.titi.nlg.Context.studentRepo;
import static hu.titi.nlg.Context.timeframeRepo;

public class StudentHandler {

    public StudentHandler() {

        redirect.any("/student/", "/student");

        before("/student", this::filterStudent);
        before("/student/*", this::filterStudent);

        get("/student", this::renderedPage);
    }

    private String renderedPage(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();

        int studentID = (Integer)request.session().attribute("studentID");

        Optional<Student> optStu = studentRepo.getStudentById(studentID);
        if (optStu.isPresent()) {
            Student student = optStu.get();
            model.put("table", timeframeRepo.getAll().stream()
                                       .map(tf -> new Pair<>(tf, eventRepo.getEventSignups(studentID, tf.getId())))
                                       .collect(Collectors.toList()));

            return render(model, "student-main-page.vts");
        }

        halt(401);
        return "";
    }

    public static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }

    /*
    private String mainPage(Request request, Response response) {
        int studentID = (Integer)request.session().attribute("studentID");

        Optional<Student> optStu = studentRepo.getStudentById(studentID);
        if (optStu.isPresent()) {
            Student student = optStu.get();
            Collection<TimeFrame> timeFrames = timeframeRepo.getAll();

            StringBuilder sb = new StringBuilder();
            sb.append("<h1>").append(student.getName()).append("</h1><br>");
            sb.append("<table>").append("<tr><th>Kezdet</th><th>Vég</th><th>Esemény</th></tr>");

            for (TimeFrame tf : timeFrames) {
                sb.append("<tr><td>").append(tf.getStart()).append("</td><td>").append(tf.getEnd()).append("</td><td>");
                sb.append(eventRepo.getEventSignups(student.getId(), tf.getId()).map(Event::getName).orElse("<i>Még nem jelentkeztél</i>"));
                sb.append("</td><td><a href=\"").append("/student/events/").append(tf.getId()).append("\">Módosítás</a></td></tr>");
            }

            sb.append("</table><br><a href=\"logout\">Kijelentkezés</a>");

            return sb.toString();
        }

        halt(401);
        return "";
    }
    */

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
            halt(401, "Nem engedélyezett művelet!");
        }
    }

}