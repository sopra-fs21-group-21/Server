package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.entity.User;
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
    private final UserService userService;

    @Autowired
    public PortfolioService(@Qualifier("portfolioRepository") PortfolioRepository portfolioRepository,
                            PositionService positionService,
                            UserService userService) {
        this.portfolioRepository = portfolioRepository;
        this.positionService = positionService;
        this.userService = userService;
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

    public List<Portfolio> getSharedPortfolios()
    {
        List<Portfolio> allPortfolios = portfolioRepository.findAll();
        List<Portfolio> visiblePortfolios = new ArrayList<>();

        for (Portfolio portfolio : allPortfolios)
        {
            if (portfolio.getPortfolioVisibility() == PortfolioVisibility.SHARED)
            {
                visiblePortfolios.add(portfolio);
            }
        }

        // We will need to sort the list here

        return visiblePortfolios;
    }

    /**
     * Saves a new portfolio in the repository.
     * The input should specify at least owner, name, visibility.
     */
    public Portfolio createPortfolio(Portfolio newPortfolio) {
        final int startingBalance = 100000;

        // Check if the name is already in use
        if (portfolioRepository.existsByPortfolioName(newPortfolio.getPortfolioName()))
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The name is not available :(");
        }

        // Add some extra information
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
    public Portfolio openPosition(Long portfolioId, Position position)
    {
        Portfolio portfolio = findPortfolioById(portfolioId);
        // Create the position and store it
        position = positionService.openPosition(position);

        // Check there is sufficient cash
        if (portfolio.getBalance().compareTo(position.getTotalWorth()) == -1)
        {
            // Not enough cash buddy
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Can't afford that :(");
        }
        // Remove cash
        portfolio.setBalance(
                portfolio.getBalance().subtract(position.getTotalWorth())
        );
        // Add position to the portfolio
        List<Position> updatedPositions = portfolio.getPositions();
        updatedPositions.add(position);
        portfolio.setPositions(updatedPositions);

        portfolio = portfolioRepository.save(portfolio);
        return portfolio;
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


    // This will make sure that the user is authorized to execute operations on the portfolio
    public void validateRequest(Portfolio portfolio, String token)
    {
        Portfolio targetPortfolio = portfolioRepository.getOne(portfolio.getId());
        User actor = userService.getUserByToken(token);
        if (targetPortfolio.getTraders().contains(actor))
        {
            return;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to execute this operation");
    }
}
