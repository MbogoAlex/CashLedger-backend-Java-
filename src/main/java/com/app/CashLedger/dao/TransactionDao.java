package com.app.CashLedger.dao;

import com.app.CashLedger.dto.TransactionDto;
import com.app.CashLedger.models.Budget;
import com.app.CashLedger.models.Transaction;
import com.app.CashLedger.models.TransactionCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Transaction> getAllTransactions(Integer userId){
        TypedQuery<Transaction> query = entityManager.createQuery("from Transaction where userAccount.id = :id order by date desc", Transaction.class);
        query.setParameter("id", userId);
        return query.getResultList();
    }

    public List<String> getExistingTransactionCodes(Integer userId) {
        TypedQuery<String> query = entityManager.createQuery("select t.transactionCode from Transaction t where t.userAccount.id = :userId order by t.date desc", String.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<String> getLatestTransactionCode(Integer userId) {
        TypedQuery<String> query = entityManager.createQuery("select t.transactionCode from Transaction t where t.userAccount.id = :userId order by t.date desc", String.class);
        query.setParameter("userId", userId);
        query.setMaxResults(1);
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
        String hql = "select t."+groupByClause+", " +
                "t.nickName, t.transactionType, " +
                "count(abs(t.transactionAmount)) as times, " +
                "sum(abs(t.transactionAmount)) as totalAmount, " +
                "sum(abs(t.transactionCost)) from Transaction t " +
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
        TypedQuery<Transaction> query = entityManager.createQuery("from Transaction where userAccount.id = :id order by date desc", Transaction.class);
        query.setParameter("id", userId);
        Double balance = 0.0;
        try {
            balance = query.getResultList().get(0).getBalance();
        } catch (Exception e) {

        }
        return balance;
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

    public List<Object[]> getGroupedByDateTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) {
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

    public List<Object[]> getGroupedByMonthAndYearTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String monthName, String year) {
        // Convert month name to number
        int month = Month.valueOf(monthName.toUpperCase()).getValue();

        // Log parameters for debugging
        System.out.println("Query Parameters:");
        System.out.println("User ID: " + userId);
        System.out.println("Entity: " + entity);
        System.out.println("Category ID: " + categoryId);
        System.out.println("Budget ID: " + budgetId);
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Month: " + monthName + " (" + month + ")");
        System.out.println("Year: " + year);

        String hql = "select t.date, " +
                "TO_CHAR(t.date, 'Month') as month, " +
                "TO_CHAR(t.date, 'YYYY') as year, " +
                "count(abs(t.transactionAmount)) as times, " +
                "sum(case when t.transactionAmount > 0 then abs(t.transactionAmount) else 0 end) as totalMoneyIn, " +
                "sum(case when t.transactionAmount < 0 then abs(t.transactionAmount) else 0 end) as totalMoneyOut, " +
                "sum(abs(t.transactionCost)) as totalCost " +
                "from Transaction t " +
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
                "and EXTRACT(MONTH FROM t.date) = :month " +
                "and EXTRACT(YEAR FROM t.date) = :year " +
                "group by t.date, TO_CHAR(t.date, 'Month'), TO_CHAR(t.date, 'YYYY') " +
                "order by t.date desc";

        TypedQuery<Object[]> query = entityManager.createQuery(hql, Object[].class);
        query.setParameter("id", userId);
        query.setParameter("entity", entity == null ? "" : entity.toLowerCase());
        query.setParameter("categoryId", categoryId);
        query.setParameter("budgetId", budgetId);
        query.setParameter("transactionType", transactionType == null || transactionType.isEmpty() ? null : transactionType.toLowerCase());
        query.setParameter("month", month); // Using the converted month number
        query.setParameter("year", Integer.parseInt(year)); // Converting year to integer

        List<Object[]> results = query.getResultList();

        // Log results for debugging
        System.out.println("Results Size: " + results.size());
        for (Object[] result : results) {
            System.out.println("Date: " + result[0] + ", Month: " + result[1] + ", Year: " + result[2] + ", Times: " + result[3] + ", Total Money In: " + result[4] + ", Total Money Out: " + result[5] + ", Total Cost: " + result[6]);
        }

        return results;
    }






    public List<Object[]> getGroupedByEntityTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) {
        String hql = "select " +
                "t.nickName, " +
                "lower(t.transactionType), " +
                "coalesce(t.entity, '') as entity, " +
                "count(t) as times, " +
                "count(case when t.transactionAmount > 0 then 1 end) as timesIn, " +
                "count(case when t.transactionAmount < 0 then 1 end) as timesOut, " +
                "sum(case when t.transactionAmount > 0 then t.transactionAmount else 0 end) as totalIn, " +
                "sum(case when t.transactionAmount < 0 then abs(t.transactionAmount) else 0 end) as totalOut, " +
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
                "and (:budgetId is null or b.id = :budgetId) " +
                "and (:transactionType is null or LOWER(t.transactionType) = :transactionType) " +
                "and (t.date >= :startDate) " +
                "and (t.date <= :endDate) " +
                "group by coalesce(t.entity, ''), t.nickName, lower(t.transactionType) " +
                "order by times desc";

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

    String firstTransactionDate(Integer userId) {
        List<Transaction> transactions = getAllTransactions(userId);
        Transaction firstTransaction = transactions.get(transactions.size() - 1);
        return firstTransaction.getDate().toString();
    }

    List<Transaction> latestTransactions(Integer userId) {
        TypedQuery<Transaction> query = entityManager.createQuery("from Transaction where userAccount.id = :id order by date desc", Transaction.class);
        query.setParameter("id", userId);
        query.setMaxResults(2);
        return query.getResultList();
    }

    List<Budget> budgets(Integer userId) {
        TypedQuery<Budget> query = entityManager.createQuery("from Budget where userAccount.id = :id", Budget.class);
        query.setParameter("id", userId);
        query.setMaxResults(2);
        return query.getResultList();
    }

    List<TransactionCategory> categories(Integer userId) {
        TypedQuery<TransactionCategory> query = entityManager.createQuery("from TransactionCategory t where t.userAccount.id = :id", TransactionCategory.class);
        query.setParameter("id", userId);
        query.setMaxResults(2);
        return query.getResultList();
    }

    Map<String, Object> getTodayExpenditure(Integer userId, String date) {
        Map<String, Object> todayExpenditure = new HashMap<>();

        // Query to sum the transaction amounts for the given date
        TypedQuery<Object[]> query = entityManager.createQuery(
                "select " +
                        "sum(case when t.transactionAmount > 0 then t.transactionAmount else 0 end) as totalIn, " +
                        "sum(case when t.transactionAmount < 0 then abs(t.transactionAmount) else 0 end) as totalOut " +
                        "from Transaction t " +
                        "where t.userAccount.id = :userId " +
                        "and t.date = :date",
                Object[].class
        );

        query.setParameter("userId", userId);
        query.setParameter("date", LocalDate.parse(date));

        // Execute the query
        Object[] result = query.getSingleResult();

        System.out.println("RESULT:");
        System.out.println(result);

        // Initialize default values
        Double totalIn = result[0] != null ? (Double) result[0] : 0.0;
        Double totalOut = result[1] != null ? (Double) result[1] : 0.0;

        // Populate the map with results
        todayExpenditure.put("totalIn", totalIn);
        todayExpenditure.put("totalOut", totalOut);

        return todayExpenditure;
    }

    public Map<String, Object> getDashboardDetails(Integer userId, String date) {
        Map<String, Object> dashboardDetails = new HashMap<>();
        String firstTransactionDate = firstTransactionDate(userId);
        Double accountBalance = getCurrentBalance(userId);
        List<Transaction> latestTransactions = latestTransactions(userId);
        List<TransactionCategory> categories = categories(userId);
        List<Budget> budgets = budgets(userId);
        Map<String, Object> todayExpenditure = getTodayExpenditure(userId, date);

        dashboardDetails.put("firstTransactionDate", firstTransactionDate);
        dashboardDetails.put("accountBalance", accountBalance);
        dashboardDetails.put("latestTransactions", latestTransactions);
        dashboardDetails.put("categories", categories);
        dashboardDetails.put("budgets", budgets);
        dashboardDetails.put("todayExpenditure", todayExpenditure);
        return dashboardDetails;
    }


    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
