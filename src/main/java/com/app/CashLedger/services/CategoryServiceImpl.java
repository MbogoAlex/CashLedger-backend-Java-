package com.app.CashLedger.services;

import com.app.CashLedger.dao.BudgetDao;
import com.app.CashLedger.dao.CategoryDao;
import com.app.CashLedger.dao.TransactionDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.*;
import com.app.CashLedger.models.*;
import com.app.CashLedger.reportModel.AllTransactionsReportModel;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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
    @Transactional
    @Override
    public TransactionCategoryDto addTransactionToCategory(Transaction transaction, Integer categoryId) {
//        System.out.println("ADDING_TRANSACTION: "+ transaction.toString());
        TransactionCategory category = categoryDao.getCategory(categoryId);
        Double updatedTimes = category.getUpdatedTimes();

        if(updatedTimes == null) {
            updatedTimes = 0.0;
        }


        List<CategoryKeyword> categoryKeywords = category.getKeywords();
        // Step 1: Pre-process the categoryKeywords into a HashMap for faster lookup.
        Map<String, CategoryKeyword> keywordMap = new HashMap<>();
        for (CategoryKeyword categoryKeyword : categoryKeywords) {
            keywordMap.put(categoryKeyword.getKeyword().toLowerCase(), categoryKeyword);
        }

// Step 2: Look up the transaction entity in the keyword map.
        String entityLower = transaction.getEntity().toLowerCase();
        CategoryKeyword matchingKeyword = keywordMap.get(entityLower);

        if (matchingKeyword != null) {
            transaction.getCategories().add(category);
            category.getTransactions().add(transaction);
            try {
                category.setUpdatedTimes(updatedTimes + 1.0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            category.setUpdatedAt(LocalDateTime.now());
            // categoryDao.updateCategory(category);
//            System.out.println("Updated category");
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

    @Override
    public ByteArrayOutputStream generateMultipleCategoriesReport(MultipleCategoriesReportDto multipleCategoriesReportDto) throws JRException, ParseException {

        // Fetch user and transactions
        UserAccount userAccount = userAccountDao.getUser(multipleCategoriesReportDto.getUserId());
        List<Transaction> transactions = transactionDao.getTransactionsForMultipleCategories(
                userAccount.getId(), null, multipleCategoriesReportDto.getCategoryIds(), null, null, true, multipleCategoriesReportDto.getStartDate(), multipleCategoriesReportDto.getLastDate());
        List<AllTransactionsReportModel> allTransactionsReportModel = new ArrayList<>();

        // Date formatting
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");

        Date start = inputFormat.parse(multipleCategoriesReportDto.getStartDate());
        Date end = inputFormat.parse(multipleCategoriesReportDto.getLastDate());

        String formattedStartDate = outputFormat.format(start);
        String formattedEndDate = outputFormat.format(end);

        // Initialize totals
        Double totalIn = 0.0;
        Double totalOut = 0.0;
        Double totalTransactionCost = 0.0;

        // Determine report owner
        String owner;
        if (userAccount.getFname() == null && userAccount.getLname() == null) {
            owner = userAccount.getPhoneNumber();
        } else if (userAccount.getFname() == null) {
            owner = userAccount.getLname();
        } else if (userAccount.getLname() == null) {
            owner = userAccount.getFname();
        } else {
            owner = userAccount.getFname() + " " + userAccount.getLname();
        }

        // Process transactions and build report models
        for (Transaction transaction : transactions) {
            List<String> categoryNames = new ArrayList<>();
            String moneyIn = "-";
            String moneyOut = "-";
            String transactionCost = "-";

            if (transaction.getTransactionAmount() > 0) {
                totalIn += transaction.getTransactionAmount();
                moneyIn = "Ksh" + transaction.getTransactionAmount();
            } else if (transaction.getTransactionAmount() < 0) {
                totalOut += Math.abs(transaction.getTransactionAmount());
                totalTransactionCost += Math.abs(transaction.getTransactionCost());
                moneyOut = "Ksh" + Math.abs(transaction.getTransactionAmount());
                transactionCost = "Ksh" + Math.abs(transaction.getTransactionCost());
            }

            if (!transaction.getCategories().isEmpty()) {
                for (TransactionCategory category : transaction.getCategories()) {
                    categoryNames.add(category.getName());
                }
            } else {
                categoryNames.add("-");
            }

            AllTransactionsReportModel model = AllTransactionsReportModel.builder()
                    .datetime(transaction.getDate() + " " + transaction.getTime())
                    .transactionType(transaction.getTransactionType())
                    .category(String.join(", ", categoryNames))
                    .entity(transaction.getEntity())
                    .moneyIn(moneyIn)
                    .moneyOut(moneyOut)
                    .transactionCost(transactionCost)
                    .build();
            allTransactionsReportModel.add(model);
        }

        if ("csv".equalsIgnoreCase(multipleCategoriesReportDto.getReportType())) {
            // Generate CSV
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 PrintWriter writer = new PrintWriter(byteArrayOutputStream)) {

                // Write CSV header
                writer.println("Owner:," + owner);
                writer.println("Start Date:," + formattedStartDate);
                writer.println("End Date:," + formattedEndDate);
                writer.println("Total in:,"+ "Ksh"+ String.format("%.2f", totalIn));
                writer.println("Total out:,"+ "Ksh"+ String.format("%.2f", totalOut));
                writer.println();
                writer.println("Report Generated:," + new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss").format(new Date()));
                writer.println();

                writer.println("Date,Time,Transaction Type,Category,Entity,Money In,Money Out,Transaction Cost");

                // Write CSV data
                for (AllTransactionsReportModel model : allTransactionsReportModel) {
                    writer.println(String.join(",",
                            model.getDatetime().split(" ")[0],   // Date
                            model.getDatetime().split(" ")[1],   // Time
                            model.getTransactionType(),
                            model.getCategory(),
                            model.getEntity(),
                            model.getMoneyIn(),
                            model.getMoneyOut(),
                            model.getTransactionCost()
                    ));
                }

                // Write totals at the end
                writer.println(",,,,,Total In: Ksh" + String.format("%.2f", totalIn) +
                        ",Total Out: Ksh" + String.format("%.2f", totalOut) +
                        ",Total Transaction Cost: Ksh" + String.format("%.2f", totalTransactionCost));

                writer.flush();
                return byteArrayOutputStream;

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error generating CSV report", e);
            }
        } else {
            // Generate PDF
            String jrxmlPath = "/templates/AllTransactionsReport.jrxml";
            try (InputStream jrxmlInput = this.getClass().getResourceAsStream(jrxmlPath);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                if (jrxmlInput == null) {
                    throw new IllegalStateException("JRXML file not found at path: " + jrxmlPath);
                }

                JRBeanCollectionDataSource allTransactionsDataSource = new JRBeanCollectionDataSource(allTransactionsReportModel);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("owner", owner);
                parameters.put("startDate", formattedStartDate);
                parameters.put("endDate", formattedEndDate);
                parameters.put("totalIn", "Ksh" + String.format("%.2f", totalIn));
                parameters.put("totalOut", "Ksh" + String.format("%.2f", totalOut));
                parameters.put("totalTransactionCost", "Ksh" + String.format("%.2f", totalTransactionCost));
                parameters.put("size", transactions.size());
                parameters.put("allTransactionsDataset", allTransactionsDataSource);

                JasperReport report = JasperCompileManager.compileReport(jrxmlInput);
                JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

                // Export the report to a PDF
                JRPdfExporter exporter = new JRPdfExporter();
                SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
                configuration.setCompressed(true);
                exporter.setConfiguration(configuration);
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                exporter.exportReport();

                return byteArrayOutputStream;

            } catch (Exception e) {
                e.printStackTrace();
                throw new JRException("Error generating PDF report", e);
            }
        }
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
                .entity(transaction.getEntity())
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
