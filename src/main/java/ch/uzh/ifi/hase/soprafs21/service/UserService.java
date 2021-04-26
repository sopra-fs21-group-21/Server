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

import javax.persistence.EntityNotFoundException;
import java.util.*;

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

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getUser(long id){
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()){
            return user.get();
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user exists");
        }
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

    public User getUserByToken(String token)
    {
        User user = userRepository.findByToken(token);
        if (user != null)
        {
            return user;
        }
        throw new EntityNotFoundException("Invalid token");
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
    public void modifyUser(User newUserData, Long userID) {
        User userById = getUser(userID);
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

        userRepository.saveAndFlush(user);
    }

}
