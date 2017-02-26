package hu.titi.nlg.repo;

import hu.titi.nlg.DBUtil;

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

        try (Connection conn = DBUtil.getConnection()) {
            if (conn == null) {
                return null;
            }

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            setter.set(preparedStatement);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();

            if (resultSet.next()) {
                ret =  Optional.of(fromSingleRow(resultSet));
            }

            resultSet.close();
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    default Collection<E> getMultipleFromSQL(String query, DBUtil.PrepStatementSetter setter) {
        try (Connection conn = DBUtil.getConnection()) {
            if (conn == null) {
                return null;
            }

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            setter.set(preparedStatement);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();

            return fromResultSet(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    default Collection<E> getAll() {
        try (Connection conn = DBUtil.getConnection()) {

            if (conn == null) {
                return null;
            }

            ResultSet result = conn.prepareStatement(selectAllQuery()).executeQuery();
            return fromResultSet(result);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    default Collection<E> fromResultSet(ResultSet resultSet) throws SQLException {
        Collection<E> ret = new ArrayList<>(resultSet.getFetchSize());

        while (resultSet.next()) {
            ret.add(fromSingleRow(resultSet));
        }

        resultSet.close();

        return ret;
    }

    E fromSingleRow(ResultSet resultSet) throws SQLException;

}
