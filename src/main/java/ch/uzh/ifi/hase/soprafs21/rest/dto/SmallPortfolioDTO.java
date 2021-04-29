package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SmallPortfolioDTO {

    private Long id;
    private String name;

    private BigDecimal cash;
    private BigDecimal capital;
    private BigDecimal totValue;
    private BigDecimal weeklyPerformance;
    private BigDecimal totalPerformance;

    public SmallPortfolioDTO(PortfolioGetDTO portfolioGetDTO) {
        this.id = portfolioGetDTO.getId();
        this.name = portfolioGetDTO.getName();
        this.cash = portfolioGetDTO.getCash();
        this.capital = portfolioGetDTO.getCapital();
        this.totValue = portfolioGetDTO.getTotValue();
        this.weeklyPerformance = portfolioGetDTO.getWeeklyPerformance();
        this.totalPerformance = portfolioGetDTO.getTotalPerformance();
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

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getTotValue() {
        return totValue;
    }

    public void setTotValue(BigDecimal totValue) {
        this.totValue = totValue;
    }

    public BigDecimal getWeeklyPerformance() {
        return weeklyPerformance;
    }

    public void setWeeklyPerformance(BigDecimal weeklyPerformance) {
        this.weeklyPerformance = weeklyPerformance;
    }

    public BigDecimal getTotalPerformance() {
        return totalPerformance;
    }

    public void setTotalPerformance(BigDecimal totalPerformance) {
        this.totalPerformance = totalPerformance;
    }
}
