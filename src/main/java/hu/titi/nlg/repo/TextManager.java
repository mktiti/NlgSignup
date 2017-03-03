package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;

import java.sql.*;

public class TextManager {

    private static final String SELECT_TEXT = "SELECT * FROM TEXT";
    private static final String UPDATE_TEXT = "UPDATE TEXT SET TEXT = ? WHERE ID = ?";
    private static final String INSERT_TEXT = "INSERT INTO TEXT VALUES (?, ?)";

    public enum Text {
        TITLE(1, "title", "Címsor", "NLG Egészségnap"),
        MESSAGE(2, "message", "Főoldali üzenet", "Jelentkezz egy-egy eseményre minden idősávban!");

        private final int ID;
        private final String name;
        private final String desc;
        private String string;

        Text(int id, String name, String desc, String s) {
            this.ID = id;
            this.name = name;
            this.desc = desc;
            this.string = s;
        }

        public String getName() {
            return name;
        }

        public int getID() {
            return ID;
        }

        public String getString() {
            return string;
        }

        private void setString(String s) {
            this.string = s;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static void init() {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return;
        }

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(SELECT_TEXT);

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String dbText = resultSet.getString(2);

                for (Text text : Text.values()) {
                    if (text.ID == id) {
                        text.setString(dbText);
                        System.out.println(text.name + " read from DB");
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(resultSet);
            DBUtil.close(statement);
            DBUtil.close(conn);
        }
    }

    public static void insertTexts() {
        for (Text text : Text.values()) {
            insertText(text);
        }
    }

    private static void insertText(Text text) {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return;
        }

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(INSERT_TEXT);
            preparedStatement.setInt(1, text.getID());
            preparedStatement.setString(2, text.getString());
            preparedStatement.executeUpdate();
            System.out.println("Text inserted");
        } catch (SQLException e) {
            System.out.println("Text couldn't be inserted, probably already created");
        } finally {
            DBUtil.close(preparedStatement);
            DBUtil.close(conn);
        }
    }

    public static void updateText(Text text, String s) {
        text.setString(s);

        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return;
        }

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(UPDATE_TEXT);
            preparedStatement.setString(1, text.getString());
            preparedStatement.setInt(2, text.getID());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(preparedStatement);
            DBUtil.close(conn);
        }
    }

}