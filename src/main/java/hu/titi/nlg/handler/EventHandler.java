package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.entity.TimeFrame;
import spark.Request;
import spark.Response;

import static hu.titi.nlg.Context.studentRepo;
import static hu.titi.nlg.Context.timeframeRepo;
import static hu.titi.nlg.Context.eventRepo;
import static hu.titi.nlg.Context.SEPARATOR;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static spark.Spark.*;

public class EventHandler {

    public EventHandler() {
        get("/admin/events", (req, res) -> listEvents());
        post("/admin/events", this::saveEvent);
        get("/admin/events/:eid/report.csv", this::getReportFile);

        get("/student/events/:tf", this::listEventsByTf);
        get("/student/events/signup/:eid", this::signUp);
    }

    private String getReportFile(Request request, Response response) {
        Event event = null;
        TimeFrame tf = null;
        Collection<Student> students = null;
        try {
            event = eventRepo.getEventById(Integer.parseInt(request.params(":eid"))).get();
            tf = timeframeRepo.getTimeframeById(event.getTimeFrameId()).get();
            students = studentRepo.getEventSignups(event.getId());
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
            halt(400);
            return "";
        } catch (NoSuchElementException nsee) {
            System.out.println("No event by id");
            halt(404);
            return "";
        }

        response.type("application/force-download");
        StringBuilder sb = new StringBuilder();
        sb.append(event.getName()).append(SEPARATOR).append(tf.getStart()).append(" - ").append(tf.getEnd()).append(SEPARATOR).append("\n\nNév").append(SEPARATOR)
          .append("E-mail").append(SEPARATOR).append('\n');

        for (Student s : students) {
            sb.append(s.getName()).append(SEPARATOR).append(s.getEmail()).append(SEPARATOR).append('\n');
        }

        return sb.toString();
    }

    private String saveEvent(Request req, Response res) {
        try {
            String name = req.queryParams("name");
            int max = Integer.parseInt(req.queryParams("max"));
            int tfId = Integer.parseInt(req.queryParams("tf"));

            eventRepo.saveEvent(name, max, tfId);
        } catch (NumberFormatException nfe) {
            System.out.println("Number format Exception");
        }
        res.redirect("/admin/events");
        return "";
    }

    private String signUp(Request request, Response response) {
        long start = System.currentTimeMillis();
        String eventString = request.params(":eid");
        int studentID = request.session().attribute("studentID");
        try {
            int eventID = Integer.parseInt(eventString);

            System.out.println("Signup to " + eventID);
            eventRepo.signUp(eventID, studentID);

        } catch (NumberFormatException nfe) {
            System.out.println("NumberFormat error");
        }

        System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
        response.redirect("/student");
        return "";
    }

    private String listEventsByTf(Request request, Response response) {
        String sTFID = request.params(":tf");
        try {
            Optional<TimeFrame> otf = timeframeRepo.getTimeframeById(Integer.parseInt(sTFID));
            if (!otf.isPresent()) {
                halt(500);
            }

            TimeFrame tf = otf.get();
            Optional<Event> signedUp = eventRepo.getEventSignups(request.session().attribute("studentID"), tf.getId());
            if (otf.isPresent()) {
                Collection<Event> events = eventRepo.getEventsByTimeframe(tf);

                StringBuilder sb = new StringBuilder();
                sb.append("<h1>").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("</h1><br>");
                sb.append("<table>").append("<tr><th>Név</th><th>Férőhelyek</th><th>Jelentkezés</th></tr>");

                for (Event e : events) {
                    sb.append("<tr><td>").append(e.getName()).append("</td><td>").append(e.getMaxStudents()).append("</td>");
                    sb.append("<td>");
                    if (!(signedUp.isPresent() && signedUp.get().getId() == e.getId())) {
                        sb.append("<a href=\"/student/events/signup/").append(e.getId()).append("\">Sign up</a>");
                    }
                    sb.append("</td></tr>");
                }

                sb.append("</table><br><a href=\"/student\">Vissza</a>");

                return sb.toString();
            }
        } catch (NumberFormatException nfe) {
            System.out.println("NumberFormat error");
        }

        response.redirect("/student");
        return "";
    }

    private String listEvents() {
        StringBuilder sb = new StringBuilder();
        Collection<TimeFrame> timeFrames = timeframeRepo.getAll();

        sb.append("<table><tr><th><Időpont></th><th>Név</th><th>Létszám</th><th>Módosítás</th><th>Törlés</th><th>Kivonat</th></tr>");

        sb.append("<tr><form action=\"/admin/events\" method=\"POST\"><td><select name=\"tf\">");
        for (TimeFrame tf : timeFrames) {
            sb.append("<option value=\"").append(tf.getId()).append("\">").append(tf.getStart()).append(" - ").append(tf.getEnd());
        }
        sb.append("</select></td>");
        sb.append("<td><input type=\"text\" name=\"name\" ></td><td><input type=\"text\" name=\"max\"></td><td><input type=\"submit\" value=\"Hozzáadás\"></td></tr>");

        for (TimeFrame tf : timeFrames) {
            Collection<Event> events = eventRepo.getEventsByTimeframe(tf);
            for (Event e : events) {
                sb.append("<tr><td>").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("</td><td>");
                sb.append(e.getName()).append("</td><td>").append(e.getMaxStudents()).append("</td><td></td><td></td><td>");
                sb.append("<a href=\"/admin/events/").append(e.getId()).append("/report.csv\">Kivonat</a>").append("</td></tr>");
            }
        }
        sb.append("</table>");

        sb.append("<br><br><a href=\"/admin\">Kijelentkezés</a>");

        return sb.toString();
    }

}