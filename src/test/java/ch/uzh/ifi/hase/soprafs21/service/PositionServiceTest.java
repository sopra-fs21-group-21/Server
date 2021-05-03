package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.PositionType;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

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
        testPosition.setCurrency("CHF");
        testPosition.setOpeningPrice(BigDecimal.valueOf(100));

        // when -> any object is being save in the userRepository -> return the dummy testUser
        Mockito.when(positionRepository.saveAndFlush(Mockito.any())).thenReturn(testPosition);
    }

    @Test
    public void openPosition_validInput() {
        // when -> any object is being saved in the positionRepository -> return the dummy testUser

        Position createdPosition = positionService.openPosition(testPosition);

        // then
        Mockito.verify(positionRepository, Mockito.times(1)).saveAndFlush(Mockito.any());


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
        Mockito.doReturn(testPosition).when(positionRepository).saveAndFlush(Mockito.any());
        Mockito.doReturn(Optional.ofNullable(testPosition)).when(positionRepository).findById(Mockito.any());

        testPosition.setType(PositionType.STOCK_LONG);
        positionRepository.saveAndFlush(testPosition);

        testPosition = positionService.updatePosition(testPosition.getId());

        BigDecimal updatedPrice = testPosition.getPrice();
        BigDecimal updatedTotalWorth = updatedPrice.multiply(testPosition.getAmount(), MathContext.DECIMAL32);

        assertEquals(updatedPrice, testPosition.getPrice());
        assertEquals(updatedTotalWorth, testPosition.getValue());
    }

    @Test
    public void updatePosition_Short()
    {
        Mockito.doReturn(testPosition).when(positionRepository).saveAndFlush(Mockito.any());
        Mockito.doReturn(Optional.ofNullable(testPosition)).when(positionRepository).findById(Mockito.any());

        testPosition.setType(PositionType.STOCK_SHORT);
        positionRepository.saveAndFlush(testPosition);

        testPosition = positionService.updatePosition(testPosition.getId());

        BigDecimal updatedPrice = testPosition.getPrice();
        BigDecimal updatedTotalWorth = testPosition.getOpeningPrice()
                .subtract(updatedPrice, MathContext.DECIMAL32)
                .multiply(testPosition.getAmount(), MathContext.DECIMAL32);

        assertEquals(updatedPrice, testPosition.getPrice());
        assertEquals(updatedTotalWorth, testPosition.getValue());
    }


}
