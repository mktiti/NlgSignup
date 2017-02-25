package hu.titi.nlg;

public class Student implements Comparable<Student> {

    private long id;
    private final String email;
    private final String code;

    public Student(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public Student(long id, String email, String code) {
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

    public long getId() {
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