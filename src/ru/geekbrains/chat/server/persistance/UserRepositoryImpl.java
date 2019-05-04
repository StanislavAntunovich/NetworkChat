package ru.geekbrains.chat.server.persistance;

import ru.geekbrains.chat.server.User;

import java.sql.*;
import java.util.List;

public class UserRepositoryImpl implements UserRepository<User, String> {
    private static final String findUserByIdRequest = "SELECT login, password FROM users WHERE login = ?;";
    public static final String addUserRequest = "INSERT into users(login, password) values (?, ?);";

    private final Connection connection;

    public UserRepositoryImpl(Connection connection) {
        this.connection = connection;
        initDB();
    }

    @Override
    public User findUserByLogin(String login) {
        User user = null;
        try {
            PreparedStatement statement = connection.prepareStatement(findUserByIdRequest);
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            user = new User(resultSet.getString(1), resultSet.getString(2));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public void addUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(addUserRequest);
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPassword());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers() {
        //TODO
        return null;
    }

    private void initDB() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE table users(" +
                    "id integer primary key autoincrement," +
                    "login varchar(50) unique," +
                    "password varchar(50)" +
                    ");");
            statement.execute("insert into users(login, password) values " +
                    "('Master', 'ADDQD'), " +
                    "('Margarita', 'qwerty001'), " +
                    "('Begemot', 'qwerty003')");
            statement.close();
        } catch (SQLException e) {
            System.out.println("база данных уже есть");
        }

    }
}
