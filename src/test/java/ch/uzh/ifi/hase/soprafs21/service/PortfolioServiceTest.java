package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PortfolioServiceTest {
    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PositionService positionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PortfolioService portfolioService;


    private Position testPosition;
    private User testUser;
    private User testUser2;
    private Portfolio testSharedPortfolio;
    private Portfolio testPrivatePortfolio;
    List<Position> positionList;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setUsername("testUser");

        Set<User> userSet = new HashSet<>();
        userSet.add(testUser);

        testUser2 = new User();
        testUser2.setUsername("testUser2");
        testUser2.setToken("123");

        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setCode("AAPL");

        positionList= new ArrayList<>();
        positionList.add(testPosition);

        testPosition.setValue(BigDecimal.valueOf(1000));



        testSharedPortfolio = new Portfolio();
        testSharedPortfolio.setPortfolioName("testSharedPortfolioName");
        testSharedPortfolio.setPortfolioVisibility(PortfolioVisibility.SHARED);
        testSharedPortfolio.setId(1L);

        testPrivatePortfolio = new Portfolio();
        testPrivatePortfolio.setId(1L);
        testPrivatePortfolio.setPortfolioName("testPrivatePortfolioName");
        testPrivatePortfolio.setPortfolioVisibility(PortfolioVisibility.PRIVATE);

        // when -> any object is being saved in the userRepository -> return the dummy testUser
        Mockito.when(portfolioRepository.saveAndFlush(Mockito.any())).thenReturn(testSharedPortfolio);

    }

    @Test
    public void createSharedPortfolio_validInput_success() {
        // when -> any object is being save in the userRepository -> return the dummy testUser
        Portfolio creationData = new Portfolio();
        creationData.setPortfolioName("testSharedPortfolio");
        // default portfolio is shared so no need to set visibility in creationData

        Mockito.when(portfolioRepository.existsByPortfolioName(Mockito.any())).thenReturn(false);
        Mockito.when(portfolioRepository.saveAndFlush(Mockito.any())).thenReturn(testSharedPortfolio);

        // then

        Portfolio createdSharedPortfolio = portfolioService.createPortfolio(testSharedPortfolio);
        Mockito.verify(portfolioRepository, Mockito.times(1)).saveAndFlush(Mockito.any());

        assertEquals(1L, createdSharedPortfolio.getId());
        assertEquals(testSharedPortfolio.getPortfolioName(), createdSharedPortfolio.getPortfolioName());
        assertEquals(testSharedPortfolio.getPortfolioVisibility(), createdSharedPortfolio.getPortfolioVisibility());
        assertNotNull(createdSharedPortfolio.getPortfolioCode());
        assertNotNull(createdSharedPortfolio.getCreationDate());
        assertEquals(BigDecimal.valueOf(100000), createdSharedPortfolio.getBalance());

        BigDecimal totalValue = BigDecimal.valueOf(100000);
        List<BigDecimal> valueTimeSeries = new ArrayList<>();
        valueTimeSeries.add(totalValue);

        assertEquals(valueTimeSeries, createdSharedPortfolio.getTotalValue());
        assertNotNull(createdSharedPortfolio.getLastUpdate());
    }

    @Test
    public void createPrivatePortfolio_validInput_success() {
        // when -> any object is being saved in the portfolioRepository -> return the dummy Portfolio
        Portfolio creationData = new Portfolio();
        creationData.setPortfolioName("testSharedPortfolio");
        creationData.setPortfolioVisibility(PortfolioVisibility.PRIVATE);

        Mockito.when(portfolioRepository.existsByPortfolioName(Mockito.any())).thenReturn(false);
        Mockito.when(portfolioRepository.saveAndFlush(Mockito.any())).thenReturn(testPrivatePortfolio);

        // then

        Portfolio createdSharedPortfolio = portfolioService.createPortfolio(testPrivatePortfolio);
        Mockito.verify(portfolioRepository, Mockito.times(1)).saveAndFlush(Mockito.any());

        assertEquals(1L, createdSharedPortfolio.getId());
        assertEquals(testPrivatePortfolio.getPortfolioName(), createdSharedPortfolio.getPortfolioName());
        assertEquals(testPrivatePortfolio.getPortfolioVisibility(), createdSharedPortfolio.getPortfolioVisibility());
        assertNotNull(createdSharedPortfolio.getPortfolioCode());
        assertNotNull(createdSharedPortfolio.getCreationDate());
        assertEquals(BigDecimal.valueOf(100000), createdSharedPortfolio.getBalance());

        BigDecimal totalValue = BigDecimal.valueOf(100000);
        List<BigDecimal> valueTimeSeries = new ArrayList<>();
        valueTimeSeries.add(totalValue);

        assertEquals(valueTimeSeries, createdSharedPortfolio.getTotalValue());
        assertNotNull(createdSharedPortfolio.getLastUpdate());
    }

    @Test
    public void createPortfolio_duplicateName_throwsException(){
        // given -> a first portfolio has already been created

        // when -> setup additional mocks for UserRepository
        Mockito.when(portfolioRepository.existsByPortfolioName(Mockito.any())).thenReturn(true);

        // then -> attempt to create second user with same username -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> portfolioService.createPortfolio(testSharedPortfolio));
    }

    @Test
    public void openPosition_validInput_success(){
        Portfolio portfolio = portfolioService.createPortfolio(testSharedPortfolio);

        Mockito.when(positionService.openPosition(Mockito.any())).thenReturn(testPosition);
        Mockito.when(portfolioRepository.findById(Mockito.any())).thenReturn(Optional.of(portfolio));

        portfolioService.openPosition(portfolio.getId(),testPosition);

        assertEquals(BigDecimal.valueOf(99000), portfolio.getBalance());
        assertEquals(1, portfolio.getPositions().size());
        assertEquals(positionList, portfolio.getPositions());
    }

    @Test
    public void openPosition_notEnoughMoney_throwsException(){
        Portfolio portfolio = portfolioService.createPortfolio(testSharedPortfolio);

        testPosition.setValue(BigDecimal.valueOf(10000000));

        Mockito.when(positionService.openPosition(Mockito.any())).thenReturn(testPosition);
        Mockito.when(portfolioRepository.findById(Mockito.any())).thenReturn(Optional.of(portfolio));

        assertThrows(ResponseStatusException.class, () -> portfolioService.openPosition(portfolio.getId(), testPosition));
    }

    @Test
    public void closePosition_validInput_success(){
        Portfolio portfolio = portfolioService.createPortfolio(testSharedPortfolio);

        testPosition.setValue(BigDecimal.valueOf(2000));
        portfolio.setPositions(positionList);

        Mockito.when(positionService.updatePosition(Mockito.any())).thenReturn(testPosition);
        Mockito.when(portfolioRepository.findById(Mockito.any())).thenReturn(Optional.of(portfolio));

        portfolioService.closePosition(portfolio.getId(), 1L);

        // Since the Portfolio starts with 100000 the added value from a long close is 2000
        assertEquals(BigDecimal.valueOf(102000), portfolio.getBalance());
        assertEquals(0, portfolio.getPositions().size());
    }

    @Test
    public void closePosition_noSuchPosition_throwsException(){
        Portfolio portfolio = portfolioService.createPortfolio(testSharedPortfolio);

        testPosition.setValue(BigDecimal.valueOf(2000));
        
        Mockito.when(portfolioRepository.findById(Mockito.any())).thenReturn(Optional.of(portfolio));

        assertThrows(ResponseStatusException.class, () -> portfolioService.closePosition(portfolio.getId(), 1L));
    }

    @Test
    public void addTraderToPortfolio_validInputs_success(){
        // given -> a portfolio and user not int that portfolio already exists
        Portfolio portfolio = portfolioService.createPortfolio(testSharedPortfolio);

        // The user that wants to join will be returned
        Mockito.when(userService.getUserByToken(Mockito.any())).thenReturn(testUser2);

        // The portfolio that the code belongs to will be returned
        Mockito.when(portfolioRepository.findPortfolioByPortfolioCode(Mockito.any())).thenReturn(Optional.ofNullable(portfolio));

        // The trader gets added to the Portfolio with that join code
        portfolioService.addTraderToPortfolio(portfolio.getPortfolioCode(), testUser2.getToken());

        //Check that the user is indeed a trader of said portfolio
        assertTrue(testSharedPortfolio.getTraders().contains(testUser2));
    }

    @Test
    public void addTraderToPortfolio_wrongCode_throwsException(){
        // given -> a portfolio and user not in that portfolio already exists
        Portfolio portfolio = portfolioService.createPortfolio(testSharedPortfolio);

        // The user that wants to join will be returned
        Mockito.when(userService.getUserByToken(Mockito.any())).thenReturn(testUser2);

        // No portfolio with that code gets returned
        Mockito.when(portfolioRepository.findPortfolioByPortfolioCode(Mockito.any())).thenReturn(Optional.empty());

        // An error gets thrown, because the joinCode is wrong
        assertThrows(ResponseStatusException.class, () -> portfolioService.addTraderToPortfolio("wrongCode", testUser2.getToken()));
    }
}
