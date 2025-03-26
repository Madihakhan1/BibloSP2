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
    }

    // Hjælpefunktion hvis du vil fjerne koblingen til en bruger
    public void removeUser() {
        if (this.user != null) {
            this.user.getBooks().remove(this); // forudsætter user.getBooks() eksisterer
        }
        this.user = null;
    }
}
