package ch.uzh.ifi.hase.soprafs21.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "PORTFOLIO")
public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    // This is a one to many relation hence the annotation
    @Column(nullable = false)
    @ManyToOne
    private User owner;

    // It is nullable, because the owner is automatically also a trader
    // This is a many to many relation hence the annotation. In the JPA tutorial it says to use sets with many to many.
    @Column(nullable = false)
    @ManyToMany(mappedBy = "collaboratingPortfolios")
    private Set<User> traders = new HashSet<User>();
}
