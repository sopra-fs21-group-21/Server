package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.constant.PositionType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

public class PositionGetDTO {
    Long id;
    String code;
    PositionType type;
    BigDecimal price;
    BigDecimal openingPrice;
    BigDecimal amount;
    BigDecimal value;
    BigDecimal performance;

    public Date getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Date openingTime) {
        this.openingTime = openingTime;
    }

    Date openingTime;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(BigDecimal openingPrice) {
        this.openingPrice = openingPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getPerformance() {
        return performance;
    }

    public void setPerformance() {
        this.performance = this.price.divide(this.openingPrice, MathContext.DECIMAL32);
    }

    public PositionType getType() {
        return type;
    }

    public void setType(PositionType type) {
        this.type = type;
    }
}
