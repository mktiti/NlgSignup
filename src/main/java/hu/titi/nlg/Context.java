package hu.titi.nlg;

import hu.titi.nlg.handler.*;
import hu.titi.nlg.repo.EventRepo;
import hu.titi.nlg.repo.StudentRepo;
import hu.titi.nlg.repo.TextManager;
import hu.titi.nlg.repo.TimeframeRepo;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Context {

    public static final char SEPARATOR = ';';

    public static final StudentRepo studentRepo = new StudentRepo();
    public static final TimeframeRepo timeframeRepo = new TimeframeRepo();
    public static final EventRepo eventRepo = new EventRepo();

    public static void main(String[] args) {
        new Context();
    }

    private Context() {

        new File("uplod").mkdir();
        staticFiles.externalLocation("upload");

        redirect.get("/", "/login");
        get("/shutdown", (req, res) -> {stop(); return "Shutting down";});

        new StudentHandler();
        new TimeframeHandler();
        new EventHandler();
        new AdminHandler();
        new LoginHandler();

        /*
        try {
            org.apache.derby.tools.ij.main(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    public static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }

    public static Map<String, Object> newModel(Request request) {
        Map<String, Object> model = new HashMap<>();
        for (TextManager.Text text : TextManager.Text.values()) {
            model.put(text.getName(), text.getString());
        }
        model.put("uname", request.session().attribute("uname"));
        return model;
    }

}