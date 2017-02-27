package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.entity.TimeFrame;
import spark.Request;
import spark.Response;

import java.sql.Time;
import java.util.Collection;
import java.util.Optional;

import static hu.titi.nlg.Context.eventRepo;
import static spark.Spark.*;
import static hu.titi.nlg.handler.AdminHandler.UserRole;
import static hu.titi.nlg.Context.studentRepo;
import static hu.titi.nlg.Context.timeframeRepo;

public class StudentHandler {

    public StudentHandler() {

        before("/student", this::filterStudent);
        before("/student/*", this::filterStudent);

        get("/student", this::mainPage);
    }

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
                sb.append("<tr><td><a href=\"student/events/").append(tf.getId()).append("\">").append(tf.getStart()).append("</a></td><td>").append(tf.getEnd()).append("</td><td>");
                sb.append(eventRepo.getSignedupEvent(student.getId(), tf.getId()).map(Event::getName).orElse("<i>Még nem jelentkeztél</i>"));
                sb.append("</td></tr>");
            }

            sb.append("</table><br><a href=\"logout\">Kijelentkezés</a>");

            return sb.toString();
        }

        halt(401);
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
            halt(401, "Nem engedélyezett művelet!");
        }
    }

}