package com.app.CashLedger.dao;

import com.app.CashLedger.models.UserAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserAccountDao {
    private final EntityManager entityManager;
    @Autowired

    public UserAccountDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Transactional
    public UserAccount saveUser(UserAccount userAccount) {
        entityManager.persist(userAccount);
        return userAccount;
    }
    @Transactional
    public UserAccount updateUser(UserAccount userAccount) {
        UserAccount managedUserAccount = entityManager.merge(userAccount);
        return managedUserAccount;
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

    public List<UserAccount> filterUsers(String name, String phoneNumber, LocalDate startDate, LocalDate endDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> userAccount = cq.from(UserAccount.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            Predicate fnamePredicate = cb.like(cb.lower(userAccount.get("fname")), "%" + name.toLowerCase() + "%");
            Predicate lnamePredicate = cb.like(cb.lower(userAccount.get("lname")), "%" + name.toLowerCase() + "%");
            predicates.add(cb.or(fnamePredicate, lnamePredicate));
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            predicates.add(cb.like(cb.lower(userAccount.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%"));
        }

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(userAccount.get("createdAt"), startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1); // inclusive of the endDate
            predicates.add(cb.lessThanOrEqualTo(userAccount.get("createdAt"), endDateTime));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<UserAccount> query = entityManager.createQuery(cq);
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
