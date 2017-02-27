package hu.titi.nlg.entity;

public class Student implements Comparable<Student> {

    private final int id;
    private final String name;
    private final String email;
    private final String code;

    public Student(String name, String email, String code) {
        id = -1;
        this.name = name;
        this.email = email;
        this.code = code;
    }

    public Student(int id, String name, String email, String code) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Student [" + id + "] {" + name + " - " + email + "}";
    }

    @Override
    public int compareTo(Student student) {
        if (student == null) {
            return 0;
        }

        return student.name.compareTo(name);
    }
}