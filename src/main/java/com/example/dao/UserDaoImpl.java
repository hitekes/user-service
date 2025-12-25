package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.save(user);
            transaction.commit();

            logger.debug("Пользователь сохранен с ID: {}", user.getId());
            return user;

        } catch (Exception e) {
            logger.error("Ошибка сохранения почты: {}", user.getEmail(), e);
            throw new DaoException("Ошибка сохранения пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);

        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по ID: {}", id, e);
            throw new DaoException("Ошибка поиска пользователя по ID: " + id, e);
        }
    }

    @Override
    public List<User> findAll() throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();

        } catch (Exception e) {
            logger.error("Ошибка ", e);
            throw new DaoException("Ошибка ", e);
        }
    }

    @Override
    public User update(User user) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.update(user);
            transaction.commit();

            // Проверяем, что пользователь действительно обновлен
            User updated = session.get(User.class, user.getId());
            logger.debug("Данные пользователя обновлены : {}", updated);
            return updated;

        } catch (Exception e) {
            logger.error("Ошибка обновления ID: {}", user.getId(), e);
            throw new DaoException("Ошибка обновления пользователя ", e);
        }
    }

    @Override
    public void delete(Long id) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Query<?> query = session.createQuery("DELETE FROM User WHERE id = :id");
            query.setParameter("id", id);
            int deletedCount = query.executeUpdate();

            transaction.commit();

            if (deletedCount > 0) {
                logger.debug("Удален пользователь по ID: {}", id);
            } else {
                logger.debug("Не найден с ID: {} ", id);
            }

        } catch (Exception e) {
            logger.error("Ошибка удаления по ID: {}", id, e);
            throw new DaoException("Ошибка удаления по ID: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class); // Исправить
            query.setParameter("email", email.trim().toLowerCase());

            User user = query.uniqueResult();
            return Optional.ofNullable(user);

        } catch (Exception e) {
            logger.error("Ошибка поиска по email: {}", email, e);
            throw new DaoException("Ошибка поиска по email: " + email, e);
        }
    }
}