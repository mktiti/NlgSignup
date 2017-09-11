package hu.titi.nlg.repo;

import hu.titi.nlg.entity.TimeFrame;
import hu.titi.nlg.util.DBUtil;

import java.sql.*;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;

public class TimeframeRepo implements Repo<TimeFrame> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM TIMEFRAME ORDER BY START_TIME";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM TIMEFRAME WHERE ID = ?";
    private static final String INSERT_NEW_SQL = "INSERT INTO TIMEFRAME (START_TIME, END_TIME) VALUES(?, ?)";
    private static final String DELETE_SQL = "DELETE FROM TIMEFRAME WHERE ID = ?";
    private static final String UPDATE_SQL = "UPDATE TIMEFRAME SET START_TIME = ?, END_TIME = ? WHERE ID = ?";
    private static final String GET_NUMBER_SQL = "SELECT COUNT(ID) FROM TIMEFRAME";
    private static final String GET_TIMEFRAMES_OF_EVENT = "SELECT TIMEFRAME.ID, TIMEFRAME.START_TIME, TIMEFRAME.END_TIME " +
                                                          "FROM EVENT_TIMEFRAMES, TIMEFRAME " +
                                                          "WHERE EVENT_TIMEFRAMES.EVENT_ID = ? AND EVENT_TIMEFRAMES.TIMEFRAME_ID = TIMEFRAME.ID";

    public Optional<TimeFrame> getTimeframeById(int id) {
        return getSingleFromSQL(SELECT_BY_ID_SQL, ps -> ps.setInt(1, id));
    }

    public boolean saveTimeframe(LocalTime start, LocalTime end) {
        return runUpdate(INSERT_NEW_SQL, ps -> {
            ps.setTime(1, Time.valueOf(start));
            ps.setTime(2, Time.valueOf(end));
        });
    }

    public boolean updateTimeframe(int id, LocalTime start, LocalTime end) {
        return runUpdate(UPDATE_SQL, ps -> {
            ps.setTime(1, Time.valueOf(start));
            ps.setTime(2, Time.valueOf(end));
            ps.setInt(3, id);
        });
    }

    public boolean deleteTimeframe(int id) {
        return runUpdate(DELETE_SQL, ps -> ps.setInt(1, id));
    }

    public int getTimeframeNumber() {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return 0;
        }

        Statement statement = null;
        ResultSet resultSet = null;
        try {

            statement = conn.createStatement();
            resultSet = statement.executeQuery(GET_NUMBER_SQL);

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(statement);
            close(conn);
        }

        return 0;
    }

    public Collection<TimeFrame> getTimeframesOfEvent(int eventId) {
        return getMultipleFromSQL(GET_TIMEFRAMES_OF_EVENT, ps -> ps.setInt(1, eventId));
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