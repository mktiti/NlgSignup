package hu.titi.nlg.repo;

import hu.titi.nlg.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

interface Repo<E> {

    String selectAllQuery();

    default Optional<E> getSingleFromSQL(String query, DBUtil.PrepStatementSetter setter) {
        Optional<E> ret = Optional.empty();

        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (conn == null) {
                return Optional.empty();
            }

            preparedStatement = conn.prepareStatement(query);
            setter.set(preparedStatement);
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();

            if (resultSet.next()) {
                ret =  Optional.of(fromSingleRow(resultSet));
            }

            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(conn);
        }

        return Optional.empty();
    }

    default Collection<E> getMultipleFromSQL(String query, DBUtil.PrepStatementSetter setter) {
        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (conn == null) {
                return null;
            }

            preparedStatement = conn.prepareStatement(query);
            setter.set(preparedStatement);
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();

            return fromResultSet(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(conn);
        }

        return null;
    }

    default boolean runUpdate(String update, DBUtil.PrepStatementSetter setter) {
        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;

        if (conn == null) {
            return false;
        }

        try {
            preparedStatement = conn.prepareStatement(update);
            setter.set(preparedStatement);
            preparedStatement.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println("Exception while runUpdate, possibly duplicate");
        } finally {
            close(preparedStatement);
            close(conn);
        }

        return false;
    }

    default Collection<E> getAll() {
        Connection conn = DBUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {

            if (conn == null) {
                return null;
            }

            preparedStatement = conn.prepareStatement(selectAllQuery());
            resultSet = preparedStatement.executeQuery();
            return fromResultSet(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(conn);
        }

        return null;
    }

    default Collection<E> fromResultSet(ResultSet resultSet) throws SQLException {
        Collection<E> ret = new ArrayList<>(resultSet.getFetchSize());

        while (resultSet.next()) {
            ret.add(fromSingleRow(resultSet));
        }

        return ret;
    }

    E fromSingleRow(ResultSet resultSet) throws SQLException;

    default void close(AutoCloseable closeable) {
        DBUtil.close(closeable);
    }

}
