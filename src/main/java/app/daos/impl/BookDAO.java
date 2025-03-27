package app.daos.impl;

import app.daos.IDAO;
import app.dtos.BookDTO;
import app.dtos.BookListDTO;
import app.entities.Book;
import app.exceptions.ApiException;
import app.security.daos.SecurityPopulatorDAO;
import app.security.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BookDAO implements IDAO<BookDTO, Integer> {

    private static BookDAO instance;
    private static EntityManagerFactory emf;

    public static BookDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new BookDAO();
        }
        return instance;
    }

    @Override
    public BookDTO read(Integer id) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            Book book = em.find(Book.class, id);
            if (book == null) {
                throw new ApiException(404, "Book not found");
            }
            return new BookDTO(book);
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during read");
        }
    }

    @Override
    public List<BookDTO> readAll() throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<BookDTO> query = em.createQuery(
                    "SELECT new app.dtos.BookDTO(b) FROM Book b", BookDTO.class
            );
            return query.getResultList();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during readAll");
        }
    }

    public List<BookDTO> readAllFromUser(UserDTO user) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<BookDTO> query = em.createQuery(
                    "SELECT new app.dtos.BookDTO(b) FROM Book b WHERE b.user.username = :username",
                    BookDTO.class
            );
            query.setParameter("username", user.getUsername());
            return query.getResultList();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during readAllFromUser");
        }
    }

    @Override
    public BookDTO create(BookDTO bookDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Book book = new Book(bookDTO);

            // Find eksisterende bruger i databasen
            if (bookDTO.getUser() != null && bookDTO.getUser().getUsername() != null) {
                String username = bookDTO.getUser().getUsername();
                app.security.entities.User user = em.find(app.security.entities.User.class, username);
                if (user == null) {
                    throw new ApiException(404, "User not found: " + username);
                }
                book.setUser(user);
            }

            em.persist(book);
            em.getTransaction().commit();
            return new BookDTO(book);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(); // for fejlsøgning
            throw new ApiException(400, "Something went wrong during create");
        }
    }


    @Override
    public BookDTO update(Integer id, BookDTO bookDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Book book = em.find(Book.class, id);
            if (book == null) {
                throw new ApiException(404, "Book not found");
            }

            book.setTitle(bookDTO.getTitle());
            book.setAuthor(bookDTO.getAuthor());
            book.setGenre(bookDTO.getGenre());
            book.setAvailable(bookDTO.isAvailable());

            em.getTransaction().commit();
            return new BookDTO(book);
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during update");
        }
    }

    public void delete(Integer id, UserDTO user) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Book book = em.find(Book.class, id);
            if (book == null) {
                throw new ApiException(404, "Book not found");
            }
            // Kun admin må slette — evt. check rolle hvis nødvendigt

            book.removeUser();
            em.remove(book);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during delete");
        }
    }

    public BookDTO[] populate() throws ApiException {
        List<BookDTO> importedBooks;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = BookDAO.class.getClassLoader().getResourceAsStream("books.json");

            if (inputStream == null) {
                throw new ApiException(500, "books.json not found in resources");
            }

            BookListDTO bookList = objectMapper.readValue(inputStream, BookListDTO.class);
            importedBooks = bookList.getBooks();
        } catch (IOException e) {
            throw new ApiException(500, "Failed to read books from JSON file: " + e.getMessage());
        }

        List<BookDTO> createdBooks = new ArrayList<>();
        for (BookDTO bookDTO : importedBooks) {
            try (var em = emf.createEntityManager()) {
                em.getTransaction().begin();

                // Hent brugeren fra databasen, så den er managed
                var user = em.find(app.security.entities.User.class, bookDTO.getUser().getUsername());
                if (user == null) {
                    throw new ApiException(404, "User not found: " + bookDTO.getUser().getUsername());
                }

                // Opret og forbind bogen til brugeren
                Book book = new Book(bookDTO);
                book.setUser(user);

                em.persist(book);
                em.getTransaction().commit();

                createdBooks.add(new BookDTO(book));
            }
        }

        return createdBooks.toArray(new BookDTO[0]);
    }



    public void saveBooks(List<Book> books) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Book book : books) {
                em.persist(book);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

}
