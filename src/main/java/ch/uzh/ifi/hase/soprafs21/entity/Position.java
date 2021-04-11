package ch.uzh.ifi.hase.soprafs21.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "POSITION")
public class Position {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, name="containedPortfolioId")
    private Long portfolioId;

    @Column(nullable = false)
    private BigDecimal totalWorth;


    public BigDecimal getTotalWorth() {
        return totalWorth;
    }

    public void setTotalWorth(BigDecimal totalWorth) {
        this.totalWorth = totalWorth;
    }

}
