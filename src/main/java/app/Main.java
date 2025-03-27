package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.daos.impl.BookDAO;
import app.security.daos.SecurityPopulatorDAO;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        // Først: Populér brugere
        SecurityPopulatorDAO.populateUsers(emf);

        // Så: Populér bøger
        BookDAO bookDAO = BookDAO.getInstance(emf);
        try {
            bookDAO.populate();
        } catch (Exception e) {
            System.out.println(" Kunne ikke populere bøger: " + e.getMessage());
        }

        // Til sidst: Start server
        ApplicationConfig.startServer(7070);
    }
}
