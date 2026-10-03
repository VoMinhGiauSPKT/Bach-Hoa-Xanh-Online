package com.mycompany.bachhoaxanhonline.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println(">>> BachHoaXanh WebApp is starting up...");
        try {
            // Warm up EntityManagerFactory and HikariCP connection pool
            JpaUtil.getEntityManagerFactory();
            System.out.println(">>> JPA & HikariCP initialized successfully.");
        } catch (Exception e) {
            System.err.println(">>> Error initializing JPA at startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println(">>> BachHoaXanh WebApp is shutting down...");
        JpaUtil.close();
        System.out.println(">>> JPA & HikariCP closed successfully.");
    }
}
