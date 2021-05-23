package ch.uzh.ifi.hase.soprafs21.controller;

import ch.uzh.ifi.hase.soprafs21.entity.*;
import ch.uzh.ifi.hase.soprafs21.rest.dto.*;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs21.service.ChatService;
import ch.uzh.ifi.hase.soprafs21.service.FinanceService;
import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;
import ch.uzh.ifi.hase.soprafs21.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;
    private final ChatService chatService;

    public PortfolioController(PortfolioService portfolioService, UserService userService, ChatService chatService)
    {
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.chatService = chatService;
    }

    @PostMapping("/portfolios")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PortfolioGetDTO createNewPortfolio(@RequestBody PortfolioPostDTO postDTO, @RequestHeader(value = "token") String token)
    {
        Portfolio portfolio = DTOMapper.INSTANCE.convertPortfolioPostDTOtoEntity(postDTO);

        // Get the owner based on the token of the request;
        // It will also throw an exception if the token is not associated to any user

        User owner = userService.getUserByToken(token);
        Set<User> traders = new HashSet<>();
        traders.add(owner);

        portfolio.setOwner(owner);
        portfolio.setTraders(traders);

        // This will complete the portfolio and save it in the repository
        portfolio = portfolioService.createPortfolio(portfolio);

        // This will update the user to include the new portfolio in its owned portfolios
        userService.addCreatedPortfolio(portfolio);

        // creates new chat associated with that portfolio
        chatService.createChat(portfolio.getId());

        PortfolioGetDTO portfolioDTO = portfolioService.makeGetDTO(portfolio);
        portfolioDTO.setJoinCode(portfolio.getPortfolioCode());
        return portfolioDTO;
    }

    /**
     * RETURNS ALL PUBLIC PORTFOLIOS
     * Use for leaderboard
     *
     * @param token
     * @return
     */
    @GetMapping("/portfolios")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PortfolioGetDTO> getPortfolios(@RequestHeader(value = "token") String token,
                                               @RequestHeader(value = "sort") String sorting
    )
    {

        List<Portfolio> portfolios = portfolioService.getSharedPortfolios();
        List<PortfolioGetDTO> portfolioGetDTOs = new ArrayList<>();

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
            if (sorting.compareTo("weekly") == 0)
            {
                // Sort by weekly performance
                portfolioGetDTOs.sort(
                        new Comparator<PortfolioGetDTO>() {
                            @Override
                            public int compare(PortfolioGetDTO o1, PortfolioGetDTO o2) {
                                return o1.getWeeklyPerformance().compareTo(o2.getWeeklyPerformance());
                            }
                        });
            }
            else if (sorting.compareTo("balance") == 0)
            {
                portfolioGetDTOs.sort(
                        new Comparator<PortfolioGetDTO>() {
                            @Override
                            public int compare(PortfolioGetDTO o1, PortfolioGetDTO o2) {
                                return o1.getBalance().compareTo(o2.getBalance());
                            }
                        });
            }
            else
            {
                // Sort by total performance
                portfolioGetDTOs.sort(
                        new Comparator<PortfolioGetDTO>() {
                            @Override
                            public int compare(PortfolioGetDTO o1, PortfolioGetDTO o2) {
                                return o1.getTotalPerformance().compareTo(o2.getTotalPerformance());
                        }
                });
            }
            return portfolioGetDTOs;
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
                                        @RequestHeader(value = "token") String token)
    {
        User user = userService.getUserByToken(token);
        Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
        PortfolioGetDTO portfolioDTO = portfolioService.makeGetDTO(portfolio);
        // If the user is a trader in the portfolio the joinCode is returned, otherwise it is not
        if (portfolio.getTraders().contains(user))
         {
            portfolioDTO.setJoinCode(portfolio.getPortfolioCode());
        }
        return portfolioDTO;
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

    @GetMapping("positions/{positionCode}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BigDecimal getPositionPrice(@PathVariable String positionCode,
                                       @RequestHeader(value = "token") String token)
    {
        // Even though we do not need a user, this will make sure
        // a valid token is being used.
        userService.getUserByToken(token);
        return FinanceService.getStockPrice(positionCode, "CHF");
    }

    @GetMapping("positions/{positionCode}/more")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, BigDecimal> getPositionInfo(@PathVariable String positionCode,
                                       @RequestHeader(value = "token") String token)
    {
        // Even though we do not need a user, this will make sure
        // a valid token is being used.
        userService.getUserByToken(token);
        return FinanceService.getStockInfo(positionCode, "CHF");
    }

    @PostMapping("/portfolios/{portfolioId}/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MessageContainerGetDTO sendMessage(@RequestBody MessagePostDTO messagePostDTO,
                                              @PathVariable Long portfolioId,
                                              @RequestHeader(value = "token") String token)
    {
        // validates User and gets sender by token
        User sender = userService.getUserByToken(token);

        // validates User belongs to portfolio
        portfolioService.validateRequest(portfolioService.findPortfolioById(portfolioId), token);

        Message newMessage = DTOMapper.INSTANCE.convertMessagePostDTOToEntity(messagePostDTO);
        newMessage.setSender(sender.getUsername());

        MessageContainer chat = chatService.sendMessage(portfolioId, newMessage);

        return DTOMapper.INSTANCE.convertEntityToMessageContainerDTO(chat);
    }

    @GetMapping("/portfolios/{portfolioId}/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MessageContainerGetDTO getChat( @PathVariable Long portfolioId,
                                            @RequestHeader(value = "token") String token)
    {
        // validates User
        userService.getUserByToken(token);

        // validates User belongs to portfolio
        portfolioService.validateRequest(portfolioService.findPortfolioById(portfolioId), token);

        MessageContainer chatHistory = chatService.getMessagesByPortfolioId(portfolioId);
        return DTOMapper.INSTANCE.convertEntityToMessageContainerDTO(chatHistory);
    }

}
