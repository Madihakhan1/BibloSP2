package app.controllers.impl;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dtos.BookDTO;
import app.security.controllers.SecurityController;
import app.security.daos.SecurityDAO;
import app.security.daos.SecurityPopulatorDAO;
import app.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.Javalin;
import io.restassured.common.mapper.TypeRef;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;


import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookControllerTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static Javalin app;
    private static String userToken, adminToken;
    private static final String BASE_URL = "http://localhost:7070/api";
    private static SecurityDAO securityDAO;


    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7070);
    }

    @BeforeEach
    void setUp() {
        System.out.println("Populating database with books and users");

        Populator.populateBooks(emf);
        UserDTO[] users = SecurityPopulatorDAO.populateUsers(emf);

        securityDAO = new SecurityDAO(emf);

        try {
            UserDTO verifiedUser = securityDAO.getVerifiedUser(users[0].getUsername(), users[0].getPassword());
            UserDTO verifiedAdmin = securityDAO.getVerifiedUser(users[2].getUsername(), users[2].getPassword());

            userToken = "Bearer " + SecurityController.getInstance().createToken(verifiedUser);
            adminToken = "Bearer " + SecurityController.getInstance().createToken(verifiedAdmin);
        } catch (ValidationException e) {
            throw new RuntimeException("User verification failed in setup", e);
        }
    }



    @AfterEach
    void tearDown() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Book").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @AfterAll
    void tearDownAll() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void read() {
        int bookId = 1; // Sørg for denne bog eksisterer i din populator

        given()
                .header("Authorization", userToken)
                .when()
                .get(BASE_URL + "/books/" + bookId)
                .then()
                .statusCode(200)
                .body("id", is(bookId));
    }

    @Test
    void readAll() {
        List<BookDTO> books =
                given()
                        .header("Authorization", userToken)
                        .when()
                        .get(BASE_URL + "/books")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(new TypeRef<List<BookDTO>>() {});

        assertThat(books.size(), greaterThan(0));
    }

    @Test
    void readAllFromUser() {
        List<BookDTO> userBooks =
                given()
                        .header("Authorization", userToken)
                        .when()
                        .get(BASE_URL + "/books/mine")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(new TypeRef<List<BookDTO>>() {});

        assertThat(userBooks, notNullValue());
    }

    @Test
    void create() {
        BookDTO newBook = new BookDTO(999, "Ny bog", "Ny forfatter");

        given()
                .header("Authorization", adminToken)
                .contentType("application/json")
                .body(newBook)
                .when()
                .post(BASE_URL + "/books/")
                .then()
                .statusCode(201)
                .body("title", is("Ny bog"));
    }

    @Test
    void update() {
        int bookId = 1;
        BookDTO updatedBook = new BookDTO(bookId, "Opdateret titel", "Opdateret forfatter");

        given()
                .header("Authorization", adminToken)
                .contentType("application/json")
                .body(updatedBook)
                .when()
                .put(BASE_URL + "/books/" + bookId)
                .then()
                .statusCode(200)
                .body("title", is("Opdateret titel"));
    }

    @Test
    void delete() {
        int bookId = 1;

        given()
                .header("Authorization", adminToken)
                .when()
                .delete(BASE_URL + "/books/" + bookId)
                .then()
                .statusCode(204);
    }

    @Test
    void populate() {
        given()
                .header("Authorization", adminToken)
                .when()
                .post(BASE_URL + "/books/populate")
                .then()
                .statusCode(200)
                .body("message", is("Books from JSON saved to database"));
    }
}
