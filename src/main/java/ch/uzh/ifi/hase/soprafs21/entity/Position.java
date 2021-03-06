package ch.uzh.ifi.hase.soprafs21.entity;

import ch.uzh.ifi.hase.soprafs21.constant.PositionType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "POSITION")

public class Position {
    @Id
    @GeneratedValue
    private Long id;

    // Code of the asset (e.g. stock, currency...) as in Alpha Vantage (e.g. "AAPL", "BTC")
    @Column(nullable = false)
    private String code;

    // Long or Short, stock or currency etc...
    @Column(nullable = false)
    private PositionType type;

    // Amount of the asset in the position (e.g. 10 stocks of "AAPL").
    // BigDecimal because it may not be a decimal for currency
    // and other financial instruments.
    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal price;

    @Column
    private String currency;

    @Column(nullable = false)
    private BigDecimal value;

    @Column (nullable = false)
    private BigDecimal openingPrice;

    @Column
    private Date openingTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public PositionType getType() {
        return type;
    }

    public void setType(PositionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal totalWorth) {
        this.value = totalWorth;
    }

    public BigDecimal getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(BigDecimal openingValue) {
        this.openingPrice = openingValue;
    }

    public Date getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Date openingTime) {
        this.openingTime = openingTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
