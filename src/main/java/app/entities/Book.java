package app.entities;

import app.dtos.BookDTO;
import app.security.entities.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@Builder
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", referencedColumnName = "username")
    @ToString.Exclude
    private User user;

    public Book(BookDTO bookDTO) {
        this.id = bookDTO.getId();
        this.title = bookDTO.getTitle();
        this.author = bookDTO.getAuthor();
        this.genre = bookDTO.getGenre();
        this.isAvailable = bookDTO.isAvailable();

        if (bookDTO.getUser() != null) {
            User userEntity = new User();
            userEntity.setUsername(bookDTO.getUser().getUsername());
            this.user = userEntity;
        }
    }

    public Book(String book, String book1) {
    }

    public void removeUser() {
        if (this.user != null) {
            this.user.getBooks().remove(this);
        }
        this.user = null;
    }
}
