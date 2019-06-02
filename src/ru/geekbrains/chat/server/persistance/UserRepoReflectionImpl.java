package ru.geekbrains.chat.server.persistance;

import ru.geekbrains.chat.server.User;
import ru.geekbrains.chat.server.persistance.annotation.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepoReflectionImpl implements UserRepository<Integer, User> {

    private final Connection connection;
    private static String findUserByLoginRequest;
    private static String addUserRequest;

    public UserRepoReflectionImpl(Connection connection, Class userClass) {
        this.connection = connection;
        createTable(userClass);
    }

    @Override
    public User findByLogin(String login) {
        User user = null;
        try (PreparedStatement statement = connection.prepareStatement(findUserByLoginRequest)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = new User(resultSet.getString(1), resultSet.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User findById(Integer id) {
        // TODO
        return null;
    }

    @Override
    public List<User> findAll() {
        // TODO
        return null;
    }

    @Override
    public void insert(User user) {
        try (PreparedStatement statement = connection.prepareStatement(addUserRequest)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPassword());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(User entity) {
        //TODO
    }

    @Override
    public void delete(Integer id) {
        //TODO
    }

    private void createTable(Class clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("Annotation @Table is missing");
        }
        Map<Class, String> valuesConverter = getValueConverter();

        StringBuilder request = new StringBuilder("CREATE TABLE IF NOT EXISTS ");

        String tableName = ((Table) clazz.getAnnotation(Table.class)).name(); //
        tableName = tableName.isEmpty() ? clazz.getSimpleName() : tableName;
        request.append(tableName).append(" (");
        String loginFieldName = "";
        String passFieldName = "";

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            if (field.isAnnotationPresent(Column.class)) {
                String colName = field.getAnnotation(Column.class).name();
                colName = colName.isEmpty() ? field.getName() : colName;
                request.append(colName)
                        .append(" ")
                        .append(valuesConverter.get(field.getType()));

                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    request.append(" PRIMARY KEY")
                            .append(field.getAnnotation(PrimaryKey.class).autoincrement() ? " AUTOINCREMENT" : "");
                }

                request.append(field.isAnnotationPresent(Unique.class) ? " UNIQUE" : "").append(", ");

                if (field.isAnnotationPresent(Login.class)) {
                    loginFieldName = colName;
                } else if (field.isAnnotationPresent(Password.class)) {
                    passFieldName = colName;
                }
            }
        }
        request.setLength(request.length() - 2);
        request.append(");");

        if (loginFieldName.isEmpty() || passFieldName.isEmpty()) {
            throw new RuntimeException("@Login or @Password annotation is missing");
        }

        initPreparedRequests(tableName, loginFieldName, passFieldName);

        try (Statement statement = connection.createStatement()) {
            statement.execute(request.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private Map<Class, String> getValueConverter() {
        Map<Class, String> valuesConverter = new HashMap<>();
        valuesConverter.put(int.class, "INTEGER");
        valuesConverter.put(Integer.class, "INTEGER");
        valuesConverter.put(String.class, "VARCHAR(255)");
        return valuesConverter;
    }

    private void initPreparedRequests(String tableName, String loginFieldName, String passFieldName) {
        findUserByLoginRequest = String.format(
                "SELECT %s, %s FROM %s WHERE %s = ?;"
                , loginFieldName
                , passFieldName
                , tableName
                , loginFieldName
        );

        addUserRequest = String.format(
                "INSERT into %s (%s, %s) values (?, ?);"
                , tableName
                , loginFieldName
                , passFieldName
        );
    }

}
