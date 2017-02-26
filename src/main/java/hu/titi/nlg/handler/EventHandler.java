package hu.titi.nlg.handler;

import hu.titi.nlg.HelloWorld;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.TimeFrame;
import hu.titi.nlg.repo.EventRepo;
import hu.titi.nlg.repo.TimeframeRepo;
import spark.Request;
import spark.Response;

import java.util.Collection;

import static spark.Spark.*;

public class EventHandler {

    private EventRepo repo = HelloWorld.eventRepo;
    private TimeframeRepo timeframeRepo = HelloWorld.timeframeRepo;

    public EventHandler() {
        get("/events", (req, res) -> listEvents());
        post("/addEvent", this::saveEvent);
    }

    private String saveEvent(Request req, Response res) {
        String name =  req.queryParams("name");
        String desc = req.queryParams("desc");
        int max = Integer.parseInt(req.queryParams("max"));
        int tfId = Integer.parseInt(req.queryParams("tf"));

        boolean added = repo.saveEvent(name, desc, max, tfId);
        res.redirect("/events");
        return added ? "Event added" : "Failed to add event";
    }

    private String listEvents() {
        StringBuilder sb = new StringBuilder();

        Collection<TimeFrame> timeFrames = timeframeRepo.getAll();

        for (TimeFrame tf : timeFrames) {
            Collection<Event> events = repo.getEventsByTimeframe(tf);
            sb.append(tf.getId()).append(" [").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("]<br>");
            for (Event e : events) {
                sb.append(e.getName()).append("<br>");
            }
            sb.append("<br>");
        }

        sb.append("<form action=\"/addEvent\" method=\"POST\">\n" +
                "  <br>Name:\n" +
                "  <input type=\"text\" name=\"name\" >\n" +
                "  <br>Desc:\n" +
                "  <input type=\"text\" name=\"decs\" >\n" +
                "  <br>Max:\n" +
                "  <input type=\"text\" name=\"max\" >\n" +
                "  <br>TF:\n" +
                "  <input type=\"text\" name=\"tf\" >\n" +
                "  <br>\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");
        return sb.toString();
    }

}