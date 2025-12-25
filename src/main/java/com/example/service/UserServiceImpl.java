package com.example.service;

import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDao userDao = new UserDaoImpl();

    @Override
    public User createUser(String name, String email, Integer age) {
        User user = new User(name, email, age);
        return userDao.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) {
        User user = getUserById(id)
                .orElseThrow(() -> new DaoException("User not found with id: " + id));

        if (name != null) user.setName(name);
        if (email != null) user.setEmail(email);
        if (age != null) user.setAge(age);

        return userDao.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDao.delete(id);
    }
}