package app.dtos;

import app.entities.Library;
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
public class LibraryDTO {
    private Integer id;
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;
    private UserDTO user = null;

    public LibraryDTO(Library library) {
        this.id = library.getId();
        this.title = library.getTitle();
        this.author = library.getAuthor();
        this.genre = library.getGenre();
        this.isAvailable = library.isAvailable();
        if (library.getUser() != null) {
            User userEntity = library.getUser();
            this.user = new UserDTO(userEntity.getUsername(), userEntity.getRolesAsStrings());
        }
    }
}
