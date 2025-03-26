package app.daos.impl;

import app.daos.IDAO;
import app.dtos.BookDTO;
import app.entities.Book;
import app.exceptions.ApiException;
import app.security.daos.SecurityPopulatorDAO;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

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
            em.persist(book);
            em.getTransaction().commit();
            return new BookDTO(book);
        } catch (Exception e) {
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
        UserDTO[] users = SecurityPopulatorDAO.populateUsers(emf);
        UserDTO userDTO = users[0];
        UserDTO adminDTO = users[1];

        BookDTO b1 = new BookDTO(null, "The Hobbit", "J.R.R. Tolkien", "Fantasy", true, userDTO);
        BookDTO b2 = new BookDTO(null, "1984", "George Orwell", "Dystopia", true, userDTO);
        BookDTO b3 = new BookDTO(null, "The Alchemist", "Paulo Coelho", "Philosophy", true, adminDTO);

        create(b1);
        create(b2);
        create(b3);

        return new BookDTO[]{b1, b2, b3};
    }

    public List<UserDTO> readAllUsers() throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<UserDTO> query = em.createQuery(
                    "SELECT new dk.bugelhartmann.UserDTO(u.username, u.password) FROM User u",
                    UserDTO.class
            );
            return query.getResultList();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during readAllUsers");
        }
    }
}
