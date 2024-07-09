package com.app.CashLedger.dao;

import com.app.CashLedger.dto.CategoryKeywordEditDto;
import com.app.CashLedger.models.CategoryKeyword;
import com.app.CashLedger.models.Transaction;
import com.app.CashLedger.models.TransactionCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.List;

@Repository
public class CategoryDao {
    private final EntityManager entityManager;
    private final TransactionDao transactionDao;
    @Autowired
    public CategoryDao(
            EntityManager entityManager,
            TransactionDao transactionDao
    ) {
        this.entityManager = entityManager;
        this.transactionDao = transactionDao;
    }

    public TransactionCategory addCategory(TransactionCategory transactionCategory) {
        entityManager.persist(transactionCategory);
        return transactionCategory;
    }

    public TransactionCategory getCategory(Integer id) {
        TypedQuery<TransactionCategory> query = entityManager.createQuery("from TransactionCategory where id = :id", TransactionCategory.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public CategoryKeyword getCategoryKeyword(Integer id) {
        TypedQuery<CategoryKeyword> query = entityManager.createQuery("from CategoryKeyword where id = :id", CategoryKeyword.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public List<TransactionCategory> getCategories(Integer id) {
        TypedQuery<TransactionCategory> query = entityManager.createQuery("from TransactionCategory where userAccount.id = :id", TransactionCategory.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

    public TransactionCategory updateCategory(TransactionCategory transactionCategory) {
        entityManager.merge(transactionCategory);
        return transactionCategory;
    }

    public String deleteCategory(Integer id) {
        Query deleteQuery1 = entityManager.createQuery("delete from CategoryKeyword where transactionCategory.id = :id");
        Query deleteQuery2 = entityManager.createQuery("delete from TransactionCategory where id = :id");
        deleteQuery1.setParameter("id", id);
        deleteQuery2.setParameter("id", id);
        deleteQuery1.executeUpdate();
        return "Deleted "+deleteQuery2.executeUpdate()+" rows";
    }

    public CategoryKeyword updateCategoryKeyword(CategoryKeyword categoryKeyword) {
        Integer categoryKeywordId = categoryKeyword.getId();
        TypedQuery<CategoryKeyword> fetchQuery = entityManager.createQuery("from CategoryKeyword where id = :id", CategoryKeyword.class);
        fetchQuery.setParameter("id", categoryKeywordId);
        CategoryKeyword existingCategoryKeyword = fetchQuery.getSingleResult();
        TransactionCategory transactionCategory = categoryKeyword.getTransactionCategory();

        if (!existingCategoryKeyword.getKeyword().equals(categoryKeyword.getKeyword())) {
            String oldKeyword = existingCategoryKeyword.getKeyword();

            // Fetch transactions meeting the conditions
            TypedQuery<Integer> transactionIdsQuery = entityManager.createQuery(
                    "select t.id from Transaction t join t.categories c where c.id = :categoryId and (LOWER(t.sender) like concat('%', :entity, '%') or LOWER(t.recipient) like concat('%', :entity, '%'))", Integer.class);
            transactionIdsQuery.setParameter("categoryId", transactionCategory.getId());
            transactionIdsQuery.setParameter("entity", oldKeyword.toLowerCase());
            List<Integer> transactionIds = transactionIdsQuery.getResultList();
            System.out.println(oldKeyword);
            System.out.println(transactionIds);

            if (!transactionIds.isEmpty()) {
                // Delete associations in the join table
                Query deleteQuery = entityManager.createNativeQuery(
                        "DELETE FROM transaction_category_mapping WHERE transaction_id IN :transactionIds AND category_id = :categoryId");
                deleteQuery.setParameter("transactionIds", transactionIds);
                deleteQuery.setParameter("categoryId", transactionCategory.getId());
                deleteQuery.executeUpdate();
            }

            List<Transaction> transactionList = transactionDao.getTransactions(transactionCategory.getUserAccount().getId(), categoryKeyword.getKeyword());
            System.out.println(transactionList.size());
            for (Transaction transaction : transactionList) {
                System.out.println(transaction.getRecipient());
                // Add the relationship in the join table
                transaction.getCategories().add(transactionCategory);
                transactionCategory.getTransactions().add(transaction);
                entityManager.merge(transaction);
            }
        }

        // Merge the updated CategoryKeyword
        entityManager.merge(categoryKeyword);
        return categoryKeyword;
    }



    public String deleteCategoryKeyword(CategoryKeywordEditDto keywordDetails) {
        TypedQuery<CategoryKeyword> fetchQuery = entityManager.createQuery("from CategoryKeyword where id = :id", CategoryKeyword.class);
        System.out.println(keywordDetails);
        fetchQuery.setParameter("id", keywordDetails.getKeywordId());
        CategoryKeyword categoryKeyword = fetchQuery.getSingleResult();
        String keyword = categoryKeyword.getKeyword();
        System.out.println(keyword);

        // Fetch transactions meeting the conditions
        TypedQuery<Integer> transactionQuery = entityManager.createQuery(
                "select t.id from Transaction t join t.categories c where c.id = :categoryId and (LOWER(t.sender) like concat('%', :entity, '%') or LOWER(t.recipient) like concat('%', :entity, '%'))", Integer.class);
        transactionQuery.setParameter("entity", keyword.toLowerCase());
        transactionQuery.setParameter("categoryId", keywordDetails.getCategoryId());
        List<Integer> transactionIds = transactionQuery.getResultList();

        for(Integer id : transactionIds) {
            System.out.println(id);
        }


        if (!transactionIds.isEmpty()) {
            // Delete associations in the join table
            Query deleteQuery = entityManager.createNativeQuery(
                    "DELETE FROM transaction_category_mapping WHERE transaction_id IN :transactionIds AND category_id = :categoryId");
            deleteQuery.setParameter("transactionIds", transactionIds);
            deleteQuery.setParameter("categoryId", keywordDetails.getCategoryId());
            deleteQuery.executeUpdate();
        }

        Query query = entityManager.createQuery("delete from CategoryKeyword where id = :id");
        query.setParameter("id", keywordDetails.getKeywordId());

//        return transactionIds.size() + " records";
        return "Deleted "+query.executeUpdate()+" rows";
    }
}
