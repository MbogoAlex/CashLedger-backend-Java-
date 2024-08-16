package com.app.CashLedger.services;

import com.app.CashLedger.dto.*;
import com.app.CashLedger.models.Transaction;
import net.sf.jasperreports.engine.JRException;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.List;

public interface CategoryService {
    TransactionCategoryDto createCategory(CategoryEditDto category, Integer userId);
    TransactionCategoryDto updateCategoryName(CategoryEditDto category, Integer id);

    TransactionCategoryDto addTransactionToCategory(Transaction transaction, Integer categoryId);

    TransactionCategoryDto addKeywordsToCategory(CategoryEditDto category, Integer id);

    CategoryKeywordDto updateCategoryKeyword(CategoryKeywordDto categoryKeywordDto);

    List<TransactionCategoryDto> getCategories(Integer userId, Integer categoryId, String name, String orderBy);

    TransactionCategoryDto getCategory(Integer categoryId);

    String deleteCategory(Integer id);
    String deleteCategoryKeyword(Integer categoryId, Integer keywordId);

    ByteArrayOutputStream generateMultipleCategoriesReport(MultipleCategoriesReportDto multipleCategoriesReportDto) throws JRException, ParseException;

}
