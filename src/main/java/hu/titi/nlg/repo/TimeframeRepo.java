package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.TimeFrame;

import java.sql.*;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Random;

public class TimeframeRepo implements Repo<TimeFrame> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM TIMEFRAME ORDER BY START_TIME";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM TIMEFRAME WHERE ID = ?";
    private static final String INSERT_NEW_SQL = "INSERT INTO TIMEFRAME VALUES(?, ?, ?)";

    public Optional<TimeFrame> getTimeframeById(int id) {
        return getSingleFromSQL(SELECT_BY_ID_SQL, ps -> ps.setInt(1, id));
    }

    public boolean saveStudent(String fromString, String toString) {
        LocalTime from = LocalTime.parse(fromString);
        LocalTime to = LocalTime.parse(toString);

        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            if (conn == null) {
                return false;
            }

            preparedStatement = conn.prepareStatement(INSERT_NEW_SQL);
            preparedStatement.setInt(1, new Random().nextInt());
            preparedStatement.setTime(2, Time.valueOf(from));
            preparedStatement.setTime(3, Time.valueOf(to));

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
    public TimeFrame fromSingleRow(ResultSet resultSet) throws SQLException {
        return new TimeFrame(resultSet.getInt(1), resultSet.getTime(2).toLocalTime(), resultSet.getTime(3).toLocalTime());
    }
}