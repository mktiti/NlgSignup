package hu.titi.nlg.handler;

import static spark.Spark.*;
import static hu.titi.nlg.Context.timeframeRepo;

import spark.Request;
import spark.Response;

public class TimeframeHandler {

    public TimeframeHandler() {
        get("/admin/timeframes", (req, res) -> listAll());
        post("/admin/timeframes", this::saveTimeframe);
    }

    private String saveTimeframe(Request req, Response res) {
        boolean added = timeframeRepo.saveTimeframe(req.queryParams("from"), req.queryParams("to"));
        res.redirect("/admin/timeframes");
        return added ? "Timeframe added" : "Failed to add timeframe";
    }

    private String listAll() {
        StringBuilder sb = new StringBuilder();
        timeframeRepo.getAll().stream().forEach(tf -> sb.append(tf.getId())
                     .append(" [").append(tf.getStart()).append(" - ").append(tf.getEnd()).append("]")
                     .append("<br>"));

        sb.append("<form action=\"/admin/timeframes\" method=\"POST\">\n" +
                "  <br>From:\n" +
                "  <input type=\"time\" name=\"from\" >\n" +
                "  To:\n" +
                "  <input type=\"time\" name=\"to\" >\n" +
                "  <input type=\"submit\" value=\"Add\">\n" +
                "</form> ");

        return sb.toString();
    }

}
