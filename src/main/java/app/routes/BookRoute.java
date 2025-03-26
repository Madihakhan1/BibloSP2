package app.routes;

import app.controllers.impl.BookController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class BookRoute {
    private final BookController bookController = new BookController();

    protected EndpointGroup getRoutes() {
        return () -> {
            put("/{id}", bookController::update, Role.ADMIN);
            post("/populate", bookController::populate, Role.ADMIN);
            post("/", bookController::create, Role.ADMIN);
            get("/", bookController::readAll, Role.ANYONE);
            get("/mine", bookController::readAllFromUser, Role.USER);
            get("/{id}", bookController::read, Role.USER);
            delete("/{id}", bookController::delete, Role.ADMIN);
         /*   put("/borrow/{id}", bookController::borrow, Role.USER);
            put("/return/{id}", bookController::returnBook, Role.USER);

          */
        };
    }
}
