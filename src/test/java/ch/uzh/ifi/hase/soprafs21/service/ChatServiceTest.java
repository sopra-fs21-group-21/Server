package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.entity.Message;
import ch.uzh.ifi.hase.soprafs21.entity.MessageContainer;
import ch.uzh.ifi.hase.soprafs21.repository.MessageContainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChatServiceTest {

    @Mock
    MessageContainerRepository messageContainerRepository;

    @InjectMocks
    private ChatService chatService;

    private Message testMessage;
    private MessageContainer testMessageContainer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testMessage = new Message();
        testMessage.setContent("Hello World!");

        testMessageContainer = new MessageContainer();
        testMessageContainer.setPortfolioId(1L);

        // when -> any object is being saved in the userRepository -> return the dummy testUser
        Mockito.when(messageContainerRepository.saveAndFlush(Mockito.any())).thenReturn(testMessageContainer);
    }

    @Test
    public void sendMessage_validInput_success() {

        Mockito.when(messageContainerRepository.findByPortfolioId(1L)).thenReturn(java.util.Optional.ofNullable(testMessageContainer));

        MessageContainer testPortfolioChat = chatService.sendMessage(1L, testMessage);

        assertEquals(1L, testPortfolioChat.getPortfolioId());
        assertEquals(testMessageContainer.getMessageList(), testPortfolioChat.getMessageList());
    }

    @Test
    public void sendMessage_wrongPortfolioId_throwsException() {
        Mockito.when(messageContainerRepository.findByPortfolioId(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResponseStatusException.class, () -> chatService.sendMessage(1L, testMessage));
    }
}
