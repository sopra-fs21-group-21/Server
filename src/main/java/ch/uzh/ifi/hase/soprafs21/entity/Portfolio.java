package ch.uzh.ifi.hase.soprafs21.entity;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.constant.PositionType;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

@Entity
@Table(name = "PORTFOLIO")
public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="portfolioId")
    private Long id;

    // This is a one to many relation hence the annotation
    @ManyToOne
    @JoinColumn(name = "id")
    private User owner;

    // It is not nullable, because the owner is automatically also a trader
    // This is a many to many relation hence the annotation. In the JPA tutorial it says to use sets with many to many.

    @ManyToMany
    private Set<User> traders = new HashSet<User>();

    @Column(nullable = false, unique=true)
    private String portfolioName;

    // This is the join code for a shared portfolio
    @Column(nullable = false, unique=true)
    private String portfolioCode;

    @Column(nullable = false)
    private PortfolioVisibility portfolioVisibility = PortfolioVisibility.SHARED;

    // Automatically sets balance to starter value when a new Portfolio gets created
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.valueOf(100000);

    @Column(nullable = false)
    private Date creationDate;

    // Contains the time series of the total value of the portfolio.
    // At index 0 you have the latest update, at index i you have the total value i days ago.
    @ElementCollection
    private List<BigDecimal> totalValue = new ArrayList<>();

    @Column
    private BigDecimal weeklyPerformance;

    @Column
    private BigDecimal totalPerformance;

    @Column
    private Date lastUpdate;

    @OneToMany
    public List<Position> positions = new ArrayList<Position>();

    // ===============Getters and setters===============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getTraders() {
        return traders;
    }

    public void setTraders(Set<User> traders) {
        this.traders = traders;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public String getPortfolioCode() {
        return portfolioCode;
    }

    public void setPortfolioCode(String portfolioCode) {
        this.portfolioCode = portfolioCode;
    }

    public PortfolioVisibility getPortfolioVisibility() {
        return portfolioVisibility;
    }

    public void setPortfolioVisibility(PortfolioVisibility portfolioVisibility) {
        this.portfolioVisibility = portfolioVisibility;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public BigDecimal getWeeklyPerformance() {
        List<BigDecimal> valueTimeSeries = getTotalValue();
        if (valueTimeSeries.size() >= 7)
        {
            // Value today divided by value last week
            return valueTimeSeries.get(0)
                    .divide(valueTimeSeries.get(6), MathContext.DECIMAL32)
                    .subtract(BigDecimal.valueOf(1));
        }
        else
        {
            return getTotalPerformance();
        }
    }

    public BigDecimal getTotalPerformance()
    {
        List<BigDecimal> valueTimeSeries = getTotalValue();
        // Performance over the last element
        return valueTimeSeries.get(valueTimeSeries.size() - 1)
                .divide(valueTimeSeries.get(0), MathContext.DECIMAL32)
                .subtract(BigDecimal.valueOf(1));
    }

    public List<BigDecimal> getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(List<BigDecimal> totalValue) {
        this.totalValue = totalValue;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
