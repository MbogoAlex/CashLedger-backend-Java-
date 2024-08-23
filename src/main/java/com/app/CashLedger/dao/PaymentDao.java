package com.app.CashLedger.dao;

import com.app.CashLedger.models.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        Integer paymentMonth = null;

        if (startDate != null) {
            startDateTime = LocalDate.parse(startDate).atStartOfDay();
        }

        if (endDate != null) {
            endDateTime = LocalDate.parse(endDate).plusDays(1).atStartOfDay().minusSeconds(1); // inclusive of the endDate
        }

        if (month != null) {
            paymentMonth = Month.valueOf(month.toUpperCase()).getValue(); // Convert the Month enum to its integer value
        }

        StringBuilder queryBuilder = new StringBuilder("from Payment p where 1=1"); // Start with a true condition

        if (name != null) {
            queryBuilder.append(" and (p.userAccount.fname = :name or p.userAccount.lname = :name)");
        }

        if (phoneNumber != null) {
            queryBuilder.append(" and p.userAccount.phoneNumber = :phoneNumber");
        }

        if (startDateTime != null) {
            queryBuilder.append(" and p.paidAt >= :startDateTime");
        }

        if (endDateTime != null) {
            queryBuilder.append(" and p.paidAt <= :endDateTime");
        }

        if (paymentMonth != null) {
            queryBuilder.append(" and EXTRACT(MONTH FROM p.paidAt) = :paymentMonth");
        }

        TypedQuery<Payment> query = entityManager.createQuery(queryBuilder.toString(), Payment.class);

        if (name != null) {
            query.setParameter("name", name);
        }

        if (phoneNumber != null) {
            query.setParameter("phoneNumber", phoneNumber);
        }

        if (startDateTime != null) {
            query.setParameter("startDateTime", startDateTime);
        }

        if (endDateTime != null) {
            query.setParameter("endDateTime", endDateTime);
        }

        if (paymentMonth != null) {
            query.setParameter("paymentMonth", paymentMonth);
        }

        return query.getResultList();
    }






    public List<Payment> getLatestPayment(Integer userId) {
        TypedQuery<Payment> query = entityManager.createQuery("from Payment where userAccount.id = :userId order by paidAt desc", Payment.class);
        query.setParameter("userId", userId);
        query.setMaxResults(1);
        return query.getResultList();
    }
}
