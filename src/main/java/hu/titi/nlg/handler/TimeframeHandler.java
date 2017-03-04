package hu.titi.nlg.handler;

import hu.titi.nlg.util.ErrorReport;
import spark.Request;
import spark.Response;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static hu.titi.nlg.util.Context.*;
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
            if (!timeframeRepo.deleteTimeframe(Integer.parseInt(id))) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.DELETE, null));
            }
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

            if (!timeframeRepo.saveTimeframe(start, end)) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.ADD, null));
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.ADD, null));
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

            if (!timeframeRepo.updateTimeframe(id, start, end)) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.MODIFY, null));
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.MODIFY, null));
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
