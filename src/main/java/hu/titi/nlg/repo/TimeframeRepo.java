package hu.titi.nlg.repo;

import hu.titi.nlg.entity.TimeFrame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TimeframeRepo implements Repo<TimeFrame> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM TIMEFRAME ORDER BY START_TIME";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM TIMEFRAME WHERE ID = ?";
    private static final String INSERT_NEW_SQL = "INSERT INTO TIMEFRAME (START_TIME, END_TIME) VALUES(?, ?)";
    private static final String DELETE_SQL = "DELETE FROM TIMEFRAME WHERE ID = ?";
    private static final String UPDATE_SQL = "UPDATE TIMEFRAME SET START_TIME = ?, END_TIME = ? WHERE ID = ?";

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

    @Override
    public String selectAllQuery() {
        return SELECT_ALL_SQL;
    }

    @Override
    public TimeFrame fromSingleRow(ResultSet resultSet) throws SQLException {
        return new TimeFrame(resultSet.getInt(1), resultSet.getTime(2).toLocalTime(), resultSet.getTime(3).toLocalTime());
    }

}