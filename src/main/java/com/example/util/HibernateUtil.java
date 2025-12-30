package com.example.util;

import com.example.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            configuration.addAnnotatedClass(User.class);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            Metadata metadata = new MetadataSources(serviceRegistry)
                    .addAnnotatedClass(User.class)
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();

            logger.info("Hibernate 6 SessionFactory created successfully for Java 25");

        } catch (Exception e) {
            logger.error("Failed to create Hibernate 6 SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed");
        }
    }

    public static void setSessionFactory(SessionFactory sessionFactory) {
        HibernateUtil.sessionFactory = sessionFactory;
    }
}