package service;

import domain.User;
import exception.UsernameNotFoundException;
import repo.UserRepo;


public class UserService {
    UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User login(String username) throws UsernameNotFoundException {
        return userRepo.getByUserName(username);
    }
}
