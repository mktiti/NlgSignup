package hu.titi.nlg.handler;

import hu.titi.nlg.entity.Student;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Random;

import static spark.Spark.*;
import static hu.titi.nlg.Context.studentRepo;

public class AdminHandler {

    enum UserRole { STUDENT, ADMIN }

    public AdminHandler() {

        redirect.any("/admin/", "/admin");

        before("/admin", this::filterAdmin);
        before("/admin/*", this::filterAdmin);

        get("/admin", this::mainPage);
        get("/admin/students", (req, res) -> listStudents());
        get("/admin/students/diakok.csv", this::getFile);
        post("/admin/students", this::saveStudent);

        post("/admin/students/upload", this::studentUpload);

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
            halt(401, "Nem engedélyezett művelet!");
        }
    }

    private String mainPage(Request request, Response response) {
        StringBuilder sb = new StringBuilder();

        sb.append("<a href=\"admin/timeframes\">Idősávok</a><br>");
        sb.append("<a href=\"admin/events\">Események</a><br>");
        sb.append("<a href=\"admin/students\">Diákok</a><br>");
        sb.append("<a href=\"logout\">Kijelentkezés</a><br>");

        return sb.toString();
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

    private String saveStudent(Request req, Response res) {
        String name = req.queryParams("name");
        String email = req.queryParams("email");

        if (name != null && name.length() > 0 && email != null && email.length() > 0) {
            studentRepo.saveStudent(name, email);
        }
        res.redirect("/admin/students");
        return "";
    }

    private String listStudents() {
        StringBuilder sb = new StringBuilder();
        Collection<Student> students = studentRepo.getAll();

        sb.append("<table><tr><th>Név</th><th>E-mail</th><th>Jelszó</th><th></th></tr><tr>");
        sb.append("<form action=\"/admin/students\" method=\"POST\">" +
                "  <td><input type=\"text\" name=\"name\" ></td>" +
                "  <td><input type=\"text\" name=\"email\"></td><td></td>" +
                "  <td><input type=\"submit\" value=\"Add\"></td></tr>" +
                "</form> ");

        for (Student s : students) {
            sb.append("<tr><td>").append(s.getName()).append("</td><td>").append(s.getEmail())
              .append("</td><td>").append(s.getCode()).append("</td><td></td></tr>");
        }
        sb.append("</table>");
        sb.append("<a href=\"/\">Vissza</a>");
        sb.append("<a href=\"/admin/students/diakok.csv\">Fájl letöltése</a>");

        sb.append("<form action=\"/admin/students/upload\" method=\"post\" enctype=\"multipart/form-data\"><input type=\"file\" name=\"file\" accept=\"*.csv\"><input type=\"submit\"></form>");

        return sb.toString();
    }

}