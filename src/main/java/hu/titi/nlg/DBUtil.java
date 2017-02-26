package hu.titi.nlg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtil {

    @FunctionalInterface
    public interface PrepStatementSetter { void set(PreparedStatement ps) throws SQLException; }

    private static final String DB_URL = "jdbc:derby:schoolDB";

    static {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        } catch (SQLException e) {
            e.printStackTrace();
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