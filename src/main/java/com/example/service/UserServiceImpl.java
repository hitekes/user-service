package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserServiceImpl() {
        this.userDao = new com.example.dao.UserDaoImpl();
    }

    @Override
    public User createUser(String name, String email, Integer age) {
        try {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .age(age)
                    .build();
            return userDao.save(user);
        } catch (DaoException e) {
            log.error("Ошибка создания пользователя: {}", email, e);
            throw new RuntimeException("Не удалось создать пользователя", e);
        }
    }

    @Override
    public Optional<User> getUserById(Long id) {
        try {
            return userDao.findById(id);
        } catch (DaoException e) {
            log.error("Ошибка получения пользователя по ID: {}", id, e);
            throw new RuntimeException("Не удалось получить пользователя", e);
        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        try {
            return userDao.findByEmail(email);
        } catch (DaoException e) {
            log.error("Ошибка получения пользователя по email: {}", email, e);
            throw new RuntimeException("Не удалось получить пользователя", e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            return userDao.findAll();
        } catch (DaoException e) {
            log.error("Ошибка получения всех пользователей", e);
            throw new RuntimeException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) {
        try {
            User user = getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + id));

            if (name != null) user.setName(name);
            if (email != null) user.setEmail(email);
            if (age != null) user.setAge(age);

            return userDao.update(user);
        } catch (DaoException e) {
            log.error("Ошибка обновления пользователя с ID: {}", id, e);
            throw new RuntimeException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public void deleteUser(Long id) {
        try {
            userDao.delete(id);
        } catch (DaoException e) {
            log.error("Ошибка удаления пользователя с ID: {}", id, e);
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }
}