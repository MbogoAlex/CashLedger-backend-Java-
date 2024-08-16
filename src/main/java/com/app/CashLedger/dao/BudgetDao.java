package com.app.CashLedger.dao;

import com.app.CashLedger.models.Budget;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class BudgetDao {
    private final EntityManager entityManager;
    @Autowired
    public BudgetDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Budget createBudget(Budget budget) {
        entityManager.persist(budget);
        return budget;
    }

    public Budget updateBudget(Budget budget) {
        entityManager.merge(budget);
        return budget;
    }

    public Budget getBudget(Integer id) {
        TypedQuery<Budget> query = entityManager.createQuery("from Budget where id = :id", Budget.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public List<Budget> getUserBudgets(Integer id, String name) {
        TypedQuery<Budget> query = entityManager.createQuery("from Budget where userAccount.id = :id and " +
                        "( :name is null or LOWER(name) like concat('%', :name, '%') or " +
                        "LOWER(category.name) like concat('%', :name, '%') ) " +
                        "order by createdAt desc",
                Budget.class);
        query.setParameter("id", id);
        if (name != null) {
            query.setParameter("name", name.toLowerCase());
        } else {
            query.setParameter("name", "");
        }
        return query.getResultList();
    }


    public List<Budget> getCategoryBudgets(Integer id, String name) {
        TypedQuery<Budget> query = entityManager.createQuery(
                "from Budget b where b.category.id = :id and " +
                        "(:name is null or LOWER(b.name) like concat('%', :name, '%') " +
                        "or LOWER(b.category.name) like concat('%', :name, '%')) " +
                        "order by b.createdAt desc", Budget.class);
        query.setParameter("id", id);
        if (name != null) {
            query.setParameter("name", name.toLowerCase());
        } else {
            query.setParameter("name", "");
        }

        return query.getResultList();
    }


    public String deleteBudget(Integer id) {
        Query query = entityManager.createQuery("delete from Budget where id = :id");
        query.setParameter("id", id);
        return "Deleted "+query.executeUpdate()+" rows";
    }
}
