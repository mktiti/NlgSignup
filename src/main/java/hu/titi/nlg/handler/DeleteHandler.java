package hu.titi.nlg.handler;

import hu.titi.nlg.util.ConfirmRequest;
import spark.Request;
import spark.Response;

import java.util.Map;

import static hu.titi.nlg.util.Context.newModel;
import static hu.titi.nlg.util.Context.render;
import static spark.Spark.get;

public class DeleteHandler {

    public DeleteHandler() {
        get("/admin/delete/*", this::askConfirm);
    }

    private String askConfirm(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        String path = request.pathInfo();

        path = "/admin" + path.substring(13);

        String message = null;
        if (path.startsWith("/admin/timeframes/delete")) {
            message = "Az idősáv törlése az összes benne talélható eseményt is törli!";
        } else if (path.startsWith("/admin/events/delete")) {
            message = "Az esemény törlése az összes erre történt jelentkezést is törli!";
        } else if (path.startsWith("/admin/students/deleteAll")) {
            message = "A művelet az összes diákot törli!";
        } else if (path.startsWith("/admin/students/delete")) {
            message = "A diák törlése az összes jelentkezését is törli!";
        }

        ConfirmRequest confirmReq = new ConfirmRequest(path, message);
        model.put("confirm", confirmReq);
        return render(model, "confirm.vts");
    }

}