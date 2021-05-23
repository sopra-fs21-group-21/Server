package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.repository.PortfolioRepository;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioGetDTO;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public Portfolio findPortfolioByCode(String code)
    {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findPortfolioByPortfolioCode(code);
        if (portfolioOptional.isPresent())
        {
            return portfolioOptional.get();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The join code entered is incorrect.");
    }

    public Portfolio addTraderToPortfolio(String portfolioCode, String userToken)
    {
        Portfolio updatedPortfolio = findPortfolioByCode(portfolioCode);
        User trader = userService.getUserByToken(userToken);

        Set<User> traders = updatedPortfolio.getTraders();

        // Check that the user we are about to add is not a trader yet
        if (traders.contains(trader))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already part of this portfolio!");
        }

        traders.add(trader);
        updatedPortfolio.setTraders(traders);

        updatedPortfolio = portfolioRepository.saveAndFlush(updatedPortfolio);
        return updatedPortfolio;
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
        BigDecimal totalValue = BigDecimal.valueOf(startingBalance);

        List<BigDecimal> valueTimeSeries = new ArrayList<>();
        valueTimeSeries.add(0, totalValue);
        newPortfolio.setTotalValue(valueTimeSeries);
        newPortfolio.setLastUpdate(new Date());

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
        if (portfolio.getBalance().compareTo(position.getValue()) < 0)
        {
            // Not enough cash buddy
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Can't afford that :(");
        }
        // Remove cash
        portfolio.setBalance(
                portfolio.getBalance().subtract(position.getValue(), MathContext.DECIMAL32)
        );
        // Add position to the portfolio
        List<Position> updatedPositions = portfolio.getPositions();
        updatedPositions.add(position);
        portfolio.setPositions(updatedPositions);

        portfolio = portfolioRepository.save(portfolio);
        return portfolio;
    }

    public Portfolio closePosition(Long portfolioId, Long positionId)
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
                    portfolio.getBalance().add(position.getValue(), MathContext.DECIMAL32)
            );
            // Delete position from position repository
            positionService.closePosition(positionId);
            // Update the portfolio in the repository (not sure it's necessary
            // but they do it on Stack Overflow
            portfolioRepository.saveAndFlush(portfolio);
            return portfolio;
        }
        else
        {
            // The position was not in the selected portfolio, nothing happens.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such open position in the portfolio.");
        }
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
            capital = capital.add(position.getValue(), MathContext.DECIMAL32);
        }
        return capital;
    }

    // Sum of cash and capital
    public BigDecimal computeTotalValue(Long portfolioId)
    {
        return getCash(portfolioId).add(getCapital(portfolioId));
    }


    // This will make sure that the user is authorized to execute operations on the portfolio
    public void validateRequest(Portfolio portfolio, String token)
    {
        Portfolio targetPortfolio = portfolioRepository.getOne(portfolio.getId());
        User actor = userService.getUserByToken(token);
        if (!targetPortfolio.getTraders().contains(actor))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to execute this operation");
        }

    }

    // Update the portfolio, if 24 hours have passed from the last update, push the updated value to the time series
    // Operates under the assumption that portfolios will be refreshed at least once a day
    public Portfolio updatePortfolio(Portfolio portfolio)
    {
        Portfolio updatedPortfolio = findPortfolioById(portfolio.getId());
        // Update the positions
        for (Position position : portfolio.getPositions())
        {
            positionService.updatePosition(position.getId());
        }
        // If 24 hours have passed since the last update update time series
        Date currentDate = new Date();
        Date lastUpdate = portfolio.getLastUpdate();
        long hoursSinceLastUpdate = TimeUnit.HOURS.convert(
                currentDate.getTime() - lastUpdate.getTime(), TimeUnit.MILLISECONDS);
        BigDecimal updatedValue = computeTotalValue(portfolio.getId());
        if (hoursSinceLastUpdate >= 24)
        {
            // Set this time as the time of the last update
            updatedPortfolio.setLastUpdate(currentDate);
            List<BigDecimal> valueTimeSeries = updatedPortfolio.getTotalValue();
            valueTimeSeries.add(0, updatedValue);
        }
        return portfolioRepository.saveAndFlush(updatedPortfolio);
    }

    public BigDecimal getWeeklyPerformance(Portfolio portfolio)
    {
        List<BigDecimal> valueTimeSeries = portfolio.getTotalValue();
        if (valueTimeSeries.size() >= 7)
        {
            // Value today divided by value last week
            return valueTimeSeries.get(0)
                    .divide(valueTimeSeries.get(6), MathContext.DECIMAL32)
                    .subtract(BigDecimal.valueOf(1));
        }
        else
        {
            return getTotalPerformance(portfolio);
        }
    }

    public BigDecimal getTotalPerformance(Portfolio portfolio)
    {
        List<BigDecimal> valueTimeSeries = portfolio.getTotalValue();
        // Performance over the last element
        return valueTimeSeries.get(valueTimeSeries.size() - 1)
                .divide(valueTimeSeries.get(0), MathContext.DECIMAL32)
                .subtract(BigDecimal.valueOf(1));
    }

    public PortfolioGetDTO makeGetDTO(Portfolio portfolio)
    {
        PortfolioGetDTO portfolioDTO = DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio);
        portfolioDTO.setTotalPerformance(getTotalPerformance(portfolio));
        portfolioDTO.setWeeklyPerformance(getWeeklyPerformance(portfolio));
        portfolioDTO.setTotValue(computeTotalValue(portfolio.getId()));
        portfolioDTO.setCapital(getCapital(portfolio.getId()));
        return portfolioDTO;
    }
}
