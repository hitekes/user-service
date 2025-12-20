package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) throws DaoException {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            if (user.getCreatedAt() == null) {
                user.setCreatedAt(java.time.LocalDateTime.now());
            }

            session.save(user);

            session.flush();

            transaction.commit();

            logger.info("Пользователь сохранен успешно. ID: {}", user.getId());
            return user;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции", rollbackEx);
                }
            }

            logger.error("Ошибка при сохранении пользователя: {}", user.getEmail(), e);

            // Проверяем тип ошибки
            if (e instanceof PersistenceException) {
                Throwable cause = e.getCause();
                if (cause != null && cause.getMessage().contains("duplicate key")) {
                    throw new DaoException("Пользователь с email '" + user.getEmail() + "' уже существует", e);
                }
            }

            throw new DaoException("Ошибка при сохранении пользователя: " + e.getMessage(), e);

        } finally {
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Ошибка при закрытии сессии", closeEx);
                }
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DaoException {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по id: {}", id, e);
            throw new DaoException("Ошибка при поиске пользователя по id: " + id, e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<User> findAll() throws DaoException {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.createQuery("FROM User", User.class).list();

        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей", e);
            throw new DaoException("Ошибка при получении всех пользователей", e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public User update(User user) throws DaoException {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.update(user);
            session.flush();

            transaction.commit();

            logger.info("Пользователь обновлен. ID: {}", user.getId());
            return user;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            logger.error("Ошибка при обновлении пользователя. ID: {}", user.getId(), e);
            throw new DaoException("Ошибка при обновлении пользователя", e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void delete(Long id) throws DaoException {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                logger.info("Пользователь с id {} удален", id);
            } else {
                logger.warn("Попытка удаления несуществующего пользователя с id {}", id);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            logger.error("Ошибка при удалении пользователя с id: {}", id, e);
            throw new DaoException("Ошибка при удалении пользователя с id: " + id, e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email.trim().toLowerCase());

            User user = query.uniqueResult();
            return Optional.ofNullable(user);

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по email: {}", email, e);
            throw new DaoException("Ошибка при поиске пользователя по email: " + email, e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}