package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;
import hu.titi.nlg.entity.Event;
import hu.titi.nlg.entity.TimeFrame;

import java.sql.*;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class EventRepo implements Repo<Event> {

    private static final String SELECT_ALL = "SELECT * FROM EVENT ORDER BY ID";
    private static final String SELECT_BY_TIMEFRAME = "SELECT * FROM EVENT WHERE TIMEFRAME_ID = ? ORDER BY ID";
    private static final String SELECT_BY_USER_BY_TIMEFRAME = "SELECT EVENT.ID, EVENT.NAME, EVENT.TIMEFRAME_ID, EVENT.MAX_SIGNUPS FROM STUDENT, SIGNUP, EVENT WHERE STUDENT.ID = SIGNUP.STUDENT_ID AND EVENT.ID = SIGNUP.EVENT_ID AND STUDENT.ID = ? AND EVENT.TIMEFRAME_ID = ?";

    private static final String INSERT_NEW = "INSERT INTO EVENT VALUES(?, ?, ?, ?)";

    private static final String DELETE_EXISTING_SIGNUP = "DELETE FROM SIGNUP WHERE STUDENT_ID = ? AND EVENT_ID = (SELECT EVENT.ID FROM SIGNUP, EVENT WHERE SIGNUP.EVENT_ID = EVENT.ID AND STUDENT_ID = ? AND TIMEFRAME_ID = (SELECT TIMEFRAME_ID FROM EVENT WHERE ID = ?))";
    private static final String SELECT_HAS_AVAILABLE = "SELECT SIGNUPS < MAX_SIGNUPS FROM EVENT_SIGNUPS where event_ID = ?";
    private static final String INSERT_SIGNUP = "INSERT INTO SIGNUP VALUES (?, ?)";

    public Optional<Event> getSignedupEvent(int userID, int timeFrameID) {
        return getSingleFromSQL(SELECT_BY_USER_BY_TIMEFRAME, ps -> {ps.setInt(1, userID); ps.setInt(2, timeFrameID);});
    }

    public Collection<Event> getEventsByTimeframe(TimeFrame tf) {
        return getMultipleFromSQL(SELECT_BY_TIMEFRAME, ps -> ps.setInt(1, tf.getId()));
    }

    public boolean signUp(int eventId, int studentId) {

        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return false;
        }

        PreparedStatement getAvailability = null;
        PreparedStatement signup = null;
        PreparedStatement deleteSignup = null;
        ResultSet resultSet = null;

        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);



            getAvailability = conn.prepareStatement(SELECT_HAS_AVAILABLE);
            getAvailability.setInt(1, eventId);

            deleteSignup = conn.prepareStatement(DELETE_EXISTING_SIGNUP);
            deleteSignup.setInt(1, studentId);
            deleteSignup.setInt(2, studentId);
            deleteSignup.setInt(3, eventId);

            signup = conn.prepareStatement(INSERT_SIGNUP);
            signup.setInt(1, eventId);
            signup.setInt(2, studentId);
            System.out.println(studentId + " - PrepStates all set up");

            resultSet = getAvailability.executeQuery();
            System.out.println(studentId + " - Availability queried");
            if (!(resultSet.next() && resultSet.getBoolean(1))) {
                System.out.println(studentId + " - no available place - rolling back");
                conn.rollback();
                return false;
            }


            System.out.println(studentId + " - Preparing to delete");
            deleteSignup.executeUpdate();
            System.out.println(studentId + " - Deleting previous signup");
            //Asdasd - 349034592
            //markuskri - 606674648
            /*
            if (studentId == 606674648) {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            */

            System.out.println(studentId + " - Preparing for creation");
            signup.executeUpdate();
            System.out.println(studentId + " - Creating new signup");

            conn.commit();
            System.out.println(studentId + " - Commited");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
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
            close(resultSet);
            close(getAvailability);
            close(deleteSignup);
            close(signup);
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            close(conn);
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
            preparedStatement.setInt(1, new Random().nextInt());
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, tfId);
            preparedStatement.setInt(4, max);

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
