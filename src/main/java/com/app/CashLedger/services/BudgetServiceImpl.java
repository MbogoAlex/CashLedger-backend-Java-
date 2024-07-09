package com.app.CashLedger.services;

import com.app.CashLedger.dao.BudgetDao;
import com.app.CashLedger.dao.CategoryDao;
import com.app.CashLedger.dao.TransactionDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.BudgetEditDto;
import com.app.CashLedger.dto.BudgetResponseDto;
import com.app.CashLedger.models.Budget;
import com.app.CashLedger.models.Transaction;
import com.app.CashLedger.models.TransactionCategory;
import com.app.CashLedger.models.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class BudgetServiceImpl implements BudgetService{
    private final BudgetDao budgetDao;
    private final UserAccountDao userAccountDao;
    private final CategoryDao categoryDao;
    private final TransactionDao transactionDao;
    @Autowired
    public BudgetServiceImpl(
            BudgetDao budgetDao,
            UserAccountDao userAccountDao,
            CategoryDao categoryDao,
            TransactionDao transactionDao
    ) {
        this.budgetDao = budgetDao;
        this.userAccountDao = userAccountDao;
        this.categoryDao = categoryDao;
        this.transactionDao = transactionDao;
    }
    @Transactional
    @Override
    public BudgetResponseDto createBudget(BudgetEditDto budgetDto, Integer userId, Integer categoryId) {
        UserAccount user = userAccountDao.getUser(userId);
        TransactionCategory category = categoryDao.getCategory(categoryId);

        Budget budget = Budget.builder()
                .name(budgetDto.getName())
                .budgetLimit(budgetDto.getBudgetLimit())
                .createdAt(LocalDateTime.now())
                .limitDate(LocalDate.parse(budgetDto.getLimitDate()))
                .limitReached(false)
                .exceededBy(0.0)
                .category(category)
                .userAccount(user)
                .build();

        return budgetToBudgetResponseDto(budgetDao.createBudget(budget), 0.0);
    }
    @Transactional

    @Override
    public BudgetResponseDto updateBudget(BudgetEditDto budgetDto, Integer budgetId) {
        Budget budget = budgetDao.getBudget(budgetId);
        List<Transaction> transactions = transactionDao.getGeneralTransactions(budget.getUserAccount().getId(), budget.getCreatedAt().toLocalDate().toString(), LocalDate.now().toString());

        double budgetExceededBy = 0.0;
        boolean budgetLimitReached = false;
        double expenditure = 0.0;
        for(Transaction transaction : transactions) {
            if(transaction.getTransactionAmount() < 0) {
                expenditure = expenditure + Math.abs(transaction.getTransactionAmount());
            }
        }

        if(expenditure >= budgetDto.getBudgetLimit()) {
            budgetLimitReached = true;
            budgetExceededBy = expenditure - budget.getBudgetLimit();
        }
        budget.setName(budgetDto.getName());
        budget.setLimitReached(budgetLimitReached);
        budget.setExceededBy(budgetExceededBy);
        budget.setLimitDate(LocalDate.parse(budget.getLimitDate().toString()));
        budget.setBudgetLimit(budgetDto.getBudgetLimit());

        return budgetToBudgetResponseDto(budgetDao.updateBudget(budget), expenditure);
    }


    @Transactional
    @Override
    public BudgetResponseDto getBudget(Integer budgetId) {
        Budget budget = budgetDao.getBudget(budgetId);
        List<Transaction> transactions = transactionDao.getGeneralTransactions(budget.getUserAccount().getId(), budget.getCreatedAt().toLocalDate().toString(), LocalDate.now().toString());
        System.out.println(transactions.size());

        double budgetExceededBy = 0.0;
        boolean budgetLimitReached = false;
        double expenditure = 0.0;
        for(Transaction transaction : transactions) {
            if(transaction.getTransactionAmount() < 0) {
                expenditure = expenditure + Math.abs(transaction.getTransactionAmount());
            }
        }

        if(expenditure >= budget.getBudgetLimit()) {
            budgetLimitReached = true;
            budgetExceededBy = expenditure - budget.getBudgetLimit();
            budget.setLimitReached(true);
            budget.setExceededBy(budgetExceededBy);
            budget.setLimitDate(LocalDate.parse(budget.getLimitDate().toString()));
            return budgetToBudgetResponseDto(budgetDao.updateBudget(budget), expenditure);
        } else {
            return budgetToBudgetResponseDto(budgetDao.getBudget(budgetId), expenditure);
        }


    }
    @Transactional
    @Override
    public List<BudgetResponseDto> getUserBudgets(Integer userId) {
        List<Budget> budgets = budgetDao.getUserBudgets(userId);
        List<BudgetResponseDto> transformedBudgets = new ArrayList<>();


        for(Budget budget : budgets) {
            List<Transaction> transactions = transactionDao.getGeneralTransactions(budget.getUserAccount().getId(), budget.getCreatedAt().toLocalDate().toString(), LocalDate.now().toString());

            double budgetExceededBy = 0.0;
            boolean budgetLimitReached = false;
            double expenditure = 0.0;
            for(Transaction transaction : transactions) {
                if(transaction.getTransactionAmount() < 0) {
                    expenditure = expenditure + Math.abs(transaction.getTransactionAmount());
                }
            }

            if(expenditure >= budget.getBudgetLimit()) {
                budgetLimitReached = true;
                budgetExceededBy = expenditure - budget.getBudgetLimit();
                budget.setLimitReached(true);
                budget.setExceededBy(budgetExceededBy);
                budget.setLimitDate(LocalDate.parse(budget.getLimitDate().toString()));
                transformedBudgets.add(budgetToBudgetResponseDto(budgetDao.updateBudget(budget), expenditure));
            } else {
                transformedBudgets.add(budgetToBudgetResponseDto(budget, expenditure));
            }


        }
        return transformedBudgets;
    }
    @Transactional
    @Override
    public String deleteBudget(Integer budgetId) {
        return budgetDao.deleteBudget(budgetId);
    }

    private BudgetResponseDto budgetToBudgetResponseDto(Budget budget, Double expenditure) {
        BudgetResponseDto.Category category = BudgetResponseDto.Category.builder()
                .id(budget.getCategory().getId())
                .name(budget.getCategory().getName())
                .build();

        BudgetResponseDto.UserDetailsDto userDetailsDto = BudgetResponseDto.UserDetailsDto.builder()
                .id(budget.getUserAccount().getId())
                .name(budget.getUserAccount().getFname())
                .build();

        BudgetResponseDto budgetResponseDto = BudgetResponseDto.builder()
                .id(budget.getId())
                .name(budget.getName())
                .budgetLimit(budget.getBudgetLimit())
                .expenditure(expenditure)
                .createdAt(budget.getCreatedAt().toString())
                .limitDate(budget.getLimitDate().toString())
                .limitReached(budget.getLimitReached())
                .exceededBy(budget.getExceededBy())
                .category(category)
                .user(userDetailsDto)
                .build();
        return budgetResponseDto;
    }


}
