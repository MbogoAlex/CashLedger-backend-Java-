package com.app.CashLedger.dao;

import com.app.CashLedger.dto.TransactionDto;
import com.app.CashLedger.models.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class TransactionDao {
    private final EntityManager entityManager;
    @Autowired
    public TransactionDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Transaction addTransaction(Transaction transaction) {
        entityManager.persist(transaction);
        return transaction;
    }

    public Transaction updateTransaction(Transaction transaction) {
        entityManager.merge(transaction);
        return transaction;
    }

    public Transaction getTransaction(Integer id) {
        TypedQuery<Transaction> query = entityManager.createQuery("from Transaction where id = :id", Transaction.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public List<Transaction> addTransactions(List<Transaction> transactions) {
        for(Transaction transaction : transactions) {
            entityManager.persist(transaction);
        }
        return transactions;
    }

    public List<Transaction> getTransactions(Integer userId, String entity) {
        TypedQuery<Transaction> query = entityManager.createQuery(
                "from Transaction where userAccount.id = :id and " +
                        "(:entity is null or " +
                        "((sender != :you and LOWER(sender) like concat('%', :entity, '%')) or " +
                        "(recipient != :you and LOWER(recipient) like concat('%', :entity, '%'))))",
                Transaction.class
        );
        query.setParameter("id", userId);
        query.setParameter("entity", entity.toLowerCase());
        query.setParameter("you", "You");
        return query.getResultList();
    }

    public List<Transaction> getGeneralTransactions(Integer userId, String startDate, String endDate) {
        TypedQuery<Transaction> query = entityManager.createQuery(
                "select t from Transaction t " +
                        "left join t.categories tc " +
                        "where t.userAccount.id = :id " +
                        "and (t.date >= :startDate) " +
                        "and (t.date <= :endDate) ",
                Transaction.class
        );
        query.setParameter("id", userId);
        query.setParameter("startDate",  LocalDate.parse(startDate));
        query.setParameter("endDate", LocalDate.parse(endDate));

        return query.getResultList();
    }

    public List<Transaction> getUserTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean latest, String startDate, String endDate) {
        String orderClause = latest ? "desc" : "asc";

        String hql = "select t from Transaction t " +
                "left join t.categories tc " +
                "left join tc.budgets b " +
                "where t.userAccount.id = :id and " +
                "(:entity is null or " +
                "((LOWER(t.sender) like concat('%', :entity, '%')) or " +
                "(LOWER(t.nickName) like concat('%', :entity, '%')) or " +
                "(LOWER(t.recipient) like concat('%', :entity, '%')))) " +
                "and (:categoryId is null or tc.id = :categoryId) " +
                "and (:budgetId is null or b.id = :budgetId) " +
                "and (:transactionType is null or LOWER(t.transactionType) = :transactionType) " +
                "and (t.date >= :startDate) " +
                "and (t.date <= :endDate) " +
                "order by t.date " + orderClause + ", t.time " + orderClause;

        TypedQuery<Transaction> query = entityManager.createQuery(hql, Transaction.class);
        query.setParameter("id", userId);
        if(entity == null) {
            query.setParameter("entity", "");
        } else if(entity.toLowerCase().equals("you")) {
            query.setParameter("entity", "");
        } else {
            query.setParameter("entity", entity.toLowerCase());
        }

        query.setParameter("categoryId", categoryId);
        query.setParameter("budgetId", budgetId);
        if(transactionType != null) {
            if(transactionType.isEmpty() || transactionType == null) {
                query.setParameter("transactionType", null);
            } else {
                query.setParameter("transactionType", transactionType.toLowerCase());
            }
        } else {
            query.setParameter("transactionType", null);
        }

        if(startDate == null) {
            query.setParameter("startDate",  LocalDate.parse("2000-03-06"));
        } else {
            query.setParameter("startDate",  LocalDate.parse(startDate));
        }

        if(endDate == null) {
            query.setParameter("endDate", LocalDate.now());
        } else {
            query.setParameter("endDate", LocalDate.parse(endDate));
        }

        return query.getResultList();
    }



    public List<Object[]> getUserTransactionsSorted(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean moneyIn, Boolean orderByAmount, Boolean ascendingOrder, String startDate, String endDate) {
        String orderClause;
        String orderItem;
        if(orderByAmount) {
            orderItem = "totalAmount";
        } else {
            orderItem = "times";
        }
        if(ascendingOrder) {
            orderClause = "asc";
        } else {
            orderClause = "desc";
        }
        String groupByClause;
        String searchQuery;
        if(moneyIn) {
            groupByClause = "sender";
            searchQuery = "t.transactionAmount > 0 ";
        } else {
            groupByClause = "recipient";
            searchQuery = "t.transactionAmount < 0 ";
        }
        String hql = "select t."+groupByClause+", t.nickName, t.transactionType, count(abs(t.transactionAmount)) as times, sum(abs(t.transactionAmount)) as totalAmount, sum(abs(t.transactionCost)) from Transaction t " +
                "left join t.categories tc " +
                "left join tc.budgets b " +
                "where t.userAccount.id = :id and " +
                "(:entity is null or " +
                "((LOWER(t.sender) like concat('%', :entity, '%')) or " +
                "(LOWER(t.nickName) like concat('%', :entity, '%')) or " +
                "(LOWER(t.recipient) like concat('%', :entity, '%')))) " +
                "and (:categoryId is null or tc.id = :categoryId) " +
                "and (:budgetId is null or tc.id = :budgetId) " +
                "and (:transactionType is null or LOWER(t.transactionType) = :transactionType) " +
                "and (t.date >= :startDate) " +
                "and (t.date <= :endDate) " +
                "and "+searchQuery+
                "group by t."+groupByClause+", t.nickName, t.transactionType " +  // Include t.nickName in GROUP BY
                "order by "+orderItem+" "+orderClause;

        TypedQuery<Object[]> query = entityManager.createQuery(hql, Object[].class);
        query.setParameter("id", userId);
        if(entity == null) {
            query.setParameter("entity", "");
        } else {
            query.setParameter("entity", entity.toLowerCase());
        }
        query.setParameter("categoryId", categoryId);
        query.setParameter("budgetId", budgetId);
        if(transactionType != null) {
            if(transactionType.isEmpty() || transactionType == null) {
                query.setParameter("transactionType", null);
            } else {
                query.setParameter("transactionType", transactionType.toLowerCase());
            }
        } else {
            query.setParameter("transactionType", null);
        }

        query.setParameter("startDate",  LocalDate.parse(startDate));
        query.setParameter("endDate", LocalDate.parse(endDate));

        return query.getResultList();
    }


    public List<Object[]> getUserTransactionsSortedByFrequency(Integer userId, String entity, Integer categoryId, String transactionType, Boolean moneyIn, Boolean ascendingOrder, String startDate, String endDate) {
        String orderClause;
        if(ascendingOrder) {
            orderClause = "asc";
        } else {
            orderClause = "desc";
        }
        String groupByClause;
        String searchQuery;
        if(moneyIn) {
            groupByClause = "sender";
            searchQuery = "t.sender != :you and LOWER(t.sender) like concat('%', :entity, '%')";
        } else {
            groupByClause = "recipient";
            searchQuery = "t.recipient != :you and LOWER(t.recipient) like concat('%', :entity, '%')";
        }
        String hql = "select t."+groupByClause+", count(abs(t.transactionAmount)) as times, sum(abs(t.transactionAmount)) as totalAmount, sum(abs(t.transactionCost)) from Transaction t " +
                "left join t.categories tc " +
                "where t.userAccount.id = :id and " +
                "(:entity is null or " +
                "(("+searchQuery+"))) " +
                "and (:categoryId is null or tc.id = :categoryId) " +
                "and (:transactionType is null or LOWER(t.transactionType) = :transactionType) " +
                "and (t.date >= :startDate) " +
                "and (t.date <= :endDate) " +
                "group by t."+groupByClause+" " +
                "order by times "+orderClause;

        TypedQuery<Object[]> query = entityManager.createQuery(hql, Object[].class);
        query.setParameter("id", userId);
        query.setParameter("entity", entity != null ? entity.toLowerCase() : null);
        query.setParameter("you", "You");
        query.setParameter("categoryId", categoryId);
        if(transactionType != null) {
            if(transactionType.isEmpty() || transactionType == null) {
                query.setParameter("transactionType", null);
            } else {
                query.setParameter("transactionType", transactionType.toLowerCase());
            }
        } else {
            query.setParameter("transactionType", null);
        }
        query.setParameter("startDate",  LocalDate.parse(startDate));
        query.setParameter("endDate", LocalDate.parse(endDate));
        return query.getResultList();
    }

    public Double getCurrentBalance(Integer userId) {
        TypedQuery<Transaction> query = entityManager.createQuery("from Transaction where userAccount.id = :id", Transaction.class);
        query.setParameter("id", userId);
        return query.getResultList().get(0).getBalance();
    }

    public List<Transaction> getExpenditure(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean moneyIn, Boolean latest, String startDate, String endDate) {
        String orderClause = latest ? "desc" : "asc";
        String searchQuery;
        if(moneyIn) {
//            searchQuery = "t.sender != :you and LOWER(t.sender) like concat('%', :entity, '%')";
            searchQuery = "t.transactionAmount > 0 ";
        } else {
//            searchQuery = "t.recipient != :you and LOWER(t.recipient) like concat('%', :entity, '%')";
            searchQuery = "t.transactionAmount < 0 ";
        }

        String hql = "from Transaction t " +
                "left join t.categories tc " +
                "left join tc.budgets b " +
                "where t.userAccount.id = :id and " +
                "(:entity is null or " +
                "((LOWER(t.sender) like concat('%', :entity, '%')) or " +
                "(LOWER(t.nickName) like concat('%', :entity, '%')) or " +
                "(LOWER(t.recipient) like concat('%', :entity, '%')))) " +
                "and (:categoryId is null or tc.id = :categoryId) " +
                "and (:budgetId is null or b.id = :budgetId) " +
                "and (:transactionType is null or LOWER(t.transactionType) = :transactionType) " +
                "and (t.date >= :startDate) " +
                "and (t.date <= :endDate) " +
                "and "+searchQuery+
                "order by t.date " + orderClause + ", t.time " + orderClause;

        TypedQuery<Transaction> query = entityManager.createQuery(hql, Transaction.class);
        query.setParameter("id", userId);
        query.setParameter("entity", entity.toLowerCase());
        query.setParameter("categoryId", categoryId);
        query.setParameter("budgetId", budgetId);
        if(transactionType != null) {
            if(transactionType.isEmpty() || transactionType == null) {
                query.setParameter("transactionType", null);
            } else {
                query.setParameter("transactionType", transactionType.toLowerCase());
            }
        } else {
            query.setParameter("transactionType", null);
        }
//        query.setParameter("you", "You");
//        query.setParameter("moneyIn", moneyIn);
        query.setParameter("startDate",  LocalDate.parse(startDate));
        query.setParameter("endDate", LocalDate.parse(endDate));

        return query.getResultList();
    }

    public List<Object[]> getGroupedTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) {
        String hql = "select t.date, " +
                "count(abs(t.transactionAmount)) as times, " +
                "sum(case when t.transactionAmount > 0 then abs(t.transactionAmount) else 0 end) as totalMoneyIn, " +
                "sum(case when t.transactionAmount < 0 then abs(t.transactionAmount) else 0 end) as totalMoneyOut, " +
                "sum(abs(t.transactionCost)) " +
                "from Transaction t " +
                "left join t.categories tc " +
                "left join tc.budgets b " +
                "where t.userAccount.id = :id and " +
                "(:entity is null or " +
                "((LOWER(t.sender) like concat('%', :entity, '%')) or " +
                "(LOWER(t.nickName) like concat('%', :entity, '%')) or " +
                "(LOWER(t.recipient) like concat('%', :entity, '%')))) " +
                "and (:categoryId is null or tc.id = :categoryId) " +
                "and (:budgetId is null or tc.id = :budgetId) " +
                "and (:transactionType is null or LOWER(t.transactionType) = :transactionType) " +
                "and (t.date >= :startDate) " +
                "and (t.date <= :endDate) " +
                "group by t.date " +
                "order by t.date desc";

        TypedQuery<Object[]> query = entityManager.createQuery(hql, Object[].class);
        query.setParameter("id", userId);
        query.setParameter("entity", entity == null ? "" : entity.toLowerCase());
        query.setParameter("categoryId", categoryId);
        query.setParameter("budgetId", budgetId);
        query.setParameter("transactionType", transactionType == null || transactionType.isEmpty() ? null : transactionType.toLowerCase());
        query.setParameter("startDate", LocalDate.parse(startDate));
        query.setParameter("endDate", LocalDate.parse(endDate));

        return query.getResultList();
    }



}
