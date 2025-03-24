package app.entities;

import dat.dtos.LibraryDTO;
import dat.security.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;

    @ToString.Exclude // Avoid recursion
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user = null;

    public Library(LibraryDTO libraryDTO) {
        this.id = libraryDTO.getId();
        this.title = libraryDTO.getTitle();
        this.author = libraryDTO.getAuthor();
        this.genre = libraryDTO.getGenre();
        this.isAvailable = libraryDTO.isAvailable();
        this.user = new User(libraryDTO.getUser());
    }

    public void removeUser() {
        if (this.user != null) {
            this.user.getBooks().remove(this); // Husk at opdatere User-klassen til at have getBooks()
        }
        this.user = null;
    }
}
