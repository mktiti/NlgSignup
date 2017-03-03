package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Student;
import hu.titi.nlg.repo.TextManager;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static hu.titi.nlg.Context.*;
import static spark.Spark.*;

public class AdminHandler {

    enum UserRole { STUDENT, ADMIN }

    public AdminHandler() {

        redirect.any("/admin/", "/admin");

        before("/admin", this::filterAdmin);
        before("/admin/*", this::filterAdmin);

        get("/admin", this::mainPage);
        get("/admin/students", this::listStudents);
        get("/admin/students/diakok.csv", this::getFile);
        post("/admin/students", this::saveStudent);
        get("/admin/texts", this::listTexts);
        post("/admin/texts/:tid", this::updateText);

        post("/admin/students/upload", this::studentUpload);
        get("/admin/students/delete/:id", this::deleteStudent);
        get("/admin/students/deleteAll", this::deleteAllAtudents);

    }

    private String getFile(Request request, Response response) {
        response.type("application/force-download");
        StringBuilder sb = new StringBuilder();
        sb.append("Név;Email;Jelszó;\n");
        for (Student s : studentRepo.getAll()) {
            sb.append(s.getName()).append(';').append(s.getEmail()).append(';').append(s.getCode()).append(";\n");
        }

        return sb.toString();
    }

    private void filterAdmin(Request request, Response response) {
        Object oRole = request.session().attribute("role");
        UserRole role;
        if (!(oRole instanceof UserRole)) {
            request.session().removeAttribute("role");
            role = null;
        } else {
            role = (UserRole)oRole;
        }

        if (role == null) {
            response.redirect("/login");
        } else if (role != UserRole.ADMIN) {
            response.redirect("/");
        }
    }

    private String mainPage(Request request, Response response) {
        return render(newModel(request), "admin-main-page.vts");
    }

    private String studentUpload(Request request, Response response) {
        try {

            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
            Part file = request.raw().getPart("file");

            studentRepo.saveFromStream(file.getInputStream());

        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }

        response.redirect("/admin/students");
        return "";
    }

    private String deleteAllAtudents(Request request, Response response) {
        studentRepo.deleteAll();
        response.redirect("/admin/students");
        return "";
    }

    private String deleteStudent(Request request, Response response) {
        String id = request.params(":id");

        try {
            studentRepo.deleteStudent(Integer.parseInt(id));
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
        } finally {
            response.redirect("/admin/students");
        }

        return "";
    }

    private String saveStudent(Request req, Response res) {
        String name = req.queryParams("name");
        String email = req.queryParams("email");

        if (name != null && (name = name.trim()).length() > 0 && email != null && (email = email.trim()).length() > 0) {
            studentRepo.saveStudent(name, email);
        }
        res.redirect("/admin/students");
        return "";
    }

    private String listStudents(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        model.put("students", studentRepo.getAll());
        return render(model, "admin-students.vts");
    }

    private String updateText(Request request, Response response) {
        String string = request.queryParams("text");
        String tid = request.params(":tid");


        try {
            if (string == null || tid == null || (string = string.trim()).length() == 0) {
                return "";
            }

            int id = Integer.parseInt(tid);
            for (TextManager.Text text : TextManager.Text.values()) {
                if (text.getID() == id) {
                    TextManager.updateText(text, string);
                    break;
                }
            }

        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception");
        } finally {
            response.redirect("/admin/texts");
        }

        return "";
    }

    private String listTexts(Request request, Response response) {
        Map<String, Object> model = newModel(request);
        Map<String, TextManager.Text> texts = new HashMap<>();
        Arrays.stream(TextManager.Text.values()).forEach(t -> texts.put(t.getName(), t));
        model.put("texts", texts);
        return render(model, "admin-texts.vts");
    }

}