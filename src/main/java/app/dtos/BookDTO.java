package app.dtos;

import app.entities.Book;
import app.security.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.bugelhartmann.UserDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO {

    private Integer id;
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;
    private UserDTO user = null;

    // DTO constructor from Book entity
    public BookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.genre = book.getGenre();
        this.isAvailable = book.isAvailable();

        if (book.getUser() != null) {
            User userEntity = book.getUser();
            this.user = new UserDTO(userEntity.getUsername(), userEntity.getRolesAsStrings());
        }
    }

    // Convert DTO to entity
    @JsonIgnore
    public Book getAsEntity() {
        Book book = new Book();
        book.setId(this.id);
        book.setTitle(this.title);
        book.setAuthor(this.author);
        book.setGenre(this.genre);
        book.setAvailable(this.isAvailable);

        if (this.user != null) {
            User userEntity = new User();
            userEntity.setUsername(this.user.getUsername());
            // Rollen behøver normalt ikke sættes her ved bare relation
            book.setUser(userEntity);
        }

        return book;
    }
}
