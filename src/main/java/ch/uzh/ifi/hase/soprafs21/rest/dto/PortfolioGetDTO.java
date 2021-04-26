package ch.uzh.ifi.hase.soprafs21.rest.dto;

import ch.uzh.ifi.hase.soprafs21.entity.Position;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.rest.mapper.DTOMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PortfolioGetDTO {
    Long id;
    String name;
    SmallUserDTO owner;
    List<SmallUserDTO> traders;
    BigDecimal cash;
    BigDecimal capital;
    BigDecimal totalValue;

public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    String joinCode;

    public List<PositionGetDTO> getPositions() {
        return positions;
    }

    // Takes the positions in the original portfolio and stores them in a list of DTOs
    public void setPositions(List<Position> positions) {
        List<PositionGetDTO> positionDTOs = new ArrayList<>();
        PositionGetDTO currentDTO;
        for (Position position : positions)
        {
            currentDTO = DTOMapper.INSTANCE.convertEntityToPositionGetDTO(position);
            // Performance is computed on the fly
            currentDTO.setPerformance();
            positionDTOs.add(currentDTO);
        }
        this.positions = positionDTOs;
    }

    List<PositionGetDTO> positions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SmallUserDTO> getTraders() {
        return traders;
    }

    public void setTraders(Set<User> traders) {
        List<SmallUserDTO> traderNames = new ArrayList<>();
        for (User trader : traders)
        {
            traderNames.add(
                    new SmallUserDTO(
                            trader.getId(),
                            trader.getUsername(),
                            trader.getStatus()
                    )
            );
        }
        this.traders = traderNames;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public SmallUserDTO getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = new SmallUserDTO(
                owner.getId(),
                owner.getUsername(),
                owner.getStatus()
        );
    }


}
