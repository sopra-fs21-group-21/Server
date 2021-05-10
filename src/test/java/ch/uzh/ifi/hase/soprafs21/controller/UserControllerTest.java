package ch.uzh.ifi.hase.soprafs21.controller;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs21.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs21.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
        testUser.setToken("123");
        testUser.setStatus(UserStatus.OFFLINE);
        testUser.setCreationDate(new Date(1));
    }

    //Get Tests
    //GET all users
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {

        // given
        List<User> allUsers = Collections.singletonList(testUser);

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(userService.getAllUsers()).willReturn(allUsers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is(testUser.getUsername())))
                .andExpect(jsonPath("$[0].status", is(testUser.getStatus().toString())));
    }

    // GET user
    @Test
    public void givenUsers_whenGetUser_thenReturnUser() throws Exception {
        // given

        // this mocks the UserService -> we define above what the userService should return when getUser() is called
        given(userService.getUserById(1)).willReturn(testUser);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is(testUser.getStatus().toString())))
                .andExpect(jsonPath("$.ownedPortfolios").isArray())
                .andExpect(jsonPath("$.ownedPortfolios", hasSize(0)))
                .andExpect(jsonPath("$.collaboratingPortfolios").isArray())
                .andExpect(jsonPath("$.collaboratingPortfolios", hasSize(0)))
                .andExpect(jsonPath("$.creationDate", is("1970-01-01T00:00:00.001+00:00")))
                .andExpect(jsonPath("$.token", is(testUser.getToken())));
    }

    // GET user Error. No such user
    @Test
    public void givenUsers_whenGetUser_thenReturnError() throws Exception {
        ResponseStatusException exe = new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user exists");
        given(userService.getUserById(Mockito.anyLong())).willThrow(exe);

        MockHttpServletRequestBuilder getRequest = get("/users/123")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("404 NOT_FOUND \"No such user exists\"", result.getResolvedException().getMessage()));

    }

    // Post tests
    // POST create user
    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // new valid user data gets sent to the API
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");
        userPostDTO.setMail("testMail");

        // The creation of the user in the UserService will return the created User
        given(userService.createUser(Mockito.any())).willReturn(testUser);

        // when/then -> do the request
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // We validate that the user has been created according to the input and check if the generated values have been generated
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.status", is(testUser.getStatus().toString())))
                .andExpect(jsonPath("$.ownedPortfolios").isArray())
                .andExpect(jsonPath("$.ownedPortfolios", hasSize(0)))
                .andExpect(jsonPath("$.collaboratingPortfolios").isArray())
                .andExpect(jsonPath("$.collaboratingPortfolios", hasSize(0)))
                .andExpect(jsonPath("$.creationDate", is("1970-01-01T00:00:00.001+00:00")))
                .andExpect(jsonPath("$.token", is(testUser.getToken())));
    }

    // POST Same username. Conflict Error
    @Test
    public void createUser_invalidInput_conflict() throws Exception {
        // given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        ResponseStatusException exe = new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));

        given(userService.createUser(Mockito.any())).willThrow(exe);

        // when/then -> do the request + validate the result
        postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals("409 CONFLICT \"The username provided is not unique. Therefore, the user could not be created!\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    // Put tests
    // PUT modify user
    @Test
    public void modifyUser_validInput_usernameChanged() throws Exception{
        String url = "/users/1";

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newTestUsername");

        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE).header("token", "a1").content(asJsonString(userPutDTO))).andExpect(status().isNoContent());
    }

    // PUT modify user. No such user ERROR
    @Test
    public void modifyUser_invalidInput_throwNotFound() throws Exception{
        String url = "/users/1";

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newTestUsername");

        ResponseStatusException exe = new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user exists");
        doThrow(exe).when(userService).modifyUser(Mockito.any(), Mockito.any(), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.put(url).header("token", "a1")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(asJsonString(userPutDTO))).andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("404 NOT_FOUND \"No such user exists\"", result.getResolvedException().getMessage()));
    }

    //PUT log in user
    @Test
    public void logInUser_validInput_loggedIn() throws Exception{
        // Since the UserStatus is being changed to ONLINE this is a PUT mapping

        // We do not know the user before logging in, hence no specification (e.g. users/1)
        String url = "/users";

        // Setup the simulated user login data
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("testPassword");

        //Assume the login is successful and the testUser gets returned
        given(userService.logInUser(Mockito.any())).willReturn(testUser);

        //Check that the user is ONLINE and gets returned correctly
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(asJsonString(userPutDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.status", is(testUser.getStatus().toString())))
                .andExpect(jsonPath("$.ownedPortfolios").isArray())
                .andExpect(jsonPath("$.ownedPortfolios", hasSize(0)))
                .andExpect(jsonPath("$.collaboratingPortfolios").isArray())
                .andExpect(jsonPath("$.collaboratingPortfolios", hasSize(0)))
                .andExpect(jsonPath("$.creationDate", is("1970-01-01T00:00:00.001+00:00")))
                .andExpect(jsonPath("$.token", is(testUser.getToken())));
    }



    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     * @param object
     * @return string
     */
    public static String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The request body could not be created.%s", e.toString()));
        }
    }
}