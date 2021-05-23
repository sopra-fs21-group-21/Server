package ch.uzh.ifi.hase.soprafs21.rest.mapper;

import ch.uzh.ifi.hase.soprafs21.entity.*;
import ch.uzh.ifi.hase.soprafs21.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g., UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "mail", target = "mail")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "mail", target = "mail")
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "ownedPortfolios", target = "ownedPortfolios")
    @Mapping(source = "collaboratingPortfolios", target = "collaboratingPortfolios")
    @Mapping(source = "creationDate", target = "creationDate")
    @Mapping(source = "mail", target = "mail")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "name", target = "portfolioName")
    Portfolio convertPortfolioPostDTOtoEntity(PortfolioPostDTO portfolioPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "portfolioName", target = "name")
    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "traders", target = "traders")
    @Mapping(source = "balance", target = "cash")
    @Mapping(source = "positions", target = "positions")
    PortfolioGetDTO convertEntityToPortfolioGetDTO(Portfolio portfolio);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "type", target = "type")
    Position convertPositionPostDTOtoEntity(PositionPostDTO positionPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "openingPrice", target = "openingPrice")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "value", target = "value")
    PositionGetDTO convertEntityToPositionGetDTO(Position position);

    @Mapping(source = "content", target = "content")
    Message convertMessagePostDTOToEntity(MessagePostDTO messagePostDTO);

    @Mapping(source = "portfolioId", target = "portfolioId")
    @Mapping(source = "messageList", target = "messageList")
    MessageContainerGetDTO convertEntityToMessageContainerDTO(MessageContainer messageContainer);

}
