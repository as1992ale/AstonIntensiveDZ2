package com.example;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class TestHibernateUtil {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory(String url, String username, String password) {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistry standardRegistry =
                        new StandardServiceRegistryBuilder()
                                .configure("hibernate-test.cfg.xml")
                                .applySetting("hibernate.connection.url", url)
                                .applySetting("hibernate.connection.username", username)
                                .applySetting("hibernate.connection.password", password)
                                .build();

                Metadata metadata = new MetadataSources(standardRegistry)
                        .getMetadataBuilder()
                        .build();

                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}