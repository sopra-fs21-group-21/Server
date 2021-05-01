package ch.uzh.ifi.hase.soprafs21.controller;

import ch.uzh.ifi.hase.soprafs21.constant.PositionType;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioGetDTO;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioPostDTO;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PositionPostDTO;
import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;
import ch.uzh.ifi.hase.soprafs21.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static ch.uzh.ifi.hase.soprafs21.controller.UserControllerTest.asJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This class tests the REST endpoints for accessing portfolios.
 * Not that only the status for valid/invalid input is checked, as
 * checking the returning values is not feasible due to the fact that they are normally computed
 * with complex operations in other classes that here are present as mocks (PositionService, PortfolioService).
 * The return values are extensively checked in PositionServiceTest and PortfolioServiceTest.
 */

@WebMvcTest(PortfolioController.class)
public class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private UserService userService;


    @Test
    public void postPortfolioNoToken_throws_exception() throws Exception {

        PortfolioPostDTO postDTO = new PortfolioPostDTO();
        postDTO.setName("test");

        MockHttpServletRequestBuilder request = post("/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postPortfolioNoName_throws_exception() throws Exception {

        PortfolioPostDTO postDTO = new PortfolioPostDTO();

        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setToken("token");

        Mockito.doReturn(testUser).when(userService).getUserByToken(testUser.getToken());

        MockHttpServletRequestBuilder request = post("/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken())
                .content(asJsonString(postDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postPortfolio_ValidInput_isCreated() throws Exception
    {
        PortfolioPostDTO postDTO = new PortfolioPostDTO();
        postDTO.setName("portfolio1");

        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setToken("token");

        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setPortfolioName(postDTO.getName());

        Mockito.doReturn(testUser).when(userService).getUserByToken(testUser.getToken());
        Mockito.doReturn(testPortfolio).when(portfolioService).createPortfolio(Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());

        MockHttpServletRequestBuilder request = post("/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken())
                .content(asJsonString(postDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

    }

    @Test
    public void getPortfolios_noToken_throwsException() throws Exception {
        MockHttpServletRequestBuilder request = get("/portfolios");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getPortfolios_validToken_isOK() throws Exception {

        List<Portfolio> portfolioList = new ArrayList<>();
        portfolioList.add(new Portfolio());
        portfolioList.add(new Portfolio());
        portfolioList.add(new Portfolio());

        User testUser = new User();
        testUser.setToken("token");

        Mockito.doReturn(portfolioList).when(portfolioService).getSharedPortfolios();
        Mockito.doReturn(testUser).when(userService).getUserByToken(testUser.getToken());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());

        MockHttpServletRequestBuilder request = get("/portfolios")
                .header("token", testUser.getToken());

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void putPortfolio_validInput_isAccepted() throws Exception
    {
        Portfolio updatedPortfolio = new Portfolio();
        User testUser = new User();
        testUser.setUsername("franco");
        updatedPortfolio.getTraders().add(new User());

        Mockito.doReturn(updatedPortfolio).when(portfolioService).addTraderToPortfolio(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(userService).addPortfolioToUser(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO())
                .when(portfolioService).makeGetDTO(Mockito.any());

        MockHttpServletRequestBuilder request = put("/portfolios")
                .header("join_code", "join")
                .header("token", "token");

        mockMvc.perform(request)
                .andExpect(status().isAccepted());
    }

    @Test
    public void putPortfolio_noToken_throwsException() throws Exception
    {
        Portfolio updatedPortfolio = new Portfolio();
        User testUser = new User();
        testUser.setUsername("franco");
        updatedPortfolio.getTraders().add(new User());

        Mockito.doReturn(updatedPortfolio).when(portfolioService).addTraderToPortfolio(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(userService).addPortfolioToUser(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO())
                .when(portfolioService).makeGetDTO(Mockito.any());

        MockHttpServletRequestBuilder request = put("/portfolios")
                .header("join_code", "join");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getPortfolio_validInput_returnsPortfolio() throws Exception
    {
        MockHttpServletRequestBuilder request = get("/portfolios/1")
                .header("token", "token");

        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());
        User testUser = new User();

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doReturn(testUser).when(userService).getUserByToken(Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void getPortfolio_noToken_throwsException() throws Exception{
        MockHttpServletRequestBuilder request = get("/portfolios/1");

        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());
        User testUser = new User();

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doReturn(testUser).when(userService).getUserByToken(Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void openPosition_validInput_isCreated() throws Exception {
        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doNothing().when(portfolioService).validateRequest(Mockito.any(), Mockito.any());
        Mockito.doReturn(testPortfolio).when(portfolioService).openPosition(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());

        PositionPostDTO positionPostDTO = new PositionPostDTO(
                "AAPL",
                BigDecimal.valueOf(10),
                PositionType.STOCK_LONG);

        MockHttpServletRequestBuilder request = post("/portfolios/1")
                .header("token", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(positionPostDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());
    }

    @Test
    public void openPosition_noToken_throwsException() throws Exception {
        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doNothing().when(portfolioService).validateRequest(Mockito.any(), Mockito.any());
        Mockito.doReturn(testPortfolio).when(portfolioService).openPosition(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());

        PositionPostDTO positionPostDTO = new PositionPostDTO(
                "AAPL",
                BigDecimal.valueOf(10),
                PositionType.STOCK_LONG);

        MockHttpServletRequestBuilder request = post("/portfolios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(positionPostDTO));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void closePosition_validInput_isOK() throws Exception {
        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doNothing().when(portfolioService).validateRequest(Mockito.any(), Mockito.any());
        Mockito.doReturn(testPortfolio).when(portfolioService).closePosition(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());


        MockHttpServletRequestBuilder request = delete("/portfolios/1/1")
                .header("token", "token");

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void closePosition_noToken_throwsException() throws Exception {
        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doNothing().when(portfolioService).validateRequest(Mockito.any(), Mockito.any());
        Mockito.doReturn(testPortfolio).when(portfolioService).closePosition(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());


        MockHttpServletRequestBuilder request = delete("/portfolios/1/1");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void closePosition_missingPathVariable_throwsException() throws Exception {
        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setTraders(new HashSet<>());

        Mockito.doReturn(testPortfolio).when(portfolioService).findPortfolioById(Mockito.any());
        Mockito.doNothing().when(portfolioService).validateRequest(Mockito.any(), Mockito.any());
        Mockito.doReturn(testPortfolio).when(portfolioService).closePosition(Mockito.any(), Mockito.any());
        Mockito.doReturn(new PortfolioGetDTO()).when(portfolioService).makeGetDTO(Mockito.any());


        MockHttpServletRequestBuilder request = delete("/portfolios/1");

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

}
