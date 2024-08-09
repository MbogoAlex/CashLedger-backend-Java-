package com.app.CashLedger.dao;

import com.app.CashLedger.models.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentDao {
    private final EntityManager entityManager;
    @Autowired
    public PaymentDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Payment makePayment(Payment payment) {
        entityManager.persist(payment);
        return payment;
    }

    public List<Payment> getLatestPayment(Integer userId) {
        TypedQuery<Payment> query = entityManager.createQuery("from Payment where userAccount.id = :userId order by paidAt desc", Payment.class);
        query.setParameter("userId", userId);
        query.setMaxResults(1);
        return query.getResultList();
    }
}
