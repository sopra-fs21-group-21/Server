package ch.uzh.ifi.hase.soprafs21.service;

import org.json.JSONObject;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private static final String apiKey = "0W9NIFEZ05JB1L3U";
    private static final String address = "https://www.alphavantage.co/query?apikey=" + apiKey;

    /**
     * Returns the original currency of a stock
     * @param stock code/symbol of the stock
     * @return original currency of a stock
     */
    private static String findCurrency(String stock)
    {
        // Stocks ending in .F are traded in the Frankfurt exchange
        if (Pattern.compile(".\\.F").
                matcher(stock)
                .find())
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
        String specificAddress = address +
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
            //body = body.getJSONObject("Global quote");
            BigDecimal originalPrice = body
                    .getJSONObject("Global Quote")
                    .getBigDecimal("05. price");
            return convertPrice(stock, originalPrice, currency);
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts the price of a given stock to the target currency. The currency of the stock price
     * is inferred based on the exchange the stock is listed on through a regex.
     */
    public static BigDecimal convertPrice(String stock, BigDecimal price, String target_currency)
    {
        String source_currency = findCurrency(stock);
        // address of the request
        String specificAddress = address +
                "&function=CURRENCY_EXCHANGE_RATE" +
                "&from_currency=" + source_currency +
                "&to_currency=" + target_currency;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(
                URI.create(specificAddress))
                .build();

        CompletableFuture<HttpResponse<String>> response = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        if (source_currency.compareTo(target_currency) == 0)
        {
            return price;
        }
        BigDecimal exchange_rate;
        try {
            JSONObject body = new JSONObject(response.get().body());
            exchange_rate = body
                    .getJSONObject("Realtime Currency Exchange Rate")
                    .getBigDecimal("5. Exchange Rate");
            return exchange_rate.multiply(price);
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
