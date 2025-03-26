package app.controllers.impl;

import app.config.HibernateConfig;
import app.controllers.IController;
import app.daos.impl.BookDAO;
import app.dtos.BookDTO;
import app.exceptions.ApiException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.List;

public class BookController implements IController<BookDTO, Integer> {
    private final BookDAO dao;

    public BookController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = BookDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            BookDTO bookDTO = dao.read(id);
            ctx.res().setStatus(200);
            ctx.json(bookDTO, BookDTO.class);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing or invalid ID parameter");
        }
    }

    @Override
    public void readAll(Context ctx) throws ApiException {
        List<BookDTO> bookDTOS = dao.readAll();
        ctx.res().setStatus(200);
        ctx.json(bookDTOS, BookDTO.class);
    }

    public void readAllFromUser(Context ctx) throws ApiException {
        UserDTO user = ctx.attribute("user");
        List<BookDTO> bookDTOS = dao.readAllFromUser(user);
        ctx.res().setStatus(200);
        ctx.json(bookDTOS, BookDTO.class);
    }

    @Override
    public void create(Context ctx) throws ApiException {
        BookDTO bookDTO = ctx.bodyAsClass(BookDTO.class);
        UserDTO user = ctx.attribute("user");
        bookDTO.setUser(user);
        bookDTO = dao.create(bookDTO);
        ctx.res().setStatus(201);
        ctx.json(bookDTO, BookDTO.class);
    }

    @Override
    public void update(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            BookDTO updatedDTO = ctx.bodyAsClass(BookDTO.class);
            BookDTO resultDTO = dao.update(id, updatedDTO);
            ctx.res().setStatus(200);
            ctx.json(resultDTO, BookDTO.class);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing or invalid ID parameter");
        }
    }

    @Override
    public void delete(Context ctx) throws ApiException {
        try {
            UserDTO userDTO = ctx.attribute("user");
            int id = Integer.parseInt(ctx.pathParam("id"));
            dao.delete(id, userDTO);
            ctx.res().setStatus(204);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing or invalid ID parameter");
        }
    }

    public void populate(Context ctx) throws ApiException {
        try {
            BookDTO[] bookDTOS = dao.populate();
            ctx.res().setStatus(200);
            ctx.json("{ \"message\": \"Database has been populated with books\" }");
        } catch (PersistenceException e) {
            throw new ApiException(400, "Populator failed");
        }
    }
}
