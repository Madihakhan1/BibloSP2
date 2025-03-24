package app.controllers.impl;

import app.config.HibernateConfig;
import app.controllers.IController;
import app.daos.impl.LibraryDAO;
import app.dtos.LibraryDTO;
import app.exceptions.ApiException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.List;

public class LibraryController implements IController<LibraryDTO, Integer> {
    private final LibraryDAO dao;

    public LibraryController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = LibraryDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            LibraryDTO libraryDTO = dao.read(id);
            ctx.res().setStatus(200);
            ctx.json(libraryDTO, LibraryDTO.class);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing or invalid ID parameter");
        }
    }

    @Override
    public void readAll(Context ctx) throws ApiException {
        List<LibraryDTO> libraryDTOS = dao.readAll();
        ctx.res().setStatus(200);
        ctx.json(libraryDTOS, LibraryDTO.class);
    }

    public void readAllFromUser(Context ctx) throws ApiException {
        UserDTO user = ctx.attribute("user");
        List<LibraryDTO> libraryDTOS = dao.readAllFromUser(user);
        ctx.res().setStatus(200);
        ctx.json(libraryDTOS, LibraryDTO.class);
    }

    @Override
    public void create(Context ctx) throws ApiException {
        LibraryDTO libraryDTO = ctx.bodyAsClass(LibraryDTO.class);
        UserDTO user = ctx.attribute("user");
        libraryDTO.setUser(user);
        libraryDTO = dao.create(libraryDTO);
        ctx.res().setStatus(201);
        ctx.json(libraryDTO, LibraryDTO.class);
    }

    @Override
    public void update(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            LibraryDTO updatedDTO = ctx.bodyAsClass(LibraryDTO.class);
            LibraryDTO resultDTO = dao.update(id, updatedDTO);
            ctx.res().setStatus(200);
            ctx.json(resultDTO, LibraryDTO.class);
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
            LibraryDTO[] libraryDTOS = dao.populate();
            ctx.res().setStatus(200);
            ctx.json("{ \"message\": \"Database has been populated with books\" }");
        } catch (PersistenceException e) {
            throw new ApiException(400, "Populator failed");
        }
    }
}
