package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.TimeFrame;

import java.sql.*;
import java.time.LocalTime;
import java.util.Random;

public class TimeframeRepo implements Repo<TimeFrame> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM TIMEFRAME ORDER BY START_TIME";
    private static final String INSERT_NEW_SQL = "INSERT INTO TIMEFRAME VALUES(?, ?, ?)";

    public boolean saveStudent(String fromString, String toString) {
        LocalTime from = LocalTime.parse(fromString);
        LocalTime to = LocalTime.parse(toString);

        try (Connection conn = DBUtil.getConnection()) {
            if (conn == null) {
                return false;
            }

            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_NEW_SQL);
            preparedStatement.setInt(1, new Random().nextInt());
            preparedStatement.setTime(2, Time.valueOf(from));
            preparedStatement.setTime(3, Time.valueOf(to));

            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String selectAllQuery() {
        return SELECT_ALL_SQL;
    }

    @Override
    public TimeFrame fromSingleRow(ResultSet resultSet) throws SQLException {
        return new TimeFrame(resultSet.getInt(1), resultSet.getTime(2).toLocalTime(), resultSet.getTime(3).toLocalTime());
    }
}