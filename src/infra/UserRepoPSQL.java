package infra;

import domain.User;
import exception.DataAccessException;
import exception.UserNotFoundException;
import repo.UserRepo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoPSQL implements UserRepo {
    private final DataSource dataSource;

    public UserRepoPSQL(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User(
                resultSet.getString("username"),
                User.Status.valueOf(resultSet.getString("status")),
                resultSet.getString("password_hash"));
        user.setID(resultSet.getInt("id"));
        return user;
    }

    @Override
    public User getById(int id) {
        String sql = "Select * from users where id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapUser(resultSet);
                throw new UserNotFoundException(id);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user with id: " + id, e);
        }
    }

    @Override
    public void add(User user) {
        String sql = "insert into users(username, status, password_hash) VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getStatus().name());
            statement.setString(3, user.getPasswordHash());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) user.setID(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add user: " + user, e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "update users set username = ?, status = ?, password_hash = ? where id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getStatus().name());
            statement.setString(3, user.getPasswordHash());
            statement.setInt(4, user.getID());
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new UserNotFoundException(user.getID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user: " + user, e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "delete from users where id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user with id: " + id, e);
        }
    }

    @Override
    public List<User> get() {
        String sql = "select * from users order by id";
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) users.add(mapUser(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get users", e);
        }
    }

    @Override
    public boolean isEmpty() {
        String sql = "select exists(select 1 from users)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return !resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check whether users is empty", e);
        }
    }

    @Override
    public Optional<User> getByUserName(String userName) {
        String sql = "select * from users where username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user by username: " + userName, e);
        }
    }
}
