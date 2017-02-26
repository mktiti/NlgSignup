package hu.titi.nlg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String dbURL = "jdbc:derby:schoolDB";

    static {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}