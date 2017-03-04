package hu.titi.nlg.repo;

import hu.titi.nlg.util.DBUtil;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.Pair;
import hu.titi.nlg.entity.TimeFrame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EventRepo implements Repo<Event> {

    private static final String SELECT_ALL = "SELECT * FROM EVENT ORDER BY ID";
    private static final String SELECT_BY_ID = "SELECT * FROM EVENT WHERE ID = ?";
    private static final String SELECT_BY_TIMEFRAME = "SELECT * FROM EVENT WHERE TIMEFRAME_ID = ? ORDER BY ID";
    private static final String SELECT_BY_USER_BY_TIMEFRAME = "SELECT EVENT.ID, EVENT.NAME, EVENT.TIMEFRAME_ID, EVENT.MAX_SIGNUPS FROM STUDENT, SIGNUP, EVENT WHERE STUDENT.ID = SIGNUP.STUDENT_ID AND EVENT.ID = SIGNUP.EVENT_ID AND STUDENT.ID = ? AND EVENT.TIMEFRAME_ID = ?";
    private static final String SELECT_WITH_AVAILABLE_BY_TIMEFRAME = "SELECT ID, NAME, TIMEFRAME_ID, EVENT.MAX_SIGNUPS, SIGNUPS FROM EVENT_SIGNUPS, EVENT WHERE EVENT_SIGNUPS.EVENT_ID = EVENT.ID AND EVENT.TIMEFRAME_ID = ? ORDER BY NAME";

    private static final String INSERT_NEW = "INSERT INTO EVENT (NAME, TIMEFRAME_ID, MAX_SIGNUPS) VALUES(?, ?, ?)";

    private static final String DELETE_EXISTING_SIGNUP = "DELETE FROM SIGNUP WHERE STUDENT_ID = ? AND EVENT_ID = (SELECT EVENT.ID FROM SIGNUP, EVENT WHERE SIGNUP.EVENT_ID = EVENT.ID AND STUDENT_ID = ? AND EVENT.ID <> ? AND TIMEFRAME_ID = (SELECT TIMEFRAME_ID FROM EVENT WHERE ID = ?))";
    private static final String SELECT_HAS_AVAILABLE = "SELECT SIGNUPS < MAX_SIGNUPS FROM EVENT_SIGNUPS where event_ID = ?";

    /*
    private static final String INSERT_DUMMY = "INSERT INTO SIGNUP VALUES (?, 0)";
    private static final String UPDATE_DUMMY = "UPDATE SIGNUP SET STUDENT_ID = ? WHERE EVENT_ID = ? AND STUDENT_ID = 0";
*/

    private static final String DELETE_SQL = "DELETE FROM EVENT WHERE ID = ?";

    private static final Map<Integer, Object> eventLocks = new HashMap<>();

    private Object getLock(int eventId) {
        synchronized (eventLocks) {
            eventLocks.putIfAbsent(eventId, new Object());
            return eventLocks.get(eventId);
        }
    }

    private void eventDeleted(int eventId) {
        synchronized (eventLocks) {
            eventLocks.remove(eventId);
        }
    }

    public Optional<Event> getEventById(int id) {
        return getSingleFromSQL(SELECT_BY_ID, ps -> ps.setInt(1, id));
    }

    public Optional<Event> getEventSignups(int studentID, int timeFrameID) {
        return getSingleFromSQL(SELECT_BY_USER_BY_TIMEFRAME, ps -> {ps.setInt(1, studentID); ps.setInt(2, timeFrameID);});
    }

    public Collection<Pair<Event, Integer>> getEventsAndSignupsByTf(TimeFrame tf) {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return null;
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {

            preparedStatement = conn.prepareStatement(SELECT_WITH_AVAILABLE_BY_TIMEFRAME);
            preparedStatement.setInt(1, tf.getId());

            resultSet = preparedStatement.executeQuery();
            Collection<Pair<Event, Integer>> ret = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                ret.add(new Pair<>(fromSingleRow(resultSet), resultSet.getInt(5)));
            }

            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(conn);
        }

        return null;
    }

    public Collection<Event> getEventsByTimeframe(TimeFrame tf) {
        return getMultipleFromSQL(SELECT_BY_TIMEFRAME, ps -> ps.setInt(1, tf.getId()));
    }

    /*
    private void debugSleep(int sID) {
        if (sID == 10612) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    */

    public boolean signUp(int eventId, int studentId) {

        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return false;
        }

        //PreparedStatement insertDummy = null;
        PreparedStatement check = null;
        //PreparedStatement updateDummy = null;
        PreparedStatement insertNew = null;
        PreparedStatement deletePrevious = null;
        ResultSet checkResult = null;

        long start = System.currentTimeMillis();
        try {
            conn.setAutoCommit(false);

            //insertDummy = conn.prepareStatement(INSERT_DUMMY);
            //insertDummy.setInt(1, eventId);

            check = conn.prepareStatement(SELECT_HAS_AVAILABLE);
            check.setInt(1, eventId);

            /*updateDummy = conn.prepareStatement(UPDATE_DUMMY);
            updateDummy.setInt(1, studentId);
            updateDummy.setInt(2, eventId);
*/
            insertNew = conn.prepareStatement("INSERT INTO SIGNUP VALUES (?, ?)");
            insertNew.setInt(1, eventId);
            insertNew.setInt(2, studentId);

            deletePrevious = conn.prepareStatement(DELETE_EXISTING_SIGNUP);
            deletePrevious.setInt(1, studentId);
            deletePrevious.setInt(2, studentId);
            deletePrevious.setInt(3, eventId);
            deletePrevious.setInt(4, eventId);

        //    System.out.println("Prepared");

        //    debugSleep(studentId);

            synchronized (getLock(eventId)) {
                //System.out.println(studentId + " - inserting dummy");
                //insertDummy.executeUpdate();
                //System.out.println(studentId + " - inserted dummy");

                //debugSleep(studentId);

          //      System.out.println(studentId + " - checking");
                checkResult = check.executeQuery();
          //      System.out.println(studentId + " - checking queried");
                if (!(checkResult.next() && checkResult.getBoolean(1))) {
            //        System.out.println(studentId + " - no available place");
                //    conn.rollback();
                    return false;
                }

            //    debugSleep(studentId);

           //     System.out.println(studentId + " - inserting new");
                insertNew.executeUpdate();
           //     System.out.println(studentId + " - inserted new");


                //System.out.println(studentId + " - updating dummy");
                //updateDummy.executeUpdate();
                //System.out.println(studentId + " - updated dummy");

            //    debugSleep(studentId);

           //     System.out.println(studentId + " - deleting previous");
                deletePrevious.executeUpdate();
           //     System.out.println(studentId + " - deleting previous");

            //    debugSleep(studentId);

            //    System.out.println(studentId + " - commiting");
                conn.commit();
            //    System.out.println(studentId + " - commited");
            }
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
            close(insertNew);
            close(checkResult);
            close(check);
            //close(insertDummy);
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
        final String trimmedName;
        if (name == null || (trimmedName = name.trim()).length() <= 0 || max <= 0) {
            return false;
        }

        return runUpdate(INSERT_NEW, ps -> {
            ps.setString(1, trimmedName);
            ps.setInt(2, tfId);
            ps.setInt(3, max);
        });
    }

    @Override
    public String selectAllQuery() {
        return SELECT_ALL;
    }

    @Override
    public Event fromSingleRow(ResultSet resultSet) throws SQLException {
        return new Event(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4));
    }

    public boolean deleteEvent(int id) {
        boolean deleted = runUpdate(DELETE_SQL, ps -> ps.setInt(1, id));
        if (deleted) {
            eventDeleted(id);
        }
        return deleted;
    }
}
