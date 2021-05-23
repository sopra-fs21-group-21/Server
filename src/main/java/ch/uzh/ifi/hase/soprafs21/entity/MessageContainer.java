package ch.uzh.ifi.hase.soprafs21.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="MESSAGECONTAINER")
public class MessageContainer {

    @Id
    private Long portfolioId;

    @OneToMany(cascade=CascadeType.ALL)
    private List<Message> messageList = new ArrayList<>();

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setPortfolioId(Long chatId) {
        this.portfolioId = chatId;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void addMessage(Message message){
        messageList.add(message);
    }


}
