package hu.titi.nlg;

import hu.titi.nlg.entity.Student;

import java.util.*;

public class StudentRepo {

    private final Set<Student> students = new HashSet<>();

    public Optional<Student> getStudentByEmail(final String email) {
        if (email == null) {
            return Optional.empty();
        }

        final String trimmed = email.trim();
        return students.stream().filter(s -> Objects.equals(s.getEmail(), trimmed)).findAny();
    }

    public Optional<Student> getStudentById(final long id) {
        return students.stream().filter(s -> s.getId() == id).findAny();
    }

    public Collection<Student> getAllStudents() {
        return Collections.unmodifiableCollection(students);
    }

    public StudentRepo() {
        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            students.add(new Student(i, "student" + i + "@gmail.com", Integer.toString(rand.nextInt())));
        }
    }

}
