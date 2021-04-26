package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.Optional;
@Service

public class PositionService {

    private final PositionRepository positionRepository;

    @Autowired
    public PositionService(@Qualifier("positionRepository") PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public Position findPositionById(Long id)
    {
        Optional<Position> optionalPosition = positionRepository.findById(id);
        if (optionalPosition.isPresent())
        {
            return optionalPosition.get();
        }
        throw new javax.persistence.EntityNotFoundException("No position associated to the ID");
    }

    private void validatePosition(Position position)
    {
        if (position.getCode() == null)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position code cannot be null.");
        }
        if (position.getAmount() == null || position.getAmount().intValue() < 0)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount for opening a position");
        }
    }

    /**
     * A new position is created and saved in the repository
     * @param newPosition specifying positionType, code, amount
     * @return position as stored in the repository
     */
    public Position openPosition(Position newPosition)
    {
        validatePosition(newPosition);
        // BelongingPortfolio, price, code and amount are passed through the DTO mapper
        newPosition.setPrice(
                FinanceService.getStockPrice(newPosition.getCode(), "CHF"));
        newPosition.setOpeningTime(new Date());
        newPosition.setOpeningPrice(
                newPosition.getPrice()
        );
        newPosition.setTotalWorth(newPosition.getOpeningPrice());
        newPosition.setCurrency("CHF");

        newPosition = positionRepository.saveAndFlush(newPosition);

        return newPosition;
    }

    /**
     * Updates price and totalWorth of the position
     */
    public Position updatePosition(Long id)
    {
        Position position = findPositionById(id);
        BigDecimal price = FinanceService.getStockPrice(position.getCode(), position.getCurrency());

        position.setPrice(price);

        // Different for different position types, default is long, price * amount
        position.setTotalWorth(
                switch(position.getType())
                        {
                            case STOCK_SHORT:
                                // (Opening price - current price) * amount
                                yield position.getOpeningPrice()
                                        .subtract(position.getPrice(), MathContext.DECIMAL32)
                                        .multiply(position.getAmount(), MathContext.DECIMAL32);
                            default:
                                yield position.getAmount().multiply(price, MathContext.DECIMAL32);
                        }

        );

        position = positionRepository.saveAndFlush(position);
        return position;
    }

    /**
     * Deletes a position from the repository
     * @param id of the position to be deleted
     */
    public void closePosition(Long id)
    {
        positionRepository.deleteById(id);
    }

}
