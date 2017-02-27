package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.TimeFrame;
import spark.Request;
import spark.Response;

import static hu.titi.nlg.Context.timeframeRepo;
import static hu.titi.nlg.Context.eventRepo;

import java.util.Collection;
import java.util.Optional;

import static spark.Spark.*;

public class EventHandler {

    public EventHandler() {
        get("/admin/events", (req, res) -> listEvents());
        post("/admin/events", this::saveEvent);

        get("/student/events/:tf", this::listEventsByTf);
        get("/student/events/signup/:eid", this::signUp);
    }

    private String saveEvent(Request req, Response res) {
        String name =  req.queryParams("name");
        int max = Integer.parseInt(req.queryParams("max"));
        int tfId = Integer.parseInt(req.queryParams("tf"));

        boolean added = eventRepo.saveEvent(name, max, tfId);
        res.redirect("/admin/events");
        return added ? "Event added" : "Failed to add event";
    }

    private String signUp(Request request, Response response) {
        String eventString = request.params(":eid");
        int studentID = request.session().attribute("studentID");
        try {
            int eventID = Integer.parseInt(eventString);

            System.out.println("Signup to " + eventID);
            eventRepo.signUp(eventID, studentID);

        } catch (NumberFormatException nfe) {
            System.out.println("NumberFormat error");
        }

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
            if (otf.isPresent()) {
                Collection<Event> events = eventRepo.getEventsByTimeframe(tf);

                StringBuilder sb = new StringBuilder();
                sb.append("<h1>").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("</h1><br>");
                sb.append("<table>").append("<tr><th>Név</th><th>Férőhelyek</th><th>Jelentkezés</th></tr>");

                for (Event e : events) {
                    sb.append("<tr><td>").append(e.getName()).append("</td><td>").append(e.getMaxStudents()).append("</td>");
                    sb.append("<td><a href=\"/student/events/signup/").append(e.getId()).append("\">Sign up</a></td></tr>");
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

        for (TimeFrame tf : timeFrames) {
            Collection<Event> events = eventRepo.getEventsByTimeframe(tf);
            sb.append(tf.getId()).append(" [").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("]<br>");
            for (Event e : events) {
                sb.append(e.getName()).append("<br>");
            }
            sb.append("<br>");
        }

        sb.append("<form action=\"/admin/events\" method=\"POST\">\n" +
                "  <br>Name:\n" +
                "  <input type=\"text\" name=\"name\" >\n" +
                "  <br>Max:\n" +
                "  <input type=\"text\" name=\"max\" >\n" +
                "  <br>TF:\n");

        for (TimeFrame tf : timeFrames) {
            sb.append("<br><input type=\"radio\" name=\"tf\" value=\"" + tf.getId() + "\">");
            sb.append(tf.getStart()).append(" - ").append(tf.getEnd());
        }

        sb.append("<br>\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");
        return sb.toString();
    }

}