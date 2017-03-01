package hu.titi.nlg.repo;

import hu.titi.nlg.Context;
import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.entity.TimeFrame;

import java.sql.*;
import java.util.*;

public class EventRepo implements Repo<Event> {

    private static final String SELECT_ALL = "SELECT * FROM EVENT ORDER BY ID";
    private static final String SELECT_BY_ID = "SELECT * FROM EVENT WHERE ID = ?";
    private static final String SELECT_BY_TIMEFRAME = "SELECT * FROM EVENT WHERE TIMEFRAME_ID = ? ORDER BY ID";
    private static final String SELECT_BY_USER_BY_TIMEFRAME = "SELECT EVENT.ID, EVENT.NAME, EVENT.TIMEFRAME_ID, EVENT.MAX_SIGNUPS FROM STUDENT, SIGNUP, EVENT WHERE STUDENT.ID = SIGNUP.STUDENT_ID AND EVENT.ID = SIGNUP.EVENT_ID AND STUDENT.ID = ? AND EVENT.TIMEFRAME_ID = ?";

    private static final String INSERT_NEW = "INSERT INTO EVENT (NAME, TIMEFRAME_ID, MAX_SIGNUPS) VALUES(?, ?, ?)";

    private static final String DELETE_EXISTING_SIGNUP = "DELETE FROM SIGNUP WHERE STUDENT_ID = ? AND EVENT_ID = (SELECT EVENT.ID FROM SIGNUP, EVENT WHERE SIGNUP.EVENT_ID = EVENT.ID AND STUDENT_ID = ? AND EVENT.ID <> ? AND TIMEFRAME_ID = (SELECT TIMEFRAME_ID FROM EVENT WHERE ID = ?))";
    private static final String SELECT_HAS_AVAILABLE = "SELECT SIGNUPS <= MAX_SIGNUPS FROM EVENT_SIGNUPS where event_ID = ?";
    private static final String INSERT_SIGNUP = "INSERT INTO SIGNUP VALUES (?, ?)";

    private static final String INSERT_DUMMY = "INSERT INTO SIGNUP VALUES (?, 0)";
    private static final String UPDATE_DUMMY = "UPDATE SIGNUP SET STUDENT_ID = ? WHERE EVENT_ID = ? AND STUDENT_ID = 0";

    /*
    private static final Map<Integer, Object> eventLocks = new HashMap<>();

    static {
        refreshEventlocks();
    }

    private static void refreshEventlocks() {
        eventLocks.clear();
        Context.timeframeRepo.getAll().stream().forEach(tf -> eventLocks.put(tf.getId(), new Object()));
    }
    */

    public Optional<Event> getEventById(int id) {
        return getSingleFromSQL(SELECT_BY_ID, ps -> ps.setInt(1, id));
    }

    public Optional<Event> getEventSignups(int studentID, int timeFrameID) {
        return getSingleFromSQL(SELECT_BY_USER_BY_TIMEFRAME, ps -> {ps.setInt(1, studentID); ps.setInt(2, timeFrameID);});
    }

    public Collection<Event> getEventsByTimeframe(TimeFrame tf) {
        return getMultipleFromSQL(SELECT_BY_TIMEFRAME, ps -> ps.setInt(1, tf.getId()));
    }

    private void debugSleep(int sID) {
        if (false && sID == 1) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean signUp(int eventId, int studentId) {

        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return false;
        }

        PreparedStatement insertDummy = null;
        PreparedStatement check = null;
        PreparedStatement updateDummy = null;
        PreparedStatement deletePrevious = null;
        ResultSet checkResult = null;

        long start = System.currentTimeMillis();
        try {
            conn.setAutoCommit(false);

            insertDummy = conn.prepareStatement(INSERT_DUMMY);
            insertDummy.setInt(1, eventId);

            check = conn.prepareStatement(SELECT_HAS_AVAILABLE);
            check.setInt(1, eventId);

            updateDummy = conn.prepareStatement(UPDATE_DUMMY);
            updateDummy.setInt(1, studentId);
            updateDummy.setInt(2, eventId);

            deletePrevious = conn.prepareStatement(DELETE_EXISTING_SIGNUP);
            deletePrevious.setInt(1, studentId);
            deletePrevious.setInt(2, studentId);
            deletePrevious.setInt(3, eventId);
            deletePrevious.setInt(4, eventId);

            System.out.println("Prepared");

            debugSleep(studentId);

            System.out.println(studentId + " - inserting dummy");
            insertDummy.executeUpdate();
            System.out.println(studentId + " - inserted dummy");

            debugSleep(studentId);

            System.out.println(studentId + " - checking");
            checkResult = check.executeQuery();
            System.out.println(studentId + " - checking queried");
            if (!(checkResult.next() && checkResult.getBoolean(1))) {
                System.out.println(studentId + " - no available place");
                conn.rollback();
                return false;
            }

            debugSleep(studentId);

            System.out.println(studentId + " - updating dummy");
            updateDummy.executeUpdate();
            System.out.println(studentId + " - updated dummy");

            debugSleep(studentId);

            System.out.println(studentId + " - deleting previous");
            deletePrevious.executeUpdate();
            System.out.println(studentId + " - deleting previous");

            debugSleep(studentId);

            System.out.println(studentId + " - commiting");
            conn.commit();
            System.out.println(studentId + " - commited");

            return true;

        } catch (SQLException e) {
            System.out.println("Exception caught - possibly already signed up or event full");
            try {
                conn.rollback();
                System.out.println(studentId + " - Rolled back after exception");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            close(deletePrevious);
            close(updateDummy);
            close(checkResult);
            close(check);
            close(insertDummy);
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            close(conn);
            System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
        }

        return false;
    }

    public boolean saveEvent(String name, int max, int tfId) {
        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            if (conn == null) {
                return false;
            }

            preparedStatement = conn.prepareStatement(INSERT_NEW);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, tfId);
            preparedStatement.setInt(3, max);

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
        return SELECT_ALL;
    }

    @Override
    public Event fromSingleRow(ResultSet resultSet) throws SQLException {
        return new Event(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4));
    }
}
