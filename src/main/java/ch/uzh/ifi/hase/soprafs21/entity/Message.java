package ch.uzh.ifi.hase.soprafs21.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Entity
@Table(name="MESSAGE")
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long messageId;

    @Column
    private String sentAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));

    @Column
    private String content;

    @Column
    private String sender;


    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public String getSentAt() {
        return sentAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
