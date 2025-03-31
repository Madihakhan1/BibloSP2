package app.controllers.impl;

import app.entities.Book;
import jakarta.persistence.EntityManagerFactory;

public class Populator {

    public static void populateBooks(EntityManagerFactory emf) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Book book1 = new Book("The Hobbit", "J.R.R. Tolkien");
            Book book2 = new Book("1984", "George Orwell");

            em.persist(book1);
            em.persist(book2);

            em.getTransaction().commit();
        }
    }
}
