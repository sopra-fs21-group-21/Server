package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.*;

@Service

public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    private final PositionService positionService;

    @Autowired
    public PortfolioService(@Qualifier("portfolioRepository") PortfolioRepository portfolioRepository,
                            PositionService positionService) {
        this.portfolioRepository = portfolioRepository;
        this.positionService = positionService;
    }

    public Portfolio findPortfolioById(Long id)
    {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findById(id);
        if (portfolioOptional.isPresent())
        {
            return portfolioOptional.get();
        }
        throw new EntityNotFoundException("No portfolio associated to the ID");
    }

    /**
     * Saves a new portfolio in the repository.
     * The input should specify at least owner, name, visibility.
     */
    public Portfolio createPortfolio(Portfolio newPortfolio) {
        final int startingBalance = 100000;

        newPortfolio.setCreationDate(new Date());
        newPortfolio.setPortfolioCode(UUID.randomUUID().toString());
        newPortfolio.setBalance(BigDecimal.valueOf(startingBalance));

        newPortfolio = portfolioRepository.saveAndFlush(newPortfolio);

        return newPortfolio;
    }

    /**
     * A position is opened through the PositionService and added to the portfolio
     * @param portfolioId target portfolio
     * @param position position to open
     * @return the position as opened
     */
    public Position openPosition(Long portfolioId, Position position)
    {
        Portfolio portfolio = findPortfolioById(portfolioId);
        // Create the position and store it
        position = positionService.openPosition(position);

        // Add the position to the portfolio
        List<Position> updatedPositions = portfolio.getPositions();
        updatedPositions.add(position);
        portfolio.setPositions(updatedPositions);

        portfolioRepository.flush();
        return position;
    }

    public void closePosition(Long portfolioId, Long positionId)
    {
        // Update the position and store the value
        Position position = positionService.updatePosition(positionId);
        Portfolio portfolio = findPortfolioById(portfolioId);
        List<Position> positions = portfolio.getPositions();
        // If the position is open in the portfolio, remove it
        if(positions.remove(position))
        {
            // Realize gain/losses
            portfolio.setBalance(
                    portfolio.getBalance().add(position.getTotalWorth())
            );
            // Delete position from position repository
            positionService.closePosition(positionId);
            portfolioRepository.flush();
        }
        // The position was not in the selected portfolio, nothing happens.
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such open position in the portfolio.");
    }

    public BigDecimal getCash(Long portfolioId)
    {
        Portfolio portfolio = findPortfolioById(portfolioId);
        return portfolio.getBalance();
    }

    // Capital is the sum of the values of all positions
    public BigDecimal getCapital(Long portfolioId)
    {
        Portfolio portfolio = findPortfolioById(portfolioId);
        List<Position> positions = portfolio.getPositions();
        BigDecimal capital = BigDecimal.valueOf(0);
        for (Position position : positions)
        {
            capital = capital.add(position.getTotalWorth());
        }
        return capital;
    }

    // Sum of cash and capital
    public BigDecimal getTotalValue(Long portfolioId)
    {
        return getCash(portfolioId).add(getCapital(portfolioId));
    }

}
