package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.constant.PositionType;

import java.math.BigDecimal;

public class PositionPostDTO {
    private String code;
    private BigDecimal amount;
    private PositionType type;

    public PositionPostDTO(String code, BigDecimal amount, PositionType type) {
        this.code = code;
        this.amount = amount;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PositionType getType() {
        return type;
    }

    public void setType(PositionType type) {
        this.type = type;
    }
}
