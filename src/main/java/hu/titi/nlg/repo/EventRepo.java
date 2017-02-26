package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.TimeFrame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Random;

public class EventRepo implements Repo<Event> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM EVENT ORDER BY ID";
    private static final String SELECT_BY_TIMEFRAME_SQL = "SELECT * FROM EVENT WHERE TIMEFRAME_ID = ? ORDER BY ID";

    private static final String INSERT_NEW_SQL = "INSERT INTO EVENT VALUES(?, ?, ?, ?, ?)";

    public Collection<Event> getEventsByTimeframe(TimeFrame tf) {
        return getMultipleFromSQL(SELECT_BY_TIMEFRAME_SQL, ps -> ps.setInt(1, tf.getId()));
    }

    public boolean saveEvent(String name, String desc, int max, int tfId) {
        try (Connection conn = DBUtil.getConnection()) {
            if (conn == null) {
                return false;
            }

            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_NEW_SQL);
            preparedStatement.setInt(1, new Random().nextInt());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, desc);
            preparedStatement.setInt(4, tfId);
            preparedStatement.setInt(5, max);

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
    public Event fromSingleRow(ResultSet resultSet) throws SQLException {
        return new Event(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet.getInt(5));
    }
}
