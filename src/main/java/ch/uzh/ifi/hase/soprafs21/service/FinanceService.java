package ch.uzh.ifi.hase.soprafs21.service;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Finance Service
 *
 * This class deals with the REST requests to Alpha Vantage and should be the only contact point between
 * Alpha Vantage and the rest of the application.
 */

public class FinanceService {

    // request endpoint address including Api Key
    private static final String API_KEY = "0W9NIFEZ05JB1L3U";
    private static final String ADDRESS = "https://www.alphavantage.co/query?apikey=" + API_KEY;

    private static final String GLOBAL_QUOTE = "Global Quote";

    private FinanceService() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns the original currency of a stock
     * @param stock code/symbol of the stock
     * @return original currency of a stock
     */
    private static String findCurrency(String stock)
    {
        // Stocks ending in .F or .FRK are traded in the Frankfurt exchange
        if (Pattern.compile(".\\.F").matcher(stock).find() ||
            Pattern.compile(".\\.FRK").matcher(stock).find())
        {
            return "EUR";
        }
        // Swiss exchange
        else if (Pattern.compile(".\\.SW").
                matcher(stock)
                .find()) {
            return "CHF";
        }
        // US stocks do not have a special suffix
        return "USD";
    }

    /**
     * Returns the latest price of a stock as provided by AlphaVantage and fetched via a
     * Global Quote request
     * @param stock code/symbol of the stock
     * @return returns the latest trading price of the stock
     */
    public static BigDecimal getStockPrice(String stock, String currency)
    {

        // I spent a bunch of hours on this and couldn't find a prettier way to add URI parameters
        String specificAddress = ADDRESS +
                "&function=GLOBAL_QUOTE" +
                "&symbol=" + stock;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(
                URI.create(specificAddress))
                .build();

        // Send the request and store the result in this special type for Async programming

        CompletableFuture<HttpResponse<String>> response = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // We may or may not have got a response, hence the try-catch syntax
        try {
            JSONObject body = new JSONObject(response.get().body());
            // Check that the response is valid
            if (!body.getJSONObject(GLOBAL_QUOTE).has("05. price"))
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code.");
            }
            BigDecimal originalPrice = body
                    .getJSONObject("Global Quote")
                    .getBigDecimal("05. price");
            return convertPrice(stock, originalPrice, currency);
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public static Map<String, BigDecimal> getStockInfo(String stock, String currency)
    {

        // I spent a bunch of hours on this and couldn't find a prettier way to add URI parameters
        String specificAddress = ADDRESS +
                "&function=GLOBAL_QUOTE" +
                "&symbol=" + stock;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(
                URI.create(specificAddress))
                .build();

        // Send the request and store the result in this special type for Async programming

        CompletableFuture<HttpResponse<String>> response = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        Map<String, BigDecimal> stockInformation = new HashMap<>();

        // We may or may not have got a response, hence the try-catch syntax
        try {
            JSONObject body = new JSONObject(response.get().body());
            // Check that the response is valid
            if (!body.getJSONObject(GLOBAL_QUOTE).has("05. price"))
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code.");
            }
            // Insert current price
            BigDecimal originalPrice = body
                    .getJSONObject(GLOBAL_QUOTE)
                    .getBigDecimal("05. price");
            stockInformation.put("currentPrice", convertPrice(stock, originalPrice, currency));
            // Insert trading volume (i.e. number of stocks traded)
            BigDecimal tradingVolume = body
                    .getJSONObject(GLOBAL_QUOTE)
                    .getBigDecimal("06. volume");
            stockInformation.put("lastDayVolume", tradingVolume);
            // Insert previous day close
            BigDecimal previousClose = body
                    .getJSONObject(GLOBAL_QUOTE)
                    .getBigDecimal("08. previous close");
            stockInformation.put("lastDayClose",
                    convertPrice(stock, previousClose, currency));
            // Change from previous close
            stockInformation.put("changeFromLastClose",
                    originalPrice
                            .subtract(previousClose, MathContext.DECIMAL32)
                            .divide(previousClose, MathContext.DECIMAL32));
            return stockInformation;
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * Converts the price of a given stock to the target currency. The currency of the stock price
     * is inferred based on the exchange the stock is listed on through a regex.
     */
    public static BigDecimal convertPrice(String stock, BigDecimal price, String targetCurrency)
    {
        String sourceCurrency = findCurrency(stock);
        // address of the request
        String specificAddress = ADDRESS +
                "&function=CURRENCY_EXCHANGE_RATE" +
                "&from_currency=" + sourceCurrency +
                "&to_currency=" + targetCurrency;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(
                URI.create(specificAddress))
                .build();

        CompletableFuture<HttpResponse<String>> response = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        if (sourceCurrency.compareTo(targetCurrency) == 0)
        {
            return price;
        }
        BigDecimal exchangeRate;
        /*
         Need this temporary variable because reading a BigDecimal directly can
         cause errors due to precision being set to 0.
        */
        double exchangeRateTemp;
        try {
            JSONObject body = new JSONObject(response.get().body());
            exchangeRateTemp = body
                    .getJSONObject("Realtime Currency Exchange Rate")
                    .getDouble("5. Exchange Rate");
            exchangeRate= BigDecimal.valueOf(exchangeRateTemp);
            return exchangeRate.multiply(price, MathContext.DECIMAL32);
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
