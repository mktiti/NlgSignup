package hu.titi.nlg.repo;

import hu.titi.nlg.entity.*;
import hu.titi.nlg.entity.Class;
import hu.titi.nlg.util.DBUtil;
import hu.titi.nlg.util.EventReportItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class EventRepo implements Repo<Event> {

    private static final String SELECT_ALL = "SELECT * FROM EVENT ORDER BY ID";
    private static final String SELECT_BY_ID = "SELECT * FROM EVENT WHERE ID = ?";
    private static final String SELECT_BY_TIMEFRAME = "SELECT * FROM EVENT, EVENT_TIMEFRAMES " +
                                                      "WHERE EVENT.ID = EVENT_TIMEFRAMES.EVENT_ID AND EVENT_TIMEFRAMES.TIMEFRAME_ID = ? " +
                                                      "ORDER BY ID";

    private static final String SELECT_BY_USER_BY_TIMEFRAME = "SELECT EVENT.ID, EVENT.NAME, EVENT.MAX_SIGNUPS " +
                                                              "FROM STUDENT, SIGNUP, EVENT, EVENT_TIMEFRAMES " +
                                                              "WHERE STUDENT.ID = SIGNUP.STUDENT_ID AND EVENT.ID = SIGNUP.EVENT_ID AND STUDENT.ID = ? " +
                                                                    "AND EVENT.ID = EVENT_TIMEFRAMES.EVENT_ID AND EVENT_TIMEFRAMES.TIMEFRAME_ID = ?";

    private static final String SELECT_UNASSIGNED = "SELECT ID, NAME, MAX_SIGNUPS " +
                                                    "FROM EVENT " +
                                                    "WHERE NOT EXISTS (" +
                                                        "SELECT 1 FROM EVENT_TIMEFRAMES WHERE EVENT_TIMEFRAMES.EVENT_ID = EVENT.ID" +
                                                    ")";

    private static final String SELECT_WITH_AVAILABLE_BY_TIMEFRAME = "SELECT ID, NAME, EVENT.MAX_SIGNUPS, SIGNUPS " +
                                                                     "FROM EVENT_SIGNUPS, EVENT, EVENT_TIMEFRAMES " +
                                                                     "WHERE EVENT_SIGNUPS.EVENT_ID = EVENT.ID AND EVENT.ID = EVENT_TIMEFRAMES.EVENT_ID AND EVENT_TIMEFRAMES.TIMEFRAME_ID = ? " +
                                                                     "ORDER BY NAME";

    private static final String INSERT_NEW = "INSERT INTO EVENT (NAME, MAX_SIGNUPS) VALUES(?, ?)";
    private static final String SET_TIMEFRAMES = "INSERT INTO EVENT_TIMEFRAMES VALUES(?, ?)";
    private static final String SET_BLACKLIST = "INSERT INTO BLACKLIST VALUES(?, ?, ?)";

    private static final String CHECK_BLACKLIST = "SELECT 'blacklisted' FROM STUDENT, BLACKLIST " +
                                                  "WHERE STUDENT.ID = ? AND BLACKLIST.EVENT_ID = ? AND " +
                                                  "STUDENT.CLASS_YEAR = BLACKLIST.CLASS_YEAR AND STUDENT.SIGN = BLACKLIST.SIGN";

    private static final String GET_BLACKLIST = "SELECT CLASS_YEAR, SIGN FROM BLACKLIST WHERE EVENT_ID = ? ORDER BY CLASS_YEAR, SIGN";

    private static final String DELETE_EXISTING_SIGNUP = "DELETE FROM SIGNUP " +
                                                         "WHERE STUDENT_ID = ? AND EVENT_ID IN " +
                                                            "(SELECT EVENT.ID FROM SIGNUP, EVENT, EVENT_TIMEFRAMES " +
                                                             "WHERE SIGNUP.EVENT_ID = EVENT.ID AND STUDENT_ID = ? AND EVENT.ID = EVENT_TIMEFRAMES.EVENT_ID AND EVENT.ID <> ? AND TIMEFRAME_ID IN " +
                                                                "(SELECT TIMEFRAME_ID FROM EVENT_TIMEFRAMES WHERE EVENT_ID = ?))";

    private static final String SELECT_HAS_AVAILABLE = "SELECT SIGNUPS < MAX_SIGNUPS FROM EVENT_SIGNUPS WHERE EVENT_ID = ?";

    private static final String SELECT_ALL_WITH_SIGNUPS_TIMEFRAMES = "SELECT EVENT.ID, EVENT.NAME, EVENT.MAX_SIGNUPS, TIMEFRAME.ID, TIMEFRAME.START_TIME, TIMEFRAME.END_TIME, SIGNUPS " +
                                                                     "FROM EVENT_SIGNUPS, EVENT, EVENT_TIMEFRAMES, TIMEFRAME " +
                                                                     "WHERE EVENT_SIGNUPS.EVENT_ID = EVENT.ID AND EVENT.ID = EVENT_TIMEFRAMES.EVENT_ID AND EVENT_TIMEFRAMES.TIMEFRAME_ID = TIMEFRAME.ID " +
                                                                     "ORDER BY TIMEFRAME.START_TIME, EVENT.NAME";

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

    public Collection<Triple<Event, Integer, Collection<Class>>> getEventsAndSignupsByTf(TimeFrame tf) {
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
            Collection<Triple<Event, Integer, Collection<Class>>> ret = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                Event event = fromSingleRow(resultSet);
                ret.add(new Triple<>(event, resultSet.getInt(4), getBlacklist(event.getId())));
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

    public Collection<Class> getBlacklist(int eventID) {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return null;
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement(GET_BLACKLIST);
            preparedStatement.setInt(1, eventID);
            resultSet = preparedStatement.executeQuery();
            LinkedList<Class> list = new LinkedList<>();
            while (resultSet.next()) {
                list.add(Class.of(resultSet.getShort(1), resultSet.getString(2)));
            }
            return list;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(conn);
        }

        return null;
    }

    public Collection<EventReportItem> getEventsAndSignups() {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return null;
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement(SELECT_ALL_WITH_SIGNUPS_TIMEFRAMES);

            resultSet = preparedStatement.executeQuery();
            LinkedList<EventReportItem> list = new LinkedList<>();
            while (resultSet.next()) {
                Event event = fromSingleRow(resultSet);
                TimeFrame tf = new TimeFrame(resultSet.getInt(4), resultSet.getTime(5).toLocalTime(), resultSet.getTime(6).toLocalTime());
                int signups = resultSet.getInt(7);

                Optional<EventReportItem> opt = list.stream().filter(t -> t.getEvent().getId() == event.getId()).findAny();
                if (opt.isPresent()) {
                    opt.get().getTimeframes().add(tf);
                } else {
                    List<TimeFrame> timeframes = new LinkedList<>();
                    timeframes.add(tf);
                    list.add(new EventReportItem(event, signups, timeframes, getBlacklist(event.getId())));
                }
            }
            return list;

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
        PreparedStatement checkBlacklist = null;
        PreparedStatement checkAvailable = null;
        //PreparedStatement updateDummy = null;
        PreparedStatement insertNew = null;
        PreparedStatement deletePrevious = null;
        ResultSet checkBlacklistResult = null;
        ResultSet checkAvailableResult = null;

        long start = System.currentTimeMillis();
        try {
            conn.setAutoCommit(false);

            //insertDummy = conn.prepareStatement(INSERT_DUMMY);
            //insertDummy.setInt(1, eventId);

            checkBlacklist = conn.prepareStatement(CHECK_BLACKLIST);
            checkBlacklist.setInt(1, studentId);
            checkBlacklist.setInt(2, eventId);

            checkBlacklistResult = checkBlacklist.executeQuery();
            if (checkBlacklistResult.next()) {
                System.out.println("Tried to sign up to blacklisted event");
                conn.rollback();
                return false;
            }

            checkAvailable = conn.prepareStatement(SELECT_HAS_AVAILABLE);
            checkAvailable.setInt(1, eventId);

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
                checkAvailableResult = checkAvailable.executeQuery();
          //      System.out.println(studentId + " - checking queried");
                if (!(checkAvailableResult.next() && checkAvailableResult.getBoolean(1))) {
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
            close(checkBlacklist);
            close(checkBlacklistResult);
            close(deletePrevious);
            close(insertNew);
            close(checkAvailableResult);
            close(checkAvailable);
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

    public boolean saveEvent(String name, int max, Collection<Integer> tfIds, Set<hu.titi.nlg.entity.Class> blacklist) {
        if (name == null || (name = name.trim()).length() == 0 || max <= 0 || tfIds == null || tfIds.size() == 0) {
            return false;
        }

        Connection conn = DBUtil.getConnection();
        PreparedStatement insertPrepared = null;
        PreparedStatement tfPrepared = null;
        PreparedStatement blacklistPrepared = null;

        if (conn == null) {
            return false;
        }

        try {
            conn.setAutoCommit(false);

            insertPrepared = conn.prepareStatement(INSERT_NEW, new String[] {"ID"});
            insertPrepared.setString(1, name);
            insertPrepared.setInt(2, max);
            insertPrepared.executeUpdate();
            ResultSet rs = insertPrepared.getGeneratedKeys();

            if (!rs.next()) {
                System.out.println("No generated id, rolling back event insertion");
                conn.rollback();
            }

            int eventId = rs.getInt(1);
            tfPrepared = conn.prepareStatement(SET_TIMEFRAMES);
            for (Integer tf : tfIds) {
                tfPrepared.setInt(1, eventId);
                tfPrepared.setInt(2, tf);
                tfPrepared.addBatch();
            }
            tfPrepared.executeBatch();

            if (blacklist != null) {
                blacklistPrepared = conn.prepareStatement(SET_BLACKLIST);
                for (Class c : blacklist) {
                    blacklistPrepared.setInt(1, eventId);
                    blacklistPrepared.setShort(2, c.year.value);
                    blacklistPrepared.setString(3, c.sign.name());
                    blacklistPrepared.addBatch();
                }
                blacklistPrepared.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Exception while runUpdate, possibly duplicate");
        } finally {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.out.println("Exception while rolling back event insertion");
            }
            close(insertPrepared);
            close(tfPrepared);
            close(blacklistPrepared);
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
        return new Event(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3));
    }

    public boolean deleteEvent(int id) {
        System.out.println("Deleting event [" + id + "]");
        boolean deleted = runUpdate(DELETE_SQL, ps -> ps.setInt(1, id));
        if (deleted) {
            eventDeleted(id);
        }
        return deleted;
    }

    public void deleteUnassignedEvents() {
        getMultipleFromSQL(SELECT_UNASSIGNED, ps -> {}).forEach(event -> deleteEvent(event.getId()));
    }
}
