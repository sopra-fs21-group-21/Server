package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.PositionType;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private PositionService positionService;

    private Position testPosition;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setCode("AAPL");
        testPosition.setPrice(BigDecimal.valueOf(100));
        testPosition.setAmount(BigDecimal.valueOf(10));

        // when -> any object is being save in the userRepository -> return the dummy testUser
        Mockito.when(positionRepository.save(Mockito.any())).thenReturn(testPosition);
    }

    @Test
    public void openPosition_validInput() {
        // when -> any object is being save in the userRepository -> return the dummy testUser
        Position createdPosition = positionService.openPosition(testPosition);

        // then
        Mockito.verify(positionRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testPosition.getId(), createdPosition.getId());
        assertEquals(testPosition.getCode(), createdPosition.getCode());
        assertEquals(testPosition.getPrice(), createdPosition.getPrice());
        assertEquals(testPosition.getOpeningPrice(), createdPosition.getOpeningPrice());
    }

    @Test
    public void openPosition_nullCode_throwsException() {
        testPosition.setCode(null);

        assertThrows(ResponseStatusException.class, () -> positionService.openPosition(testPosition));
    }

    @Test
    public void openPosition_invalidAmount_throwsException() {
        testPosition.setAmount(BigDecimal.valueOf(-5));
        assertThrows(ResponseStatusException.class, () -> positionService.openPosition(testPosition));
    }

    @Test
    public void updatePosition_Long()
    {
        testPosition.setType(PositionType.STOCK_LONG);
        positionRepository.save(testPosition);

        BigDecimal updatedPrice = BigDecimal.valueOf(150);
        BigDecimal updatedTotalWorth = updatedPrice.multiply(testPosition.getAmount());

        Mockito.when(positionService.findPositionById(Mockito.any())).thenReturn(testPosition);
        Mockito.when(FinanceService.getStockPrice(Mockito.any(), Mockito.any())).thenReturn(updatedPrice);

        positionService.updatePosition(Mockito.any());

        assertEquals(updatedPrice, testPosition.getPrice());
        assertEquals(updatedTotalWorth, testPosition.getValue());
    }


}
