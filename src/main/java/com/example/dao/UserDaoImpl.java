package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.HibernateUtil;
import jakarta.persistence.criteria.CriteriaQuery;
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
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);  // Изменили save() на persist() для Hibernate 6
            transaction.commit();

            logger.debug("Пользователь сохранен с ID: {}", user.getId());
            return user;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка сохранения пользователя с email: {}", user.getEmail(), e);
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
            // Для Hibernate 6
            CriteriaQuery<User> criteriaQuery = session.getCriteriaBuilder().createQuery(User.class);
            criteriaQuery.from(User.class);
            return session.createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения всех пользователей", e);
            throw new DaoException("Ошибка получения всех пользователей", e);
        }
    }

    @Override
    public User update(User user) throws DaoException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User updatedUser = session.merge(user);
            transaction.commit();

            logger.debug("Данные пользователя обновлены: {}", updatedUser);
            return updatedUser;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка обновления пользователя с ID: {}", user.getId(), e);
            throw new DaoException("Ошибка обновления пользователя", e);
        }
    }

    @Override
    public void delete(Long id) throws DaoException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.debug("Удален пользователь по ID: {}", id);
            } else {
                logger.debug("Не найден пользователь с ID: {}", id);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка удаления пользователя по ID: {}", id, e);
            throw new DaoException("Ошибка удаления пользователя по ID: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class);
            query.setParameter("email", email.trim().toLowerCase());

            User user = query.uniqueResult();
            return Optional.ofNullable(user);

        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по email: {}", email, e);
            throw new DaoException("Ошибка поиска пользователя по email: " + email, e);
        }
    }
}