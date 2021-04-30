package ch.uzh.ifi.hase.soprafs21.entity;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unique across the database -> composes the primary key
 */
@Entity
@Table(name="\"user\"")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, unique = true)
    private String mail;

    @Column(nullable = false)
    private Date creationDate;

    // This is a one to many relation hence the annotation
    @OneToMany (mappedBy = "owner")
    private List<Portfolio> ownedPortfolios = new ArrayList<>();

    // This is a many to many relation hence the annotation. In the JPA tutorial it says to use sets with many to many.
    @ManyToMany
    private Set<Portfolio> collaboratingPortfolios = new HashSet<>();

    // ===============Getters and setters===============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getMail() { return mail; }

    public void setMail(String mail) { this.mail = mail; }

    public Date getCreationDate() { return creationDate; }

    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    public List<Portfolio> getOwnedPortfolios() { return ownedPortfolios; }

    // This might be redundant
    public void setOwnedPortfolios(List<Portfolio> ownedPortfolios) { this.ownedPortfolios = ownedPortfolios; }

    public Set<Portfolio> getCollaboratingPortfolios() { return collaboratingPortfolios; }

    // This might be redundant
    public void setCollaboratingPortfolios(Set<Portfolio> collaboratingPortfolios) { this.collaboratingPortfolios = collaboratingPortfolios; }

    // ===============Additional Methods===============

    // Adds Portfolio to owned portfolio list.
    public void addOwnedPortfolio(Portfolio newPortfolio) {
        this.ownedPortfolios.add(newPortfolio);
    }

    // Adds Portfolio to collaborating portfolio list.
    public void addCollaboratingPortfolio(Portfolio joinedPortfolio) {
        this.collaboratingPortfolios.add(joinedPortfolio);
    }

}
