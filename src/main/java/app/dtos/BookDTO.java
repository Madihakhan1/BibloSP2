package app.dtos;

import app.entities.Book;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import app.security.entities.User;
import dk.bugelhartmann.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO {
    private Integer id;
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;
    private UserDTO user = null;

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
}
