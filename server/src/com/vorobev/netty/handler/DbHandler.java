package com.vorobev.netty.handler;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DbHandler implements Runnable {
    private final String DB_URL = "jdbc:sqlite:network_storage.db";
    private Connection connection;
    private PreparedStatement findUserStatement;
    private PreparedStatement addUserStatement;
    private PreparedStatement getUserStatement;


    @Override
    public void run() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            log.debug("Connection to the database");
            findUserStatement = createFindUserStatement();
            addUserStatement = createAddUserStatement();
            getUserStatement = createGetUserStatement();

        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данныз");
            e.printStackTrace();
        }
    }

    public boolean findUserForAuth(String login, String password) throws SQLException {
        findUserStatement.setString(1, login);
        findUserStatement.setString(2, password);
        String nickname = null;
        ResultSet resultSet = findUserStatement.executeQuery();
        while (resultSet.next()) {
            nickname = resultSet.getString("login");
        }
        resultSet.close();
        return nickname != null;
    }

    public void addUser(String login, String password) {
        try {
            addUserStatement.setString(1, login);
            addUserStatement.setString(2, password);
            addUserStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Не удалось добавить пользователя");
            e.printStackTrace();
        }
    }

    private PreparedStatement createAddUserStatement() throws SQLException {
        return connection.prepareStatement("INSERT INTO users (login,password) values (?,?)");
    }

    private PreparedStatement createFindUserStatement() throws SQLException {
        return connection.prepareStatement("SELECT login FROM users WHERE login = ? AND password = ? ");
    }

    private PreparedStatement createGetUserStatement() throws SQLException {
        return connection.prepareStatement("SELECT login FROM users WHERE login = ?");
    }


    public boolean findUser(String regLogin) throws SQLException {
        String nickname = null;
        getUserStatement.setString(1, regLogin);
        ResultSet resultSet = getUserStatement.executeQuery();
        while (resultSet.next()) {
            nickname = resultSet.getString("login");
        }
        resultSet.close();
        return nickname != null;
    }
}
