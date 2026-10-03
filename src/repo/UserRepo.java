package repo;

import domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepo {
    User getById(int id);
    void add(User user);
    void update(User user);
    void delete(int id);
    List<User> get();
    boolean isEmpty();

    Optional<User> getByUserName(String userName);
}
