package hu.titi.nlg;

import hu.titi.nlg.handler.EventHandler;
import hu.titi.nlg.handler.StudentHandler;
import hu.titi.nlg.handler.TimeframeHandler;
import hu.titi.nlg.repo.EventRepo;
import hu.titi.nlg.repo.StudentRepo;
import hu.titi.nlg.repo.TimeframeRepo;

import java.io.IOException;

import static spark.Spark.*;

public class HelloWorld {

    public static final StudentRepo studentRepo = new StudentRepo();
    public static final TimeframeRepo timeframeRepo = new TimeframeRepo();
    public static final EventRepo eventRepo = new EventRepo();

    public static void main(String[] args) {
        new HelloWorld();
    }

    private HelloWorld() {
        get("/shutdown", (req, res) -> {stop(); return "Shutting down";});

        new StudentHandler();
        new TimeframeHandler();
        new EventHandler();

        try {
            org.apache.derby.tools.ij.main(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}