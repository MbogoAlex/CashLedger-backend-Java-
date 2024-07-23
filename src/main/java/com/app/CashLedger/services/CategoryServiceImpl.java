package com.app.CashLedger.services;

import com.app.CashLedger.dao.BudgetDao;
import com.app.CashLedger.dao.CategoryDao;
import com.app.CashLedger.dao.TransactionDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.*;
import com.app.CashLedger.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{
    private final CategoryDao categoryDao;
    private final UserAccountDao userAccountDao;
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    @Autowired
    public CategoryServiceImpl(
            CategoryDao categoryDao,
            UserAccountDao userAccountDao,
            TransactionDao transactionDao,
            BudgetDao budgetDao
    ) {
        this.categoryDao = categoryDao;
        this.userAccountDao = userAccountDao;
        this.transactionDao = transactionDao;
        this.budgetDao = budgetDao;
    }
    @Transactional
    @Override
    public TransactionCategoryDto createCategory(CategoryEditDto category, Integer userId) {
        UserAccount user = userAccountDao.getUser(userId);
        List<Transaction> filteredTransactions = new ArrayList<>();
        List<CategoryKeyword> categoryKeywords = new ArrayList<>();
        TransactionCategory transactionCategory = new TransactionCategory();
        transactionCategory.setName(category.getCategoryName());
        transactionCategory.setCreatedAt(LocalDateTime.now());
        transactionCategory.setUserAccount(user);

        for (String keyword : category.getKeywords()) {
            List<Transaction> transactions = transactionDao.getTransactions(userId, keyword);
            filteredTransactions.addAll(transactions);

            CategoryKeyword categoryKeyword = CategoryKeyword.builder()
                    .keyword(keyword)
                    .nickName(keyword)
                    .transactionCategory(transactionCategory)
                    .build();
            categoryKeywords.add(categoryKeyword);
        }

        transactionCategory.getTransactions().addAll(filteredTransactions);
        transactionCategory.getKeywords().addAll(categoryKeywords);

        // Persist the transaction category first to get the generated ID
        TransactionCategory savedCategory = categoryDao.addCategory(transactionCategory);

        // Now associate transactions with the saved category
        for (Transaction transaction : filteredTransactions) {
            transaction.getCategories().add(savedCategory);
            transactionDao.updateTransaction(transaction);
        }

        return transformTransactionCategory(savedCategory);
    }

    @Transactional
    @Override
    public TransactionCategoryDto updateCategoryName(CategoryEditDto category, Integer id) {
        TransactionCategory category1 = categoryDao.getCategory(id);
        category1.setName(category.getCategoryName());

        return transformTransactionCategory(categoryDao.updateCategory(category1));
    }
//    @Transactional
    @Override
    public TransactionCategoryDto addTransactionToCategory(Transaction transaction, Integer categoryId) {
        TransactionCategory category = categoryDao.getCategory(categoryId);
        List<Transaction> existingTransactions = category.getTransactions();
        category.setUpdatedTimes(category.getUpdatedTimes() + 1);
        category.setUpdatedAt(LocalDateTime.now());

        for(Transaction transaction1 : existingTransactions) {
            if(transaction.getRecipient().equals(transaction1.getRecipient()) || transaction.getSender().equals(transaction1.getSender())) {
                if(!transaction1.getCategories().contains(category)) {
                    transaction.getCategories().add(category);
                    category.getTransactions().add(transaction);
                    break;
                }
            }
        }

        return transformTransactionCategory(categoryDao.updateCategory(category));
    }
    @Transactional
    @Override
    public TransactionCategoryDto addKeywordsToCategory(CategoryEditDto categoryEditDto, Integer id) {
        TransactionCategory category = categoryDao.getCategory(id);
        List<Transaction> filteredTransactions = new ArrayList<>();
        List<CategoryKeyword> categoryKeywords = new ArrayList<>();

        for (String keyword : categoryEditDto.getKeywords()) {
            List<Transaction> transactions = transactionDao.getTransactions(categoryEditDto.getUserId(), keyword);
            filteredTransactions.addAll(transactions);

            CategoryKeyword categoryKeyword = CategoryKeyword.builder()
                    .keyword(keyword)
                    .nickName(keyword)
                    .transactionCategory(category)
                    .build();
            categoryKeywords.add(categoryKeyword);
        }

        category.getTransactions().addAll(filteredTransactions);
        category.getKeywords().addAll(categoryKeywords);

        // Persist the transaction category first to get the generated ID
        TransactionCategory savedCategory = categoryDao.updateCategory(category);

        // Now associate transactions with the saved category
        for (Transaction transaction : filteredTransactions) {
            transaction.getCategories().add(savedCategory);
            transactionDao.updateTransaction(transaction);
        }


        return transformTransactionCategory(savedCategory);
    }

    @Transactional
    @Override
    public CategoryKeywordDto updateCategoryKeyword(CategoryKeywordDto categoryKeywordDto) {
        CategoryKeyword categoryKeyword = categoryDao.getCategoryKeyword(categoryKeywordDto.getId());
        TransactionCategory category = categoryKeyword.getTransactionCategory();
        List<Transaction> transactions = category.getTransactions();

        for(Transaction transaction : transactions) {
            if(transaction.getSender().equals(categoryKeyword.getKeyword()) || transaction.getRecipient().equals(categoryKeyword.getKeyword())) {
                transaction.setNickName(categoryKeywordDto.getNickName());
                transactionDao.updateTransaction(transaction);
            }
        }

        categoryKeyword.setKeyword(categoryKeywordDto.getKeyWord());
        categoryKeyword.setNickName(categoryKeywordDto.getNickName());
        return categoryToCategoryKeywordDto(categoryDao.updateCategoryKeyword(categoryKeyword));
    }

    @Override
    public List<TransactionCategoryDto> getCategories(Integer userId, Integer categoryId, String name, String orderBy) {
        List<TransactionCategory> categories = categoryDao.getCategories(userId, categoryId, name, orderBy);
        List<TransactionCategoryDto> categoryDtos = new ArrayList<>();

        for(TransactionCategory category : categories) {
            categoryDtos.add(transformTransactionCategory(category));
        }

        return categoryDtos;
    }

    @Override
    public TransactionCategoryDto getCategory(Integer categoryId) {
        return transformTransactionCategory(categoryDao.getCategory(categoryId));
    }

    @Transactional
    @Override
    public String deleteCategory(Integer id) {
        TransactionCategory category = categoryDao.getCategory(id);
        List<Budget> budgets = category.getBudgets();
        for(Budget budget : budgets) {
            budgetDao.deleteBudget(budget.getId());
        }
        return categoryDao.deleteCategory(id);
    }
    @Transactional
    @Override
    public String deleteCategoryKeyword(Integer categoryId, Integer keywordId) {
        return categoryDao.deleteCategoryKeyword(categoryId, keywordId);
    }

    private TransactionCategoryDto transformTransactionCategory(TransactionCategory transactionCategory) {
        List<TransactionCategoryDto.BudgetDto> budgets = new ArrayList<>();
        List<TransactionDto> transactions = new ArrayList<>();
        List<CategoryKeywordDto> keywords = new ArrayList<>();
        for(Transaction transaction : transactionCategory.getTransactions()) {
            transactions.add(transactionToTransactionDto(transaction));
        }

        for(Budget budget : transactionCategory.getBudgets()) {
            TransactionCategoryDto.BudgetDto budgetDto = TransactionCategoryDto.BudgetDto.builder()
                    .id(budget.getId())
                    .createdAt(budget.getCreatedAt().toString())
                    .name(budget.getName())
                    .budgetLimit(budget.getBudgetLimit())
                    .createdAt(budget.getCreatedAt().toString())
                    .limitDate(budget.getLimitDate().toString())
                    .limitReached(budget.getLimitReached())
                    .exceededBy(budget.getExceededBy())
                    .build();
            budgets.add(budgetDto);
        }

        for(CategoryKeyword categoryKeyword : transactionCategory.getKeywords()) {
            keywords.add(categoryToCategoryKeywordDto(categoryKeyword));
        }



        TransactionCategoryDto transactionCategoryDto = TransactionCategoryDto.builder()
                .id(transactionCategory.getId())
                .name(transactionCategory.getName())
                .createdAt(transactionCategory.getCreatedAt())
                .transactions(transactions)
                .keywords(keywords)
                .budgets(budgets)
                .build();
        return transactionCategoryDto;
    }

    private TransactionDto transactionToTransactionDto(Transaction transaction) {
        List<TransactionDto.Category> categories = new ArrayList<>();
        for(TransactionCategory transactionCategory : transaction.getCategories()) {
            TransactionDto.Category category = TransactionDto.Category.builder()
                    .id(transactionCategory.getId())
                    .name(transactionCategory.getName())
                    .build();
            categories.add(category);
        }
        TransactionDto transactionDto = TransactionDto.builder()
                .transactionId(transaction.getId())
                .transactionCode(transaction.getTransactionCode())
                .transactionType(transaction.getTransactionType())
                .transactionAmount(transaction.getTransactionAmount())
                .transactionCost(transaction.getTransactionCost())
                .date(transaction.getDate())
                .time(transaction.getTime())
                .sender(transaction.getSender())
                .nickName(transaction.getNickName())
                .recipient(transaction.getRecipient())
                .balance(transaction.getBalance())
                .categories(categories)
                .build();
        return transactionDto;
    }

    private CategoryKeywordDto categoryToCategoryKeywordDto(CategoryKeyword categoryKeyword) {
        CategoryKeywordDto categoryKeywordDto = CategoryKeywordDto.builder()
                .id(categoryKeyword.getId())
                .keyWord(categoryKeyword.getKeyword())
                .nickName(categoryKeyword.getNickName())
                .build();
        return categoryKeywordDto;
    }

}
