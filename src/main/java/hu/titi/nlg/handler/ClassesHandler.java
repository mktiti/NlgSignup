package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Class;
import hu.titi.nlg.entity.Pair;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.util.ErrorReport;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static hu.titi.nlg.util.Context.*;
import static spark.Spark.get;
import static spark.Spark.post;

public class ClassesHandler {

    public ClassesHandler() {
        get("/admin/classes", this::listClasses);
        get("/admin/classes/:year/:sign", this::getClassReport);
    }

    private String listClasses(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        model.put("years", Class.Year.values());
        model.put("signs", Class.Sign.values());
        return render(model, "admin-classes.vts");
    }

    private String getClassReport(Request request, Response response) {
        try {
            Class c = Class.of(Integer.parseInt(request.params(":year")), request.params(":sign"));
            if (c == null) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, "rossz adatok"));
                response.redirect("/admin/classes");
                return "";
            }

            Map<String, Object> model = newModel(request);
            model.put("class", c);
            model.put("tfnumber", timeframeRepo.getTimeframeNumber());
            model.put("students", studentRepo.getClassWithTfNumber(c));
            return render(model, "admin-class-report.vts");

        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, "rossz évfolyamszám"));
            response.redirect("/admin/classes");
        } catch (NoSuchElementException nsee) {
            System.out.println("No year or sign");
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, null));
            response.redirect("/admin/classes");
        }

        return "";
    }
}