package hu.titi.nlg.handler;

import hu.titi.nlg.entity.*;
import hu.titi.nlg.entity.Class;
import hu.titi.nlg.util.ErrorReport;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.stream.Collectors;

import static hu.titi.nlg.util.Context.*;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.redirect;

public class EventHandler {

    public EventHandler() {
        get("/admin/events", this::listEvents);
        post("/admin/events", this::saveEvent);
        get("/admin/events/:eid", this::getReport);
        get("/admin/events/:eid/report.csv", this::getReportFile);
        get("/admin/events/report.csv", this::getCompleteReportFile);
        get("/admin/events/delete/:eid", this::deleteEvent);

        get("/student/events/:tf", this::listEventsByTf);
        get("/student/events/signup/:eid", this::signUp);

    }

    private String getCompleteReportFile(Request request, Response response) {
        StringBuilder sb = new StringBuilder();
        response.type("application/force-download");

        for (Event event : eventRepo.getAll()) {
            appendEvent(sb, timeframeRepo.getTimeframesOfEvent(event.getId()), event, studentRepo.getEventSignups(event.getId()));
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private String getReportFile(Request request, Response response) {
        Event event;
        Collection<TimeFrame> tfs;
        Collection<Student> students;
        try {
            event = eventRepo.getEventById(Integer.parseInt(request.params(":eid"))).get();
            tfs = timeframeRepo.getTimeframesOfEvent(event.getId());
            students = studentRepo.getEventSignups(event.getId());
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, "rossz azonosító"));
            response.redirect("/admin/events");
            return "";
        } catch (NoSuchElementException nsee) {
            System.out.println("No event by id");
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, null));

            response.redirect("/admin/events");
            return "";
        }

        response.type("application/force-download");
        StringBuilder sb = new StringBuilder();
        appendEvent(sb, tfs, event, students);
        return sb.toString();
    }

    private static void appendEvent(StringBuilder sb, Collection<TimeFrame> tfs, Event event, Collection<Student> students) {
        sb.append(event.getName()).append(SEPARATOR);
        for (TimeFrame tf : tfs) {
            sb.append("[").append(tf.getStart()).append("-").append(tf.getEnd()).append("]");
        }
        sb.append(SEPARATOR)
          .append("\n\nNév").append(SEPARATOR)
          .append("E-mail").append(SEPARATOR)
          .append("Osztály").append(SEPARATOR).append('\n');

        for (Student s : students) {
            sb.append(s.getName()).append(SEPARATOR).append(s.getEmail()).append(SEPARATOR).append(s.getaClass()).append(SEPARATOR).append('\n');
        }
    }

    private static Set<hu.titi.nlg.entity.Class> parseBlacklist(String s) {
        if (s == null || (s = s.trim()).length() == 0) {
            return Collections.emptySet();
        }

        Set<Class> ret = new HashSet<>();

        try {
            for (String name : s.split(",")) {
                name = name.trim().replaceAll("\\.", "");
                if (name.length() == 1 && name.charAt(0) == '9') {
                    name += '*';
                }

                if (name.length() != 2 && name.length() != 3) {
                    return null;
                }

                String yearString = name.substring(0, name.length() - 1);
                char signChar = name.charAt(name.length() - 1);

                int year = Integer.parseInt(yearString);

                if (signChar == '*') {
                    Collection<Class> classes = Class.getAllFromYear(year);
                    if (classes == null) {
                        System.out.println("Null classes while parsing blacklisted classes, reporting error");
                        return null;
                    }
                    ret.addAll(classes);
                } else {
                    Class c = Class.of(year, signChar);
                    if (c == null) {
                        System.out.println("Null class while parsing blacklisted classes, reporting error");
                        return null;
                    }
                    ret.add(Class.of(year, signChar));
                }

            }

            return ret;
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception while parsing blacklisted classes, reporting error");
        } catch (NullPointerException npe) {
            System.out.println("Null pointer exception while parsing blacklisted classes, reporting error");
        }

        return null;
    }

    private String saveEvent(Request request, Response response) {
        try {
            String name = request.queryParams("name");
            Set<Class> blacklist = parseBlacklist(request.queryParams("blacklist"));
            int max = Integer.parseInt(request.queryParams("max"));

            if (blacklist == null) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.ADD, "hibás tiltólista"));
                response.redirect("/admin/events");
                return null;
            }

            List<Integer> tfs = timeframeRepo.getAll().stream()
                                    .filter(tf -> "true".equals(request.queryParams("tf" + tf.getId())))
                                    .map(TimeFrame::getId)
                                    .collect(Collectors.toList());

            if (!eventRepo.saveEvent(name, max, tfs, blacklist)) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.ADD, null));
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Number format Exception");
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.ADD, "rossz max létszám"));
        }
        response.redirect("/admin/events");
        return "";
    }

    private String deleteEvent(Request request, Response response) {
        String id = request.params(":eid");

        try {
            if (!eventRepo.deleteEvent(Integer.parseInt(id))) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.DELETE, null));
            }
        } catch (NumberFormatException nfe) {
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.DELETE, "rossz azonosító"));
            System.out.println("Number format exception");
        } finally {
            response.redirect("/admin/events");
        }

        return "";
    }

    private String signUp(Request request, Response response) {
        String eventString = request.params(":eid");
        int studentID = request.session().attribute("studentID");
        try {
            int eventID = Integer.parseInt(eventString);

            if (!eventRepo.signUp(eventID, studentID)) {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.SIGNUP, null));
            }

        } catch (NumberFormatException nfe) {
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.SIGNUP, "nem lézető esemény"));
            System.out.println("NumberFormat error");
        }

        response.redirect("/student");
        return "";
    }

    private String listEventsByTf(Request request, Response response) {
        String sTFID = request.params(":tf");
        try {
            Optional<TimeFrame> otf = timeframeRepo.getTimeframeById(Integer.parseInt(sTFID));
            if (!otf.isPresent()) {
                response.redirect("/student");
                return "";
            }

            TimeFrame tf = otf.get();
            Optional<Student> studentOpt = studentRepo.getStudentById(request.session().attribute("studentID"));
            if (!studentOpt.isPresent()) {
                response.redirect("/logout");
                return "";
            }
            
            Optional<Event> signedUp = eventRepo.getEventSignups(request.session().attribute("studentID"), tf.getId());
            if (otf.isPresent()) {
                Map<String, Object> model = newModel(request);
                model.put("tf", tf);
                model.put("events", eventRepo.getEventsAndSignupsByTf(tf));
                model.put("signedUp", signedUp);
                model.put("class", studentOpt.get().getaClass());

                return render(model, "student-signup.vts");
            }

        } catch (NumberFormatException nfe) {
            System.out.println("NumberFormat error");
        }

        response.redirect("/student");
        return "";
    }

    private String listEvents(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        model.put("timeframes", timeframeRepo.getAll());
        model.put("table", eventRepo.getEventsAndSignups());
        return render(model, "admin-events.vts");
    }

    private String getReport(Request request, Response response) {
        try {
            Optional<Event> opt = eventRepo.getEventById(Integer.parseInt(request.params(":eid")));
            if (opt.isPresent()) {
                Map<String, Object> model = newModel(request);
                model.put("event", opt.get());
                model.put("students", studentRepo.getEventSignups(opt.get().getId()));
                return render(model, "admin-event-report.vts");
            } else {
                request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, "nem létező esemény"));
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
            request.session().attribute("error", new ErrorReport(ErrorReport.ErrorType.REPORT, "hibás azonosító"));
        }

        response.redirect("/admin/events");
        return "";
    }

}