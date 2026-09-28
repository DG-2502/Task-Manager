package repo;

import domain.User;

import java.util.List;

public interface UserRepo {
    User getById(int id);
    void add(User user);
    void update(User user);
    void delete(int id);
    List<User> get();

    User getByUserName(String userName);
}
