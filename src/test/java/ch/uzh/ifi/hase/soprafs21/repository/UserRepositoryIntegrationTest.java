package ch.uzh.ifi.hase.soprafs21.repository;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@DataJpaTest
//public class UserRepositoryIntegrationTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    public void findByUsername_success() {
//        // given
//        Date currentDate = new Date();
//        User user = new User();
//        user.setUsername("firstname@lastname");
//        user.setStatus(UserStatus.OFFLINE);
//        user.setToken("1");
//        user.setMail("mail@mail.com");
//        user.setPassword("***");
//        user.setCreationDate(currentDate);
//
//        entityManager.persist(user);
//        entityManager.flush();
//
//        // when
//        User found = userRepository.findByUsername(user.getUsername());
//
//        // then
//        assertNotNull(found.getId());
//        assertEquals(found.getUsername(), user.getUsername());
//        assertEquals(found.getToken(), user.getToken());
//        assertEquals(found.getStatus(), user.getStatus());
//        assertEquals(found.getCreationDate(), user.getCreationDate());
//        assertEquals(found.getMail(), user.getMail());
//        assertEquals(found.getPassword(), user.getPassword());
//    }
//}
