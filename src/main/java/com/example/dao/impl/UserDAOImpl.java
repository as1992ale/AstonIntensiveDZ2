package com.example.dao.impl;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public Long create(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Long id = (Long) session.save(user);
            transaction.commit();
            logger.info("User created successfully with ID: {}", id);
            return id;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to find user by ID", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            logger.error("Error finding all users: {}", e.getMessage());
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    @Override
    public boolean update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
            logger.info("User with ID {} updated successfully", user.getId());
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                transaction.commit();
                logger.info("User with ID {} deleted successfully", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Error finding user by email {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to find user by email", e);
        }
    }
}