package app.infra;

import app.domain.Task;
import app.exception.DataAccessException;
import app.exception.TaskNotFoundException;
import org.springframework.stereotype.Repository;
import app.repo.TaskRepo;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepoPSQL implements TaskRepo {
    private final DataSource dataSource;

    public TaskRepoPSQL(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
        Task task = new Task(
                resultSet.getString("title"),
                resultSet.getObject("start_date", LocalDate.class),
                Task.State.valueOf(resultSet.getString("state")),
                resultSet.getInt("creator_id")
        );
        task.setId(resultSet.getInt("id"));
        return task;
    }

    @Override
    public Task getById(int id) {
        String sql = "Select * from tasks where id = ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapTask(resultSet);
                throw new TaskNotFoundException(id);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch task with id: " + id, e);
        }
    }

    @Override
    public void add(Task task) {
        String sql = "insert into tasks(title, start_date, state, creator_id) VALUES (?, ?, ?, ?) returning id";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, task.getTitle());
            statement.setDate(2, Date.valueOf(task.getStartDate()));
            statement.setString(3, task.getState().name());
            statement.setInt(4, task.getCreatorId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) task.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add task: " + task, e);
        }
    }

    @Override
    public void update(Task task) {
        String sql = "update tasks set title = ?, start_date = ?, state = ?, creator_id = ? where id = ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, task.getTitle());
            statement.setDate(2, Date.valueOf(task.getStartDate()));
            statement.setString(3, task.getState().name());
            statement.setInt(4, task.getCreatorId());
            statement.setInt(5, task.getId());
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new TaskNotFoundException(task.getId());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update task: " + task, e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "delete from tasks where id = ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete task with id: " + id, e);
        }
    }

    @Override
    public List<Task> getByUser(int userId) {
        String sql = "select * from tasks where creator_id = ? order by id";
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) tasks.add(mapTask(resultSet));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get tasks", e);
        }
    }

    @Override
    public List<Task> getByStateAndUser(Task.State state, int userId) {
        String sql = "select * from tasks where creator_id = ? and state = ? order by id";
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setString(2, state.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) tasks.add(mapTask(resultSet));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get tasks", e);
        }
    }

    @Override
    public void deleteByStateAndUser(Task.State state, int userId) {
        String sql = "delete from tasks where creator_id = ? and state = ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setString(2, state.name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete task with creator_id: " + userId + " and state: " + state.name(), e);
        }
    }

    @Override
    public void deleteByUserId(int userId) {
        String sql = "delete from tasks where creator_id = ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete task with creator_id: " + userId, e);
        }
    }
}
