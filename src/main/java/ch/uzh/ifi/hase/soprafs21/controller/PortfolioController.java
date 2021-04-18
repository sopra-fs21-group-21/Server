package ch.uzh.ifi.hase.soprafs21.controller;

import ch.uzh.ifi.hase.soprafs21.constant.PortfolioVisibility;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioGetDTO;
import ch.uzh.ifi.hase.soprafs21.rest.dto.PortfolioPostDTO;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs21.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService)
    {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/portfolios")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PortfolioGetDTO createNewPortfolio(@RequestBody PortfolioPostDTO postDTO)
    {
        Portfolio portfolio = DTOMapper.INSTANCE.convertPortfolioPostDTOtoEntity(postDTO);
        if (portfolio.getPortfolioVisibility() == null)
        {
            // DEFAULT VISIBILITY
            portfolio.setPortfolioVisibility(PortfolioVisibility.SHARED);
        }
        portfolio = portfolioService.createPortfolio(portfolio);
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
        }
        return portfolioGetDTOs;
    }

}
