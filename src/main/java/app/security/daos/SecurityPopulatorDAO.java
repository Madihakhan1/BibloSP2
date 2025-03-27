package app.security.daos;

import app.config.HibernateConfig;
import app.security.entities.Role;
import app.security.entities.User;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManagerFactory;

public class SecurityPopulatorDAO {

    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static UserDTO[] populateUsers(EntityManagerFactory emf) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Roller
            Role userRole = em.find(Role.class, "USER");
            if (userRole == null) {
                userRole = new Role("USER");
                em.persist(userRole);
            }

            Role adminRole = em.find(Role.class, "ADMIN");
            if (adminRole == null) {
                adminRole = new Role("ADMIN");
                em.persist(adminRole);
            }

            // Brugere
            User reader1 = em.find(User.class, "reader1");
            if (reader1 == null) {
                reader1 = new User("reader1", "user123");
                reader1.addRole(userRole);
                em.persist(reader1);
            }

            User reader2 = em.find(User.class, "reader2");
            if (reader2 == null) {
                reader2 = new User("reader2", "user124");
                reader2.addRole(userRole);
                em.persist(reader2);
            }

            User admin = em.find(User.class, "admintest");
            if (admin == null) {
                admin = new User("admintest", "admin123");
                admin.addRole(adminRole);
                em.persist(admin);
            }

            em.getTransaction().commit();

            return new UserDTO[]{
                    new UserDTO(reader1.getUsername(), "user123"),
                    new UserDTO(reader2.getUsername(), "user124"),
                    new UserDTO(admin.getUsername(), "admin123")
            };
        }
    }


}
