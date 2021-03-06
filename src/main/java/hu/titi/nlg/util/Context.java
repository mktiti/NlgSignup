package hu.titi.nlg.util;

import hu.titi.nlg.handler.*;
import hu.titi.nlg.repo.*;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.port;
import static spark.Spark.redirect;

public class Context {

    public static final char SEPARATOR = ';';

    public static final StudentRepo studentRepo = new StudentRepo();
    public static final TimeframeRepo timeframeRepo = new TimeframeRepo();
    public static final EventRepo eventRepo = new EventRepo();

    public static void main(String[] args) {
        new Context(args);
    }

    private Context(String[] args) {

        int port = 4567;
        if (args.length > 0) {
            try {
                System.out.println(args[0]);
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.print("Port number uninterpretable");
            }
        }

        System.out.println("Port: " + port);
        port(port);

        String adminPassword = "adminpassword67";
        try (BufferedReader br = new BufferedReader(new FileReader("admin.txt"))) {
            String line = br.readLine();
            if (line != null && (line = line.trim()).length() > 0) {
                adminPassword = line;
                System.out.println("Admin password read from file");
            }
        } catch (IOException ioe) {
            System.out.println("Cannot read admin file, using default admin password");
        }

        DBUtil.init();

        new StudentHandler();
        new TimeframeHandler();
        new EventHandler();
        new ClassesHandler();
        new AdminHandler();
        new LoginHandler(adminPassword);
        new DeleteHandler();

        redirect.get("/", "/login");
    }

    public static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }

    public static Map<String, Object> newModel(Request request) {
        Map<String, Object> model = new HashMap<>();
        for (TextManager.Text text : TextManager.Text.values()) {
            model.put(text.getName(), text.getString());
        }
        model.put("openToSignup", StateManager.isOpenToSignup());

        model.put("uname", request.session().attribute("uname"));
        ErrorReport error = request.session().attribute("error");
        if (error != null) {
            model.put("error", error.getText());
            request.session().removeAttribute("error");
        }
        return model;
    }

}