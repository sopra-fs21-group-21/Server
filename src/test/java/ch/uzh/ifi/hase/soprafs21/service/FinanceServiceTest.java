package ch.uzh.ifi.hase.soprafs21.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FinanceServiceTest {

    @Test
    public void getPriceDifferentCurrency_successful()
    {
        // this test makes use of the entire functionality of finance service.
        BigDecimal price = FinanceService.getStockPrice("VOW.F", "CHF");

        assertNotNull(price);
    }

    @Test
    public void getPositionInformation_test()
    {
        Map<String, BigDecimal> response = FinanceService.getStockInfo("AAPL", "CHF");
        assertNotNull(response);
    }
}
