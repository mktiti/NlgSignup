package hu.titi.nlg.handler;

import spark.Request;
import spark.Response;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static hu.titi.nlg.Context.*;
import static spark.Spark.get;
import static spark.Spark.post;

public class TimeframeHandler {

    public TimeframeHandler() {
        get("/admin/timeframes", this::listAll);
        post("/admin/timeframes", this::saveTimeframe);
        post("/admin/timeframes/:tid", this::modifyTimeframe);
        get("/admin/timeframes/delete/:tid", this::deleteTimeframe);
    }

    private String deleteTimeframe(Request request, Response response) {
        String id = request.params(":tid");

        try {
            timeframeRepo.deleteTimeframe(Integer.parseInt(id));
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
        } finally {
            response.redirect("/admin/timeframes");
        }

        return "";
    }

    private String saveTimeframe(Request request, Response response) {
        String startString = request.queryParams("start");
        String endString = request.queryParams("end");

        try {
            LocalTime start = LocalTime.parse(startString);
            LocalTime end = LocalTime.parse(endString);

            timeframeRepo.saveTimeframe(start, end);
        } catch (DateTimeParseException | NumberFormatException e) {
            System.out.println("Format exception");
        } finally {
            response.redirect("/admin/timeframes");
        }

        return "";
    }

    private String modifyTimeframe(Request request, Response response) {
        String startString = request.queryParams("start");
        String endString = request.queryParams("end");
        String idString = request.params(":tid");

        try {
            LocalTime start = LocalTime.parse(startString);
            LocalTime end = LocalTime.parse(endString);
            int id = Integer.parseInt(idString);

            timeframeRepo.updateTimeframe(id, start, end);
        } catch (DateTimeParseException | NumberFormatException e) {
            System.out.println("Format exception");
        } finally {
            response.redirect("/admin/timeframes");
        }

        return "";
    }

    private String listAll(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        model.put("timeframes", timeframeRepo.getAll());
        return render(model, "admin-timeframes.vts");
    }

}
