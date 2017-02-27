package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.entity.TimeFrame;

import static hu.titi.nlg.DBUtil.PrepStatementSetter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class StudentRepo implements Repo<Student> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM STUDENT ORDER BY NAME";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM STUDENT WHERE ID = ?";
    private static final String SELECT_BY_EMAIL_SQL = "SELECT * FROM STUDENT WHERE EMAIL = ?";

    private static final String INSERT_NEW_SQL = "INSERT INTO STUDENT VALUES(?, ?, ?, ?)";

    public Map<TimeFrame, Event> getSignupByTimeframe(int timeframeID) {
        return null;
    }

    public Optional<Student> getStudentByEmail(String email) {
        if (email == null || email.length() == 0) {
            return Optional.empty();
        }

        return getSingleFromSQL(SELECT_BY_EMAIL_SQL, ps -> ps.setString(1, email));
    }

    public Optional<Student> getStudentById(int id) {
        return getSingleFromSQL(SELECT_BY_ID_SQL, ps -> ps.setInt(1, id));
    }

    public boolean saveStudent(String name, String email, String passkey) {
        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            if (conn == null) {
                return false;
            }

            preparedStatement = conn.prepareStatement(INSERT_NEW_SQL);
            preparedStatement.setInt(1, new Random().nextInt());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, passkey);

            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(preparedStatement);
            close(conn);
        }

        return false;
    }

    @Override
    public String selectAllQuery() {
        return SELECT_ALL_SQL;
    }

    @Override
    public Student fromSingleRow(ResultSet resultSet) throws SQLException {
        return new Student(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
    }

}