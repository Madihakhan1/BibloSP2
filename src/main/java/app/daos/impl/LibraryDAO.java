package app.daos.impl;

import app.daos.IDAO;
import app.dtos.LibraryDTO;
import app.entities.Liberary;
import app.exceptions.ApiException;
import app.security.daos.SecurityPopulatorDAO;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class LibraryDAO implements IDAO<Liberary, Integer> {

    private static LibraryDAO instance;
    private static EntityManagerFactory emf;

    public static LibraryDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new LibraryDAO();
        }
        return instance;
    }

    @Override
    public LibraryDTO read(Integer integer) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            Liberary liberary = em.find(Todo.class, integer);
            if (todo == null) {
                throw new ApiException(404, "Todo not found");
            }
            return new TodoDTO(todo);
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during read");
        }
    }

    @Override
    public List<TodoDTO> readAll() throws ApiException {
        try(EntityManager em = emf.createEntityManager()) {
            TypedQuery<TodoDTO> query = em.createQuery("SELECT new dat.dtos.TodoDTO(t) FROM Todo t", TodoDTO.class);
            return query.getResultList();
        } catch(Exception e) {
            throw new ApiException(400, "Something went wrong during readAll");
        }
    }

    public List<TodoDTO> readAllFromUser(UserDTO user) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<TodoDTO> query = em.createQuery("SELECT new dat.dtos.TodoDTO(t) FROM Todo t WHERE t.user.username = :username", TodoDTO.class);
            query.setParameter("username", user.getUsername());
            return query.getResultList();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during readAllFromUser");
        }
    }

    @Override
    public TodoDTO create(TodoDTO todoDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Todo todo = new Todo(todoDTO);
            em.persist(todo);
            em.getTransaction().commit();
            return new TodoDTO(todo);
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during create");
        }
    }

    @Override
    public TodoDTO update(Integer integer, TodoDTO todoDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Todo todo = em.find(Todo.class, integer);
            if (todo == null) {
                throw new ApiException(404, "Todo not found");
            }

            todo.setTitle(todoDTO.getTitle());
            todo.setDescription(todoDTO.getDescription());
            todo.setDone(todoDTO.isDone());
            em.getTransaction().commit();
            return new TodoDTO(todo);
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during update");
        }
    }

    public void delete(Integer integer, UserDTO user) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Todo todo = em.find(Todo.class, integer);
            if (todo == null) {
                throw new ApiException(404, "Todo not found");
            }
            if (todo.getUser().getUsername().equals(user.getUsername())) {
                todo.removeUser();
                em.remove(todo);
                em.getTransaction().commit();
            } else {
                throw new ApiException(403, "You are not allowed to delete this todo");
            }
            todo.removeUser();
            em.remove(todo);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during delete");

        }
    }

    public TodoDTO[] populate() throws ApiException {
        UserDTO[] users = SecurityPopulatorDAO.populateUsers(emf);
        UserDTO userDTO = users[0];
        UserDTO adminDTO = users[1];
        TodoDTO t1 = new TodoDTO(null, "Buy milk", "Buy milk at the store", false, userDTO);
        TodoDTO t2 = new TodoDTO(null, "Buy bread", "Buy bread at the store", false, userDTO);
        TodoDTO t3 = new TodoDTO(null, "Buy cheese", "Buy cheese at the store", false, adminDTO);
        create(t1);
        create(t2);
        create(t3);
        return new TodoDTO[] {t1, t2, t3};
    }

    public List<UserDTO> readAllUsers() throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<UserDTO> query = em.createQuery("SELECT new dk.bugelhartmann.UserDTO(u.username, u.password) FROM User u", UserDTO.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new ApiException(400, "Something went wrong during readAllFromUser");
        }
    }


}

