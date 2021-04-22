package ch.uzh.ifi.hase.soprafs21.controller;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioGetDTO;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioPostDTO;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PositionPostDTO;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;
import ch.uzh.ifi.hase.soprafs21.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Port;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    public PortfolioController(PortfolioService portfolioService, UserService userService)
    {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @PostMapping("/portfolios")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PortfolioGetDTO createNewPortfolio(@RequestBody PortfolioPostDTO postDTO, @RequestHeader(value = "token") String token)
    {
        Portfolio portfolio = DTOMapper.INSTANCE.convertPortfolioPostDTOtoEntity(postDTO);
        if (portfolio.getPortfolioVisibility() == null)
        {
            // DEFAULT VISIBILITY
            portfolio.setPortfolioVisibility(PortfolioVisibility.SHARED);
        }
        // Get the owner based on the token of the request;
        // It will also throw an exception if the token is not associated to any user
        User owner = userService.getUserByToken(token);
        Set<User> traders = new HashSet<User>();
        traders.add(owner);

        portfolio.setOwner(owner);
        portfolio.setTraders(traders);

        // This will complete the portfolio and save it in the repository
        portfolio = portfolioService.createPortfolio(portfolio);

        // This will update the user to include the new portfolio in its owned portfolios
        userService.addCreatedPortfolio(portfolio);

        return DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio);
    }

    @GetMapping("/portfolios")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PortfolioGetDTO> getPortfolios()
    {
        List<Portfolio> portfolios = portfolioService.getSharedPortfolios();
        List<PortfolioGetDTO> portfolioGetDTOs = new ArrayList<>();

        PortfolioGetDTO currentDto;
        for (Portfolio portfolio : portfolios)
        {
            currentDto = DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio);
            currentDto.setCapital(portfolioService.getCapital(portfolio.getId()));
            currentDto.setTotalValue(portfolioService.getTotalValue(portfolio.getId()));
            portfolioGetDTOs.add(currentDto);
        }
        return portfolioGetDTOs;
    }

    @PostMapping("/portfolios/{portfolioId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PortfolioGetDTO openPosition(@RequestBody PositionPostDTO positionPostDTO,
                                        @PathVariable Long portfolioId,
                                        @RequestHeader(value = "token") String token)
    {
        Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
        portfolioService.validateRequest(portfolio, token);

        Position position = DTOMapper.INSTANCE.convertPositionPostDTOtoEntity(positionPostDTO);
        portfolio = portfolioService.openPosition(portfolioId, position);

        // we need to add capital and total value as they are not stored in the database
        PortfolioGetDTO portfolioDTO = DTOMapper.INSTANCE.convertEntityToPortfolioGetDTO(portfolio);
        portfolioDTO.setCapital(
                portfolioService.getCapital(portfolioId)
        );
        portfolioDTO.setTotalValue(
                portfolioService.getTotalValue(portfolioId)
        );
        return portfolioDTO;
    }

}
