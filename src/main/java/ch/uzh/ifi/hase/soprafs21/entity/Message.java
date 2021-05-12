package ch.uzh.ifi.hase.soprafs21.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="MESSAGE")
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long messageId;

    @Column
    private Date sentAt = new Date();

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

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Date getSentAt() {
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
