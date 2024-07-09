package com.app.CashLedger.dao;

import com.app.CashLedger.models.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MessageDao {
    private final EntityManager entityManager;
    @Autowired
    public MessageDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Message addMessage(Message message) {
        entityManager.persist(message);
        return message;
    }

    public List<Message> addMessages(List<Message> messages) {
        for(Message message : messages) {
            entityManager.persist(message);
        }
        return messages;
    }

    public List<Message> getMessages() {
        TypedQuery<Message> query = entityManager.createQuery("from Message", Message.class);
        return query.getResultList();
    }

    public List<Message> getMessagesById(Integer id) {
        TypedQuery<Message> query = entityManager.createQuery("from Message where userAccount.id = :id", Message.class);
        query.setParameter("id", id);
        return query.getResultList();
    }
}
