package ch.uzh.ifi.hase.soprafs21.rest.dto;


import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;

/**
 * This class doesn't have mappings and it provides the object returned
 * for each user (owner and traders) in portfolioGetDTOs.
 * It was needed to avoid fractal Get mapping.
 */
public class SmallUserDTO {
    private Long id;
    private String username;
    private UserStatus status;

    public SmallUserDTO(Long id, String username, UserStatus status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

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
}
