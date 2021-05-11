
package ch.uzh.ifi.hase.soprafs21.integration;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.constant.PositionType;
import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.controller.PortfolioController;
import ch.uzh.ifi.hase.soprafs21.controller.UserController;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.repository.PortfolioRepository;
import ch.uzh.ifi.hase.soprafs21.repository.PositionRepository;
import ch.uzh.ifi.hase.soprafs21.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs21.rest.dto.*;
import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;
import ch.uzh.ifi.hase.soprafs21.service.PositionService;
import ch.uzh.ifi.hase.soprafs21.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.collection.IsArrayWithSize;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ControllersIntegrationTest {

    @Autowired
    private UserController userController;

    @Autowired
    private PortfolioController portfolioController;

    @Autowired
    private UserService userService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Qualifier("portfolioRepository")
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PositionService positionService;

    @Qualifier("positionRepository")
    @Autowired
    private PositionRepository positionRepository;
    /**
     * This Integration test tests the typical first time use case of our application. First a user gets created, then a user
     * creates a new portfolio and finally a user opens a position and logs off. This tests checks that every server request returns
     * the necessary and correct values to the client.
     * @throws Exception
     */
    @Test
    @Transactional
    void HappyTrail_FirstTimeUseCase_BigBangIntegrationTest() throws Exception {

        // First we create the input body of POST /users to create a new user
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");
        userPostDTO.setMail("testMail");

        // We perform the creation of the user according to the controller
        UserGetDTO createUserResult = userController.createUser(userPostDTO);

        // We confirm the values of returned body
        assertEquals("testUsername", createUserResult.getUsername());
        assertEquals(1L, createUserResult.getId());
        assertNotNull(createUserResult.getToken());
        assertEquals(UserStatus.ONLINE, createUserResult.getStatus());
        assertNotNull(createUserResult.getCreationDate());
        assertNotNull(createUserResult.getCollaboratingPortfolios());
        assertNotNull(createUserResult.getOwnedPortfolios());

        System.out.println("User creation successful");

        // With the user created we want to create a new shared Portfolio
        PortfolioPostDTO portfolioPostDTO = new PortfolioPostDTO();
        portfolioPostDTO.setName("testPortfolio");
        // Since the default visibility is shared it does not need to be set explicitly for the test.

        // Create the portfolio with the postDTO and the created users token for verification
        PortfolioGetDTO createPortfolioResult = portfolioController.createNewPortfolio(portfolioPostDTO, createUserResult.getToken());

        // Check general Portfolio Info
        assertEquals(1L, createPortfolioResult.getId());
        assertEquals("testPortfolio", createPortfolioResult.getName());
        assertEquals(createUserResult.getUsername(), createPortfolioResult.getOwner().getUsername());

        // Check collaboration features
        assertEquals(createUserResult.getUsername(), createPortfolioResult.getTraders().get(0).getUsername());
        assertEquals(PortfolioVisibility.SHARED, createPortfolioResult.getPortfolioVisibility());
        assertNotNull(createPortfolioResult.getJoinCode());

        // Check financial data
        assertEquals(BigDecimal.valueOf(100000), createPortfolioResult.getCash());
        assertEquals(BigDecimal.valueOf(0), createPortfolioResult.getCapital());
        assertEquals(BigDecimal.valueOf(100000), createPortfolioResult.getBalance());
        assertEquals(BigDecimal.valueOf(0), createPortfolioResult.getWeeklyPerformance());
        assertEquals(BigDecimal.valueOf(0), createPortfolioResult.getTotalPerformance());
        assertEquals(BigDecimal.valueOf(100000), createPortfolioResult.getTotValue());
        assertTrue(createPortfolioResult.getPositions().isEmpty());

        UserGetDTO getUserResult = userController.getUser("1");

        // Check if the portfolio has been assigned correctly to the user
        assertEquals(getUserResult.getOwnedPortfolios().get(0).getName(), createPortfolioResult.getName());

        System.out.println("Portfolio creation successful");

        // Now we open a long Apple position by buying 5 Apple stocks

        // First we create the request
        PositionPostDTO positionPostDTO = new PositionPostDTO("AAPL", BigDecimal.valueOf(5), PositionType.STOCK_LONG);

        // The request gets performed and the updated portfolio gets returned
        PortfolioGetDTO openPositionResult = portfolioController.openPosition(positionPostDTO, createPortfolioResult.getId(), createUserResult.getToken());

        // We check that the position has been added to the portfolio
        assertFalse(openPositionResult.getPositions().isEmpty());
        assertEquals("AAPL", openPositionResult.getPositions().get(0).getCode());

        // We extract the Position to be able to test the updated financial data
        PositionGetDTO testPositionGetDTO = openPositionResult.getPositions().get(0);

        // Check if the proper amount has been deducted from portfolio balance
        assertEquals(BigDecimal.valueOf(100000).subtract(testPositionGetDTO.getValue(), MathContext.DECIMAL32), openPositionResult.getBalance());

        System.out.println("Position opening successful");

        // The user logs out
        userController.logoutUser(getUserResult.getToken());

        // We retrieve the user from another users perspective
        UserGetDTO loggedOutUser = userController.getUser("1");

        // We check that the user gets displayed as OFFLINE
        assertEquals(UserStatus.OFFLINE, loggedOutUser.getStatus());

        System.out.println("Logout successful");
    }

}
