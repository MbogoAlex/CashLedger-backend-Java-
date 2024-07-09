package com.app.CashLedger.services;

import com.app.CashLedger.dto.CategoryEditDto;
import com.app.CashLedger.dto.CategoryKeywordDto;
import com.app.CashLedger.dto.CategoryKeywordEditDto;
import com.app.CashLedger.dto.TransactionCategoryDto;
import com.app.CashLedger.models.Transaction;

import java.util.List;

public interface CategoryService {
    TransactionCategoryDto createCategory(CategoryEditDto category, Integer userId);
    TransactionCategoryDto updateCategoryName(CategoryEditDto category, Integer id);

    TransactionCategoryDto addTransactionToCategory(Transaction transaction, Integer categoryId);

    CategoryKeywordDto updateCategoryKeyword(CategoryKeywordDto categoryKeywordDto);

    List<TransactionCategoryDto> getCategories(Integer userId);

    String deleteCategory(Integer id);
    String deleteCategoryKeyword(CategoryKeywordEditDto keywordDetails);

}
