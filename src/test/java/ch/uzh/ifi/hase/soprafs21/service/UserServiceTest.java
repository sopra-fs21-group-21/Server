package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setMail("test@mail.com");
        testUser.setPassword("***");

        // when -> any object is being saved in the userRepository -> return the dummy testUser
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    @Test
    public void createUser_validInputs_success() {
        // when -> any object is being save in the userRepository -> return the dummy testUser
        User createdUser = userService.createUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // given -> a first user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
        Mockito.when(userRepository.findByMail(Mockito.any())).thenReturn(null);

        // then -> attempt to create second user with same username -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void createUser_duplicateMail_throwsException() {
        // given -> a first user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
        Mockito.when(userRepository.findByMail(Mockito.any())).thenReturn(testUser);

        // then -> attempt to create second user with same mail -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void getUser_validInput_success() {
        // given -> the wanted user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.ofNullable(testUser));

        // then -> get user with id 1 -> check that correct user gets returned
        assertEquals(testUser, userService.getUserById(1));
    }

    @Test
    public void getUser_noUserWithId_throwsException() {
        // given no user with id 2

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());

        // then -> attempt to get user by id that does not exist -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> userService.getUserById(2));
    }

    @Test
    public void logInUser_validInput_success() {
        // given -> the user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
        User loggedInUser = userService.logInUser(testUser);

        // then -> attempt to log in the user -> check if the correct user has been logged in and that his status is ONLINE
        assertEquals(testUser, loggedInUser);
        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    }

    @Test
    public void logInUser_wrongPassword_throwsException() {
        // given -> a user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        User wrongPasswordUser = new User();
        wrongPasswordUser.setUsername("testUsername");
        wrongPasswordUser.setPassword("*****");

        // then -> attempt to log into testUser with wrong password -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> userService.logInUser(wrongPasswordUser));
    }

    @Test
    public void logInUser_NoSuchUser_throwsException() {
        // given -> testUser data

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

        // then -> attempt to log in user that does not exist -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> userService.logInUser(testUser));
    }

    /** The proposed changes to a user are sent in an User Object.
     * The field of the User object are the proposed changes.
     * E.g. To change the username a user object with only the username field gets sent to the method.
     */

    @Test
    public void modifyUser_validInputUsername_success() {
        // given -> a user has already been created
        User userToModify = userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.ofNullable(testUser));

        User newUsernameUser = new User();
        newUsernameUser.setUsername("newTestUsername");

        userService.modifyUser(newUsernameUser, 1L, userToModify.getToken());

        // then -> attempt to change username to new username -> check that the username has been changed
        assertEquals(newUsernameUser.getUsername(), testUser.getUsername());
    }

    @Test
    public void modifyUser_validInputPassword_success() {
        // given -> a user has already been created
        User userToModify = userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.ofNullable(testUser));

        User newPasswordUser = new User();
        newPasswordUser.setPassword("newTestPassword");

        userService.modifyUser(newPasswordUser, 1L, userToModify.getToken());

        // then -> attempt to change password to new password -> check that the password has been changed
        assertEquals(newPasswordUser.getPassword(), testUser.getPassword());
    }

    @Test
    public void modifyUser_validInputMail_success() {
        // given -> a first has already been created
        User userToModify = userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.ofNullable(testUser));

        User newMailUser = new User();
        newMailUser.setMail("newTestMail");

        userService.modifyUser(newMailUser, 1L, userToModify.getToken());

        // then -> attempt to change mail to new mail -> check that the mail has been changed
        assertEquals(newMailUser.getMail(), testUser.getMail());
    }

    @Test
    public void modifyUser_nullUser_throwsException() {
        // given -> a user has already been created
        User userToModify = userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.ofNullable(testUser));

        User newEmptyUser = new User();

        // then -> attempt to change user using an empty user object -> check that an error is thrown
        String usedToken = userToModify.getToken();
        assertThrows(ResponseStatusException.class, () -> userService.modifyUser(newEmptyUser, 1L, usedToken));
    }
}