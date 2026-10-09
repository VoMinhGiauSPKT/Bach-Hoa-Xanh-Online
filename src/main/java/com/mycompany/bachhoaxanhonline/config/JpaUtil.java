package com.mycompany.bachhoaxanhonline.config;

import com.mycompany.bachhoaxanhonline.util.ConfigUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static volatile EntityManagerFactory emf;

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            try {
                Map<String, Object> overrides = new HashMap<>();

                String dbUrl = ConfigUtil.get("DB_URL");
                if (dbUrl != null && !dbUrl.isEmpty()) {
                    overrides.put("jakarta.persistence.jdbc.url", dbUrl);
                }

                String dbUser = ConfigUtil.get("DB_USER");
                if (dbUser != null && !dbUser.isEmpty()) {
                    overrides.put("jakarta.persistence.jdbc.user", dbUser);
                }

                String dbPassword = ConfigUtil.get("DB_PASSWORD");
                if (dbPassword != null && !dbPassword.isEmpty()) {
                    overrides.put("jakarta.persistence.jdbc.password", dbPassword);
                }

                emf = Persistence.createEntityManagerFactory("BachHoaXanhPU", overrides);
            } catch (Throwable ex) {
                System.err.println("Failed to initialize EntityManagerFactory: " + ex);
                ex.printStackTrace();
                throw new ExceptionInInitializerError(ex);
            }
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
