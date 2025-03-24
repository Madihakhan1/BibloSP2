package app.routes;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private final LibraryRoute libraryRoute = new LibraryRoute();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/libraries", libraryRoute.getRoutes());
        };
    }
}
