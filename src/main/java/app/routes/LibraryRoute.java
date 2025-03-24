package app.routes;

import app.controllers.impl.LibraryController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class LibraryRoute {
    private final LibraryController libraryController = new LibraryController();

    protected EndpointGroup getRoutes() {

        return () -> {
            post("/populate", libraryController::populate, Role.ANYONE);
            post("/", libraryController::create, Role.USER);
            get("/", libraryController::readAll, Role.ANYONE);
            get("/mine", libraryController::readAllFromUser, Role.USER);
            get("/{id}", libraryController::read, Role.USER);
            put("/{id}", libraryController::update, Role.USER);
            delete("/{id}", libraryController::delete, Role.USER);
        };
    }
}
