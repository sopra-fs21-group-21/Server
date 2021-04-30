package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;

import java.util.*;

public class UserGetDTO {

    private Long id;
    private String username;
    private UserStatus status;
    private List<SmallPortfolioDTO> ownedPortfolios;
    private Set<SmallPortfolioDTO> collaboratingPortfolios;
    private Date creationDate;
    private String token;
    private String mail;


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

    public List<SmallPortfolioDTO> getOwnedPortfolios() {
        return ownedPortfolios;
    }

    public void setOwnedPortfolios(List<Portfolio> ownedPortfolios) {
        List<SmallPortfolioDTO> smallPortfolioDTOS = new ArrayList<>();
        for (Portfolio portfolio : ownedPortfolios)
        {
            smallPortfolioDTOS.add(
                    new SmallPortfolioDTO(DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio))
            );
        }
        this.ownedPortfolios = smallPortfolioDTOS;
    }

    public Set<SmallPortfolioDTO> getCollaboratingPortfolios() {
        return collaboratingPortfolios;
    }

    public void setCollaboratingPortfolios(Set<Portfolio> collaboratingPortfolios) {
        // need to convert the entities into DTOs to avoid fractal get mapping
        Set<SmallPortfolioDTO> smallPortfolioDTOS = new HashSet<>();
        for (Portfolio portfolio: collaboratingPortfolios)
        {
            smallPortfolioDTOS.add(
                    new SmallPortfolioDTO(DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio))
            );
        }
        this.collaboratingPortfolios = smallPortfolioDTOS;
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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
