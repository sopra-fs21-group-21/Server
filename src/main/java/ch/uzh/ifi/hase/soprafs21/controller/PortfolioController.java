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
import org.springframework.web.server.ResponseStatusException;

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
        try {


        User owner = userService.getUserByToken(token);
        Set<User> traders = new HashSet<>();
        traders.add(owner);

        portfolio.setOwner(owner);
        portfolio.setTraders(traders);

        // This will complete the portfolio and save it in the repository
        portfolio = portfolioService.createPortfolio(portfolio);

        // This will update the user to include the new portfolio in its owned portfolios
        userService.addCreatedPortfolio(portfolio);

        PortfolioGetDTO portfolioDTO = portfolioService.makeGetDTO(portfolio);
        portfolioDTO.setJoinCode(portfolio.getPortfolioCode());
        return portfolioDTO;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }


    }

    @GetMapping("/portfolios")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PortfolioGetDTO> getPortfolios(@RequestHeader(value = "token") String token)
    {

        List<Portfolio> portfolios = portfolioService.getSharedPortfolios();
        List<PortfolioGetDTO> portfolioGetDTOs = new ArrayList<>();

        try {
            User user = userService.getUserByToken(token);
            PortfolioGetDTO currentDto;
            for (Portfolio portfolio : portfolios)
            {
                currentDto = portfolioService.makeGetDTO(portfolio);
                if (portfolio.getTraders().contains(user))
                {
                    currentDto.setJoinCode(portfolio.getPortfolioCode());
                }

                portfolioGetDTOs.add(currentDto);
            }
            return portfolioGetDTOs;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }


    }


    /**
     * ADD A TRADER TO AN EXISTING PORTFOLIO
     * the body will contain
     * code: String, the join code of the portfolio
     */
    @PutMapping("/portfolios")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public PortfolioGetDTO addTrader(@RequestHeader(value = "join_code") String code,
                                     @RequestHeader(value = "token") String token
    )
    {
        Portfolio portfolio;
        // Add trader to portfolio
        portfolio = portfolioService.addTraderToPortfolio(code, token);
        // Add portfolio to trader
        // Make sure you don't swap these statements as portfolioService will check that the user
        // is not a trader in the portfolio yet, but userService will not.
        userService.addPortfolioToUser(portfolio, token);

        return portfolioService.makeGetDTO(portfolio);
    }

    /**
     * GET INFORMATION FOR A SPECIFIC PORTFOLIO
     * If the request is made by a trader in the portfolio join code will be included,
     * otherwise it won't.
     */
    @GetMapping("portfolios/{portfolioId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PortfolioGetDTO getPortfolio(@PathVariable Long portfolioId,
                                        @RequestHeader(value = "token") String token
    )
    {
        try{
        User user = userService.getUserByToken(token);
        Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
        PortfolioGetDTO portfolioDTO = portfolioService.makeGetDTO(portfolio);
        // If the user is a trader in the portfolio the joinCode is returned, otherwise it is not
        if (portfolio.getTraders().contains(user))
         {
            portfolioDTO.setJoinCode(portfolio.getPortfolioCode());
        }
        return portfolioDTO;
    } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    /**
     * OPEN A POSITION IN A GIVEN PORTFOLIO
     * Returns the updated portfolioGetDTO
     *
     * The header will contain the token.
     *
     * The body will have:
     * code: String, code of the stock
     * amount: integer, how many stocks
     * type: String, position type, see the enumerated type
     *
     */
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
        return portfolioService.makeGetDTO(portfolio);
    }

    /**
     * CLOSE A POSITION IN A PORTFOLIO
     * Returns the updated portfolioGetDTO
     */
    @DeleteMapping("/portfolios/{portfolioId}/{positionId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PortfolioGetDTO closePosition(@PathVariable Long portfolioId,
                                         @PathVariable Long positionId,
                                         @RequestHeader(value = "token") String token
    )
    {
        Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
        portfolioService.validateRequest(portfolio, token);

        portfolio = portfolioService.closePosition(portfolioId, positionId);

        // we need to add capital and total value as they are not stored in the database
        return portfolioService.makeGetDTO(portfolio);
    }



}
