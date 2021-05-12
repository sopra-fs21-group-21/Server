package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.entity.Message;
import ch.uzh.ifi.hase.soprafs21.entity.MessageContainer;
import ch.uzh.ifi.hase.soprafs21.repository.MessageContainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChatService {

    private final MessageContainerRepository messageContainerRepository;

    @Autowired
    public ChatService(@Qualifier("messageContainerRepository") MessageContainerRepository messageContainerRepository) {
        this.messageContainerRepository = messageContainerRepository;
    }

    public MessageContainer sendMessage(long portfolioId, Message message){
        MessageContainer portfolioChat = getMessagesByPortfolioId(portfolioId);
        portfolioChat.addMessage(message);
        portfolioChat = messageContainerRepository.saveAndFlush(portfolioChat);
        return portfolioChat;
    }

    public MessageContainer getMessagesByPortfolioId(long portfolioId){
        return messageContainerRepository.findByPortfolioId(portfolioId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST, "No chat for this portfolio"));
    }

    public void createChat(long portfolioId){
        MessageContainer newChat = new MessageContainer();
        newChat.setPortfolioId(portfolioId);

        messageContainerRepository.save(newChat);
        messageContainerRepository.flush();
    }

}
