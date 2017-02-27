package hu.titi.nlg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtil {

    @FunctionalInterface
    public interface PrepStatementSetter { void set(PreparedStatement ps) throws SQLException; }

    private static final String DB_URL = "jdbc:derby:schoolDB;create=true";

    private static final String CREATE_STUDENT = "CREATE TABLE STUDENT(ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(100) NOT NULL, EMAIL VARCHAR(100) NOT NULL, PASSKEY VARCHAR(20) NOT NULL, UNIQUE(EMAIL))";
    private static final String CREATE_TIMEFRAME = "CREATE TABLE TIMEFRAME(ID INTEGER NOT NULL PRIMARY KEY, START_TIME TIME NOT NULL, END_TIME TIME NOT NULL, UNIQUE(START_TIME), UNIQUE(END_TIME))";
    private static final String CREATE_EVENT = "CREATE TABLE EVENT(ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(200) NOT NULL, TIMEFRAME_ID INTEGER NOT NULL REFERENCES TIMEFRAME(ID), MAX_SIGNUPS INT NOT NULL, UNIQUE(NAME))";
    private static final String CREATE_SIGNUP = "CREATE TABLE SIGNUP(EVENT_ID INT NOT NULL REFERENCES EVENT(ID), STUDENT_ID INT NOT NULL REFERENCES STUDENT(ID), PRIMARY KEY(EVENT_ID, STUDENT_ID))";
    private static final String CREATE_EVENT_SIGNUP = "CREATE VIEW EVENT_SIGNUPS AS SELECT EVENT.ID AS EVENT_ID, COUNT(SIGNUP.EVENT_ID) AS SIGNUPS, EVENT.MAX_SIGNUPS FROM EVENT LEFT OUTER JOIN SIGNUP ON EVENT.ID = SIGNUP.EVENT_ID GROUP BY EVENT.ID, EVENT.MAX_SIGNUPS";

    static {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = getConnection()) {

            createTable(conn, CREATE_STUDENT);
            createTable(conn, CREATE_TIMEFRAME);
            createTable(conn, CREATE_EVENT);
            createTable(conn, CREATE_SIGNUP);
            createTable(conn, CREATE_EVENT_SIGNUP);

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static void createTable(Connection conn, String query) {
        try {
            conn.createStatement().execute(query);
            System.out.println("Table created!");
        } catch (SQLException sqle) {
            System.out.println("Table probably already exists");
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}