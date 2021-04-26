package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;

import java.util.*;

public class UserGetDTO {

    private Long id;
    private String username;
    private UserStatus status;
    private List<PortfolioGetDTO> ownedPortfolios;
    private Set<PortfolioGetDTO> collaboratingPortfolios;
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

    public List<PortfolioGetDTO> getOwnedPortfolios() {
        return ownedPortfolios;
    }

    public void setOwnedPortfolios(List<Portfolio> ownedPortfolios) {
        List<PortfolioGetDTO> portfolioDTOs = new ArrayList<>();
        for (Portfolio portfolio : ownedPortfolios)
        {
            portfolioDTOs.add(
                    DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio)
            );
        }
        this.ownedPortfolios = portfolioDTOs;
    }

    public Set<PortfolioGetDTO> getCollaboratingPortfolios() {
        return collaboratingPortfolios;
    }

    public void setCollaboratingPortfolios(Set<Portfolio> collaboratingPortfolios) {
        // need to convert the entities into DTOs to avoid fractal get mapping
        Set<PortfolioGetDTO> portfolioDTOs = new HashSet<>();
        for (Portfolio portfolio: collaboratingPortfolios)
        {
            portfolioDTOs.add(
                    DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio)
            );
        }
        this.collaboratingPortfolios = portfolioDTOs;
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
