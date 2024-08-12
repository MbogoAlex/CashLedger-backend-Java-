package com.app.CashLedger.services;

import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.dto.TransactionDto;
import com.app.CashLedger.dto.TransactionEditDto;
import com.app.CashLedger.models.Transaction;
import com.app.CashLedger.models.UserAccount;
import com.app.CashLedger.reportModel.AllTransactionsReportModel;
import net.sf.jasperreports.engine.JRException;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    TransactionDto addTransaction(Transaction transaction);

    List<TransactionDto> addTransactions(List<TransactionDto> transactions, Integer userId);

    TransactionDto extractTransactionDetails(MessageDto message, UserAccount userAccount);

    String getMessageTransactionCode(MessageDto message);

    List<String> getLatestTransactionCode(Integer userId);

    String updateTransaction(TransactionEditDto transactionEditDto);

    List<TransactionDto> getTransactions(Integer userId, String entity);

    Map<Object, Object> getUserTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean latest, String startDate, String endDate);

    Map<Object, Object> getUserTransactionsSorted(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean moneyIn, Boolean orderByAmount, Boolean ascendingOrder, String startDate, String endDate);
    Map<Object, Object> getGroupedByDateTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate);
    Map<Object, Object> getUserTransactionsSortedByFrequency(Integer userId, String entity, Integer categoryId, String transactionType, Boolean moneyIn, Boolean ascendingOrder, String startDate, String endDate);

    Double getCurrentBalance(Integer userId);

    Map<Object, Object> getExpenditure(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean moneyIn, Boolean latest, String startDate, String endDate);

    Map<Object, Object> getGroupedByEntityTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate);
    Map<Object, Object> getGroupedByMonthAndYearTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String month, String year);

    ByteArrayOutputStream generateAllTransactionsReport(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) throws JRException, ParseException;

    Map<String, Object> getDashboardDetails(Integer userId, String date);

    String deleteAllTransactions();

}