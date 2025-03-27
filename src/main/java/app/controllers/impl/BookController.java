package app.controllers.impl;

import app.config.HibernateConfig;
import app.controllers.IController;
import app.daos.impl.BookDAO;
import app.dtos.BookDTO;
import app.entities.Book;
import app.dtos.BookListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookController implements IController<BookDTO, Integer> {

    private static final List<BookDTO> books = new ArrayList<>();

    static {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = BookController.class.getClassLoader().getResourceAsStream("books.json");
            BookListDTO bookList = objectMapper.readValue(inputStream, BookListDTO.class);
            books.addAll(bookList.getBooks());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load books from JSON file.");
        }
    }


    @Override
    public void read(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        BookDTO book = books.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);

        if (book == null) {
            ctx.status(404).result("Book not found");
        } else {
            ctx.status(200).json(book);
        }
    }

    @Override
    public void readAll(Context ctx) {
        ctx.status(200).json(books);
    }

    public void readAllFromUser(Context ctx) {
        UserDTO user = ctx.attribute("user");

        List<BookDTO> userBooks = books.stream()
                .filter(b -> b.getUser() != null && b.getUser().getUsername().equals(user.getUsername()))
                .collect(Collectors.toList());

        ctx.status(200).json(userBooks);
    }

    @Override
    public void create(Context ctx) {
        BookDTO newBook = ctx.bodyAsClass(BookDTO.class);
        books.add(newBook);
        ctx.status(201).json(newBook);
    }

    @Override
    public void update(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        BookDTO updatedBook = ctx.bodyAsClass(BookDTO.class);

        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == id) {
                books.set(i, updatedBook);
                ctx.status(200).json(updatedBook);
                return;
            }
        }
        ctx.status(404).result("Book not found");
    }

    @Override
    public void delete(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean removed = books.removeIf(b -> b.getId() == id);

        if (removed) {
            ctx.status(204);
        } else {
            ctx.status(404).result("Book not found");
        }
    }

    public void saveBooksToDatabase(EntityManagerFactory emf) {
        List<Book> booksToSave = books.stream()
                .map(BookDTO::getAsEntity)
                .collect(Collectors.toList());

        BookDAO bookDAO = BookDAO.getInstance(emf);
        bookDAO.saveBooks(booksToSave);
    }


    public void populate(Context ctx) {
        try {
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            saveBooksToDatabase(emf);
            ctx.status(200).json("{\"message\": \"Books from JSON saved to database\"}");
        } catch (Exception e) {
            ctx.status(500).json("{\"error\": \"Failed to populate books to database\"}");
        }
    }

}
