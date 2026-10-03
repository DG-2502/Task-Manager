package infra;

import domain.User;
import exception.UserNotFoundException;
import repo.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoArr implements UserRepo {
    protected int ID = -1;
    protected ArrayList<User> users = new ArrayList<>();

    private User copy(User user) {
        User copy = new User(user.getUsername(), user.isAdmin(), user.getPasswordHash());
        copy.setID(user.getID());
        return copy;
    }

    @Override
    public User getById(int id) {
        for (User user : users) {
            if (user.getID() == id) return copy(user);
        }
        throw new UserNotFoundException(id);
    }

    @Override
    public void add(User user) {
        user.setID(++ID);
        users.add(user);
    }

    @Override
    public void update(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getID() == updatedUser.getID()) {
                users.set(i, updatedUser);
                return;
            }
        }
        throw new UserNotFoundException(updatedUser.getID());
    }

    @Override
    public void delete(int id) {
        users.removeIf(user -> user.getID() == id);
    }

    @Override
    public List<User> get() {
        return users.stream().map(this::copy).toList();
    }

    @Override
    public boolean isEmpty() {
        return users.isEmpty();
    }

    @Override
    public Optional<User> getByUserName(String username) {
        return users.stream().filter(user -> user.getUsername().equals(username)).findAny().map(this::copy);
    }
}
