package com.app.CashLedger.dao;

import com.app.CashLedger.models.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PaymentDao {
    private final EntityManager entityManager;
    @Autowired
    public PaymentDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Transactional
    public Payment makePayment(Payment payment) {
        entityManager.persist(payment);
        return payment;
    }

    public List<Payment> getPayments(String name, String month, String phoneNumber, String startDate, String endDate) {
        TypedQuery<Payment> query = entityManager.createQuery("from Payment where name is null or " +
                "userAccount.fname = :name or " +
                "name is null or userAccount.lname = :name or " +
                "phoneNumber is null or userAccount.phoneNumber = :phoneNumber and " +
                "DATE(paidAt) >= :startDate and " +
                "DATE(paidAt) <= :startDate", Payment.class);

        return query.getResultList();
    }

    public List<Payment> getLatestPayment(Integer userId) {
        TypedQuery<Payment> query = entityManager.createQuery("from Payment where userAccount.id = :userId order by paidAt desc", Payment.class);
        query.setParameter("userId", userId);
        query.setMaxResults(1);
        return query.getResultList();
    }
}
