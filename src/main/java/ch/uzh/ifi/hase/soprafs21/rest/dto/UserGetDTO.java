package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class UserGetDTO {

    private Long id;
    private String username;
    private UserStatus status;
    private List<Portfolio> ownedPortfolios;
    private Set<Portfolio> collaboratingPortfolios;
    private Date creationDate;
    private String token;

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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<Portfolio> getOwnedPortfolios() {
        return ownedPortfolios;
    }

    public void setOwnedPortfolios(List<Portfolio> ownedPortfolios) {
        this.ownedPortfolios = ownedPortfolios;
    }

    public Set<Portfolio> getCollaboratingPortfolios() {
        return collaboratingPortfolios;
    }

    public void setCollaboratingPortfolios(Set<Portfolio> collaboratingPortfolios) {
        this.collaboratingPortfolios = collaboratingPortfolios;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
