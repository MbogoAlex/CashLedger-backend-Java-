package com.app.CashLedger.dao;

import com.app.CashLedger.models.UserAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserAccountDao {
    private final EntityManager entityManager;
    @Autowired

    public UserAccountDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public UserAccount saveUser(UserAccount userAccount) {
        entityManager.persist(userAccount);
        return userAccount;
    }

    public UserAccount updateUser(UserAccount userAccount) {
        entityManager.merge(userAccount);
        return userAccount;
    }

    public UserAccount getUser(Integer id) {
        TypedQuery<UserAccount> query = entityManager.createQuery("from UserAccount where id = :id", UserAccount.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public List<UserAccount> getUsers() {
        TypedQuery<UserAccount> query = entityManager.createQuery("from UserAccount", UserAccount.class);
        return query.getResultList();
    }

    public UserAccount findByEmail(String email) {
        TypedQuery<UserAccount> query = entityManager.createQuery("from UserAccount where email = :email", UserAccount.class);
        query.setParameter("email", email);
        return query.getSingleResult();
    }

    public UserAccount findByPhoneNumber(String phoneNumber) {
        TypedQuery<UserAccount> query = entityManager.createQuery("from UserAccount where phoneNumber = :phoneNumber", UserAccount.class);
        query.setParameter("phoneNumber", phoneNumber);
        return query.getSingleResult();
    }

    public Boolean existsByEmail(String email) {
        TypedQuery<UserAccount> query = entityManager.createQuery("from UserAccount where email = :email", UserAccount.class);
        query.setParameter("email", email);
        List<UserAccount> results = query.getResultList();
        return !results.isEmpty();
    }

    public Boolean existsByPhoneNumber(String phoneNumber) {
        TypedQuery<UserAccount> query = entityManager.createQuery("from UserAccount where phoneNumber = :phoneNumber", UserAccount.class);
        query.setParameter("phoneNumber", phoneNumber);
        List<UserAccount> results = query.getResultList();
        return !results.isEmpty();
    }
}
