package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class StudentRepoDB {

    private static final String SELECT_ALL_SQL = "SELECT * FROM STUDENT ORDER BY EMAIL";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM STUDENT WHERE ID = ?";
    private static final String SELECT_BY_EMAIL_SQL = "SELECT * FROM STUDENT WHERE ID = ?";

    private static final String INSERT_NEW_SQL = "INSERT INTO STUDENT VALUES(?, ?, ?)";

    @FunctionalInterface
    private interface PrepStatementSetter { void set(PreparedStatement ps) throws SQLException; }

    public Optional<Student> getStudentByEmail(String email) {
        if (email == null || email.length() == 0) {
            return null;
        }

        return getSingleFromSQL(SELECT_BY_EMAIL_SQL, ps -> ps.setString(1, email));
    }

    public Optional<Student> getStudentById(int id) {
        return getSingleFromSQL(SELECT_BY_ID_SQL, ps -> ps.setInt(1, id));
    }

    private Optional<Student> getSingleFromSQL(String query, PrepStatementSetter setter) {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return null;
        }

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            setter.set(preparedStatement);
            ResultSet resultSet = preparedStatement.getResultSet();

            if (resultSet.next()) {
                return Optional.of(fromSingleRow(resultSet));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Collection<Student> getAllStudents() {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return null;
        }

        try {
            ResultSet result = conn.prepareStatement(SELECT_ALL_SQL).executeQuery();

            return fromResultSet(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean saveStudent(String email, String passkey) {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_NEW_SQL);
            preparedStatement.setInt(1, new Random().nextInt());
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passkey);

            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static Collection<Student> fromResultSet(ResultSet resultSet) throws SQLException {
        Collection<Student> ret = new ArrayList<>(resultSet.getFetchSize());

        while (resultSet.next()) {
            ret.add(fromSingleRow(resultSet));
        }

        resultSet.close();

        return ret;
    }

    private static Student fromSingleRow(ResultSet resultSet) throws SQLException {
        return new Student(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
    }

}
