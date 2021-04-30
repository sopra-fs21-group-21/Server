package ch.uzh.ifi.hase.soprafs21.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FinanceServiceTest {

    @Test
    public void getPriceDifferentCurrency_successful()
    {
        // this test makes use of the entire functionality of finance service.
        BigDecimal price = FinanceService.getStockPrice("AAPL", "CHF");

        assertNotNull(price);
    }
}
