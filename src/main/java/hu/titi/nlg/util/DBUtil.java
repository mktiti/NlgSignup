package hu.titi.nlg.util;

import hu.titi.nlg.repo.TextManager;

import java.sql.*;

public class DBUtil {

    @FunctionalInterface
    public interface PrepStatementSetter { void set(PreparedStatement ps) throws SQLException; }

    private static final String DB_URL = "jdbc:derby:schoolDB;create=true";

    private static final String CREATE_STUDENT = "CREATE TABLE STUDENT(ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1) PRIMARY KEY, NAME VARCHAR(100) NOT NULL, EMAIL VARCHAR(100) NOT NULL, CLASS_YEAR SMALLINT CONSTRAINT YEAR_CHK CHECK (CLASS_YEAR BETWEEN 9 AND 12), SIGN CHAR CONSTRAINT SIGN_CHK CHECK (SIGN IN ('A', 'B', 'C', 'D')), PASSKEY VARCHAR(20) NOT NULL, UNIQUE(EMAIL))";
    private static final String CREATE_TIMEFRAME = "CREATE TABLE TIMEFRAME(ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, START_TIME TIME NOT NULL, END_TIME TIME NOT NULL, UNIQUE(START_TIME), UNIQUE(END_TIME))";
    private static final String CREATE_EVENT = "CREATE TABLE EVENT(ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, NAME VARCHAR(200) NOT NULL, TIMEFRAME_ID INTEGER NOT NULL REFERENCES TIMEFRAME(ID) ON DELETE CASCADE ON UPDATE RESTRICT, MAX_SIGNUPS INT NOT NULL, UNIQUE(NAME))";
    private static final String CREATE_SIGNUP = "CREATE TABLE SIGNUP(EVENT_ID INT NOT NULL REFERENCES EVENT(ID) ON DELETE CASCADE ON UPDATE RESTRICT, STUDENT_ID INT NOT NULL REFERENCES STUDENT(ID) ON DELETE CASCADE ON UPDATE RESTRICT, PRIMARY KEY(EVENT_ID, STUDENT_ID))";
    private static final String CREATE_EVENT_SIGNUP = "CREATE VIEW EVENT_SIGNUPS AS SELECT EVENT.ID AS EVENT_ID, COUNT(SIGNUP.EVENT_ID) AS SIGNUPS, EVENT.MAX_SIGNUPS FROM EVENT LEFT OUTER JOIN SIGNUP ON EVENT.ID = SIGNUP.EVENT_ID GROUP BY EVENT.ID, EVENT.MAX_SIGNUPS";

    private static final String CREATE_TEXTS = "CREATE TABLE TEXT(ID INTEGER NOT NULL PRIMARY KEY, TEXT VARCHAR(1000) NOT NULL)";

    private static final String INSERT_DUMMY_STUDENT = "INSERT INTO STUDENT (NAME, EMAIL, PASSKEY) VALUES ('$TEMP$', '$TEMP$', '$TEMP$')";
    private static final String CHECK_DUMMY_STUDENT = "SELECT NAME FROM STUDENT WHERE ID = 0";

    static {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Connection conn = getConnection();
        Statement check = null;
        Statement insert = null;
        ResultSet checkRs = null;
        try {

            createTable(conn, CREATE_STUDENT);
            createTable(conn, CREATE_TIMEFRAME);
            createTable(conn, CREATE_EVENT);
            createTable(conn, CREATE_SIGNUP);
            createTable(conn, CREATE_EVENT_SIGNUP);
            createTable(conn, CREATE_TEXTS);

            TextManager.insertTexts();
            TextManager.init();

            check = conn.createStatement();
            checkRs = check.executeQuery(CHECK_DUMMY_STUDENT);
            if (!checkRs.next()) {
                insert = conn.createStatement();
                insert.execute(INSERT_DUMMY_STUDENT);
            }

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            close(insert);
            close(checkRs);
            close(check);
            close(conn);
        }
    }

    static void init() {
        // Force static initializer to run
    }

    private static void createTable(Connection conn, String query) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.execute(query);
            System.out.println("Init action used!");
        } catch (SQLException sqle) {
            System.out.println("Init action failed, possibly table exists");
        } finally {
            close(statement);
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

    public static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}