package hu.titi.nlg.handler;

import static spark.Spark.*;

import hu.titi.nlg.HelloWorld;
import hu.titi.nlg.repo.TimeframeRepo;
import spark.Request;
import spark.Response;

public class TimeframeHandler {

    private TimeframeRepo repo = HelloWorld.timeframeRepo;

    public TimeframeHandler() {
        get("/timeframes", (req, res) -> listAll());
        post("/addTimeframe", this::saveTimeframe);
    }

    private String saveTimeframe(Request req, Response res) {
        boolean added = repo.saveStudent(req.queryParams("from"), req.queryParams("to"));
        res.redirect("/timeframes");
        return added ? "Timeframe added" : "Failed to add timeframe";
    }

    private String listAll() {
        StringBuilder sb = new StringBuilder();
        repo.getAll().stream().forEach(tf -> sb.append(tf.getId())
                     .append(" [").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("]")
                     .append("<br>"));

        sb.append("<form action=\"/addTimeframe\" method=\"POST\">\n" +
                "  <br>From:\n" +
                "  <input type=\"time\" name=\"from\" >\n" +
                "  To:\n" +
                "  <input type=\"time\" name=\"to\" >\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");

        return sb.toString();
    }

}
