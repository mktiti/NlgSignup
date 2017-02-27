package hu.titi.nlg;

import hu.titi.nlg.handler.*;
import hu.titi.nlg.repo.EventRepo;
import hu.titi.nlg.repo.StudentRepo;
import hu.titi.nlg.repo.TimeframeRepo;

import java.io.IOException;

import static spark.Spark.*;

public class Context {

    public static final StudentRepo studentRepo = new StudentRepo();
    public static final TimeframeRepo timeframeRepo = new TimeframeRepo();
    public static final EventRepo eventRepo = new EventRepo();

    public static void main(String[] args) {
        new Context();
    }

    private Context() {
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
}