package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;

public class PortfolioPostDTO {

    private String name;

    private PortfolioVisibility visibility;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PortfolioVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PortfolioVisibility visibility) {
        this.visibility = visibility;
    }
}
