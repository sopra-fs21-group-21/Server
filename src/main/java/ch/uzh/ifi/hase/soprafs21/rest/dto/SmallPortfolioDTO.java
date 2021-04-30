package ch.uzh.ifi.hase.soprafs21.rest.dto;


import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;

import java.math.BigDecimal;

public class SmallPortfolioDTO {

    private Long id;
    private String name;

    private BigDecimal weeklyPerformance;

    public SmallPortfolioDTO(PortfolioGetDTO portfolioGetDTO) {
        this.id = portfolioGetDTO.getId();
        this.name = portfolioGetDTO.getName();
        this.weeklyPerformance = portfolioGetDTO.getWeeklyPerformance();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeeklyPerformance() {
        return weeklyPerformance;
    }

    public void setWeeklyPerformance(BigDecimal weeklyPerformance) {
        this.weeklyPerformance = weeklyPerformance;
    }

}
