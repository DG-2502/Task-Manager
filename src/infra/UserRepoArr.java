package infra;

import domain.User;
import exception.UserNotFoundException;
import exception.UsernameNotFoundException;
import repo.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoArr implements UserRepo {
    protected int ID = -1;
    protected ArrayList<User> users = new ArrayList<>();

    @Override
    public User getById(int id) {
        for (User user : users) {
            if (user.getID() == id) {
                return user;
            }
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
        User existingUser = getById(updatedUser.getID());

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setAdmin(updatedUser.isAdmin());
    }

    @Override
    public void delete(int id) {
        User user = getById(id);

        users.remove(user);
    }

    @Override
    public List<User> get() {
        return users;
    }

    @Override
    public User getByUserName(String username) {
        Optional<User> optionalUser = users.stream().filter(user -> user.getUsername().equals(username)).findAny();
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new UsernameNotFoundException(username);
    }
}
