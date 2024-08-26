package com.app.CashLedger.dao;

import com.app.CashLedger.models.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payment> cq = cb.createQuery(Payment.class);
        Root<Payment> payment = cq.from(Payment.class);

        List<Predicate> predicates = new ArrayList<>();

        // Handle name filter
        if (name != null && !name.isEmpty()) {
            Predicate fnamePredicate = cb.equal(cb.lower(payment.get("userAccount").get("fname")), name.toLowerCase());
            Predicate lnamePredicate = cb.equal(cb.lower(payment.get("userAccount").get("lname")), name.toLowerCase());
            predicates.add(cb.or(fnamePredicate, lnamePredicate));
        }

        // Handle phone number filter
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            predicates.add(cb.equal(cb.lower(payment.get("userAccount").get("phoneNumber")), phoneNumber.toLowerCase()));
        }

        // Handle date range filter
        if (startDate != null) {
            LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(payment.get("paidAt"), startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = LocalDate.parse(endDate).plusDays(1).atStartOfDay().minusSeconds(1); // inclusive of the endDate
            predicates.add(cb.lessThanOrEqualTo(payment.get("paidAt"), endDateTime));
        }

        // Handle month filter
        if (month != null && !month.isEmpty()) {
            Integer paymentMonth = Month.valueOf(month.toUpperCase()).getValue();
            predicates.add(cb.equal(cb.function("MONTH", Integer.class, payment.get("paidAt")), paymentMonth));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Payment> query = entityManager.createQuery(cq);


        return query.getResultList();
    }


    public List<Payment> getUserPayments(Integer userId, String startDate, String endDate) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        Integer paymentMonth = null;

        if (startDate != null) {
            startDateTime = LocalDate.parse(startDate).atStartOfDay();
        }

        if (endDate != null) {
            endDateTime = LocalDate.parse(endDate).plusDays(1).atStartOfDay().minusSeconds(1); // inclusive of the endDate
        }


        StringBuilder queryBuilder = new StringBuilder("from Payment p where 1=1"); // Start with a true condition

        if(userId != null) {
            queryBuilder.append(" and p.userAccount.id = :userId");
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


        if (startDateTime != null) {
            query.setParameter("startDateTime", startDateTime);
        }

        if (endDateTime != null) {
            query.setParameter("endDateTime", endDateTime);
        }

        if (paymentMonth != null) {
            query.setParameter("paymentMonth", paymentMonth);
        }

        query.setParameter("userId", userId);

        return query.getResultList();
    }






    public List<Payment> getLatestPayment(Integer userId) {
        TypedQuery<Payment> query = entityManager.createQuery("from Payment where userAccount.id = :userId order by paidAt desc", Payment.class);
        query.setParameter("userId", userId);
        query.setMaxResults(1);
        return query.getResultList();
    }
}
