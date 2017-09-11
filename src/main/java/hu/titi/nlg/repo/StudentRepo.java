package hu.titi.nlg.repo;

import hu.titi.nlg.entity.Class;
import hu.titi.nlg.entity.Pair;
import hu.titi.nlg.entity.Student;
import hu.titi.nlg.util.Context;
import hu.titi.nlg.util.DBUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class StudentRepo implements Repo<Student> {

    private static final String SELECT_ALL_SQL = "SELECT * FROM STUDENT WHERE ID <> 0 ORDER BY NAME";
    private static final String DELETE_SQL = "DELETE FROM STUDENT WHERE ID <> 0 AND ID = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM STUDENT WHERE ID <> 0";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM STUDENT WHERE ID <> 0 AND ID = ?";
    private static final String SELECT_BY_EMAIL_SQL = "SELECT * FROM STUDENT WHERE ID <> 0 AND EMAIL = ?";
    private static final String SELECT_BY_CLASS = "SELECT * FROM STUDENT WHERE ID <> 0 AND CLASS_YEAR = ? AND SIGN = ?";

    private static final String SELECT_BY_EVENT_ID = "SELECT STUDENT.ID, STUDENT.NAME, STUDENT.EMAIL, STUDENT.CLASS_YEAR, STUDENT.SIGN, STUDENT.PASSKEY " +
                                                     "FROM STUDENT, SIGNUP " +
                                                     "WHERE STUDENT.ID <> 0 AND SIGNUP.STUDENT_ID = STUDENT.ID AND SIGNUP.EVENT_ID = ?";

    private static final String SELECT_BY_CLASS_WITH_TF_NUMBER = "SELECT STUDENT.ID, STUDENT.NAME, STUDENT.EMAIL, STUDENT.CLASS_YEAR, STUDENT.SIGN, STUDENT.PASSKEY, SUM(CASE WHEN TIMEFRAME.ID IS NULL THEN 0 ELSE 1 END) " +
                                                                 "FROM STUDENT LEFT JOIN SIGNUP ON STUDENT.ID = SIGNUP.STUDENT_ID " +
                                                                 "LEFT JOIN EVENT ON SIGNUP.EVENT_ID = EVENT.ID " +
                                                                 "LEFT JOIN TIMEFRAME ON EVENT.TIMEFRAME_ID = TIMEFRAME.ID " +
                                                                 "WHERE STUDENT.CLASS_YEAR = ? AND STUDENT.SIGN = ? " +
                                                                 "GROUP BY STUDENT.ID, STUDENT.NAME, STUDENT.EMAIL, STUDENT.CLASS_YEAR, STUDENT.SIGN, STUDENT.PASSKEY " +
                                                                 "ORDER BY STUDENT.NAME";

    private static final String INSERT_NEW_SQL = "INSERT INTO STUDENT (NAME, EMAIL, CLASS_YEAR, SIGN, PASSKEY) VALUES (?, ?, ?, ?, ?)";

    private final static Random random = new Random();

    public Optional<Student> getStudentByEmail(String email) {
        if (email == null || email.length() == 0) {
            return Optional.empty();
        }

        return getSingleFromSQL(SELECT_BY_EMAIL_SQL, ps -> ps.setString(1, email));
    }

    public Optional<Student> getStudentById(int id) {
        return getSingleFromSQL(SELECT_BY_ID_SQL, ps -> ps.setInt(1, id));
    }

    public Collection<Student> getStudentsByClass(Class aClass) {
        return getMultipleFromSQL(SELECT_BY_CLASS, ps -> {
            ps.setShort(1, aClass.year.value);
            ps.setString(2, aClass.sign.name());
        });
    }

    public Collection<Student> getEventSignups(int eventID) {
        return getMultipleFromSQL(SELECT_BY_EVENT_ID, ps -> ps.setInt(1, eventID));
    }

    public boolean deleteStudent(int id) {
        return runUpdate(DELETE_SQL, ps -> ps.setInt(1, id));
    }

    public boolean deleteAll() {
        return runUpdate(DELETE_ALL_SQL, p -> {});
    }

    public Collection<Pair<Student, Integer>> getClassWithTfNumber(Class aClass) {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            return null;
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {

            preparedStatement = conn.prepareStatement(SELECT_BY_CLASS_WITH_TF_NUMBER);
            preparedStatement.setShort(1, aClass.year.value);
            preparedStatement.setString(2, aClass.sign.name());

            resultSet = preparedStatement.executeQuery();
            Collection<Pair<Student, Integer>> ret = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                ret.add(new Pair<>(fromSingleRow(resultSet), resultSet.getInt(7)));
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

    private static String generatePass() {
        char[] pass = new char[6];
        for (int i = 0; i < pass.length; i++) {
            for (int j = 0; j < 6; j++) {
                int r = random.nextInt(36);
                pass[i] = (r < 10) ? (char)('0' + r) : (char)('A' + (r - 10));
            }
        }
        return new String(pass);
    }

    public boolean saveFromStream(final InputStream is) {

        final BlockingQueue<Optional<Student>> queue = new ArrayBlockingQueue<>(20);
        new Thread(() -> {
            try {

                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] attrs = line.split(String.valueOf(Context.SEPARATOR));
                        if (attrs.length < 4) {
                            break;
                        }

                        String name = attrs[0];
                        String email = attrs[1];
                        String year = attrs[2];
                        String sign = attrs[3];

                        if (name != null && (name = name.trim()).length() > 0
                            && email != null && (email = email.trim()).length() > 0
                            && year != null && (year = year.trim()).length() > 0
                            && sign != null && (sign = sign.trim()).length() == 1) {
                            queue.put(Optional.of(new Student(name, email, Class.of(Integer.parseInt(year), sign), generatePass())));
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                queue.add(Optional.empty());
            }
        }).start();

        Optional<Student> os;
        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement(INSERT_NEW_SQL);
            while ((os = queue.take()).isPresent()) {
                Student s = os.get();

                preparedStatement.setString(1, s.getName());
                preparedStatement.setString(2, s.getEmail());

                preparedStatement.setShort(3, s.getaClass().year.value);
                preparedStatement.setString(4, s.getaClass().sign.name());

                preparedStatement.setString(5, s.getCode());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            System.out.println("Exception while inserting students - possibly duplicate email");
        } finally {
            close(preparedStatement);
            close(conn);
        }

        return false;
    }

    public boolean saveStudent(String name, String email, Class aClass) {
        return runUpdate(INSERT_NEW_SQL, ps -> {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setShort(3, aClass.year.value);
            ps.setString(4, aClass.sign.name());
            ps.setString(5, generatePass());
        });
    }

    @Override
    public String selectAllQuery() {
        return SELECT_ALL_SQL;
    }

    @Override
    public Student fromSingleRow(ResultSet resultSet) throws SQLException {
        return new Student(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), Class.of(resultSet.getShort(4), resultSet.getString(5)), resultSet.getString(6));
    }

}