package hu.titi.nlg.repo;

import hu.titi.nlg.util.DBUtil;

import java.sql.*;

public class StateManager {

    private static final String INSERT_SIGNUP_STATE = "INSERT INTO CONFIG (ID, VALUE) VALUES (?, ?)";
    private static final String UPDATE_SIGNUP_STATE = "UPDATE CONFIG SET VALUE = ? WHERE ID = ?";
    private static final String GET_SIGNUP_STATE = "SELECT VALUE FROM CONFIG WHERE ID = ?";

    private static final int SIGNUP_STATE_ID = 1;
    private static final String OPEN_VALUE = "OPEN";
    private static final String CLOSED_VALUE = "CLOSED";

    private static volatile boolean openToSignup = true;

    public static void insertSignupState() {
        final Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return;
        }

        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(INSERT_SIGNUP_STATE);
            statement.setInt(1, SIGNUP_STATE_ID);
            statement.setString(2, getConfigValue());
            statement.executeUpdate();
            System.out.println("Config init action used!");
        } catch (SQLException sqle) {
            System.out.println("Config init action failed, possibly state config already exists");
        } finally {
            DBUtil.close(statement);
            DBUtil.close(conn);
        }
    }

    public static void init() {
        final Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return;
        }

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.prepareStatement(GET_SIGNUP_STATE);
            statement.setInt(1, SIGNUP_STATE_ID);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                openToSignup = getState(resultSet.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(resultSet);
            DBUtil.close(statement);
            DBUtil.close(conn);
        }
    }

    private static String getConfigValue() {
        return openToSignup ? OPEN_VALUE : CLOSED_VALUE;
    }

    private static boolean getState(final String value) {
        return value == null || !value.trim().equals(CLOSED_VALUE);
    }

    public static void enableSignup() {
        setOpenness(true);
    }

    public static void disableSignup() {
        setOpenness(false);
    }

    private static void setOpenness(boolean open) {
        openToSignup = open;

        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return;
        }

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(UPDATE_SIGNUP_STATE);
            preparedStatement.setString(1, getConfigValue());
            preparedStatement.setInt(2, SIGNUP_STATE_ID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(preparedStatement);
            DBUtil.close(conn);
        }
    }

    public static boolean isOpenToSignup() {
        return openToSignup;
    }

}