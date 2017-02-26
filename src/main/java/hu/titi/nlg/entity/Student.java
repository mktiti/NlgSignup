package hu.titi.nlg.entity;

public class Student implements Comparable<Student> {

    private int id;
    private final String email;
    private final String code;

    public Student(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public Student(int id, String email, String code) {
        this.id = id;
        this.email = email;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Student {" + email + "}";
    }

    @Override
    public int compareTo(Student student) {
        if (student == null) {
            return 0;
        }

        return student.email.compareTo(email);
    }
}