package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User getUserById(long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user exists"));
    }

    public User getUserByToken(String token)
    {
        return userRepository.findByToken(token)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token"));
    }

    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);

        Date currentDate = new Date();
        newUser.setCreationDate(currentDate);

        checkIfUserExists(newUser);

        // saves the given entity but data is only persisted in the database once flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }


    /**
     * This is a helper method that will check the uniqueness criteria of the username and the name
     * defined in the User entity. The method will do nothing if the input is unique and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */


    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        User userByMail = userRepository.findByMail(userToBeCreated.getMail());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null && userByMail != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username and e-mail", "are"));
        }
        else if (userByUsername != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
        }
        else if (userByMail != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "e-mail", "is"));
        }
    }

    //Logs in user by checking credentials and changing status to ONLINE
    public User logInUser(User userLoggingIn) {
        User userByUsername = userRepository.findByUsername(userLoggingIn.getUsername());
        if (userByUsername != null){
            if (userLoggingIn.getPassword().equals(userByUsername.getPassword())) {
                userByUsername.setStatus(UserStatus.ONLINE);
                userRepository.save(userByUsername);
                return userByUsername;
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong password");
            }
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user exists");
        }
    }

    //Changes Username/Password/Mail of a user using an id and a user object with the proposed changes
    public void modifyUser(User newUserData, Long userID, String token) {
        User userById = getUserById(userID);
        verifyUser(userById, token);
        if (newUserData.getUsername()!=null){
            if(userRepository.findByUsername(newUserData.getUsername())!=null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided username is already taken. Please choose another one.");
            }
            userById.setUsername(newUserData.getUsername());
            userRepository.save(userById);
        }
        else if(newUserData.getPassword()!=null){
            userById.setPassword(newUserData.getPassword());
            userRepository.save(userById);
        }
        else if(newUserData.getMail()!=null){
            userById.setMail(newUserData.getMail());
            userRepository.save(userById);
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nothing to modify");
        }
    }

    private void verifyUser(User userToVerify, String token){
        if (!userToVerify.getToken().equals(token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to execute this operation");
        }
    }

    public void logoutUser(String token){
        User userByToken = getUserByToken(token);
        userByToken.setStatus(UserStatus.OFFLINE);
        userRepository.save(userByToken);
    }


    // Needed when a new portfolio is created, to add it to the owned portfolios
    public void addCreatedPortfolio(Portfolio portfolio)
    {
        User updateUser = userRepository.getOne(
                portfolio.getOwner().getId()
        );
        updateUser.addOwnedPortfolio(portfolio);
        userRepository.save(updateUser);
    }

    public void addPortfolioToUser(Portfolio portfolio, String token)
    {
        User user = getUserByToken(token);
        Set<Portfolio> joinedPortfolios = user.getCollaboratingPortfolios();

        joinedPortfolios.add(portfolio);
        user.setCollaboratingPortfolios(joinedPortfolios);

        userRepository.saveAndFlush(user);
    }

}
