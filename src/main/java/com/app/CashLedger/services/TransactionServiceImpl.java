package com.app.CashLedger.services;

import com.app.CashLedger.dao.CategoryDao;
import com.app.CashLedger.dao.MessageDao;
import com.app.CashLedger.dao.TransactionDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.dto.TransactionDto;
import com.app.CashLedger.dto.TransactionEditDto;
import com.app.CashLedger.models.*;
import com.app.CashLedger.reportModel.AllTransactionsReportModel;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TransactionServiceImpl implements TransactionService{
    private final TransactionDao transactionDao;
    private final UserAccountDao userAccountDao;
    private final CategoryDao categoryDao;
    private final CategoryService categoryService;
    private final MessageDao messageDao;
    @Autowired
    public TransactionServiceImpl(
            TransactionDao transactionDao,
            UserAccountDao userAccountDao,
            CategoryDao categoryDao,
            CategoryService categoryService,
            MessageDao messageDao
    ) {
        this.transactionDao = transactionDao;
        this.userAccountDao = userAccountDao;
        this.categoryDao = categoryDao;
        this.categoryService = categoryService;
        this.messageDao = messageDao;
    }
    @Override
    public TransactionDto addTransaction(Transaction transaction) {
        return null;
    }

    @Override
    public List<TransactionDto> addTransactions(List<TransactionDto> transactions, Integer userId) {
        UserAccount userAccount = userAccountDao.getUser(userId);
        List<Transaction> existingTransactions = userAccount.getTransactions();

        Set<String> transactionCodes = new HashSet<>();

        for(Transaction transaction : existingTransactions) {
            transactionCodes.add(transaction.getTransactionCode());
        }

        List<Transaction> transactionsToAdd = new ArrayList<>();

        for(TransactionDto transaction : transactions) {
            Transaction transaction1 = new Transaction();
            transaction1.setTransactionCode(transaction.getTransactionCode());
            transaction1.setTransactionType(transaction.getTransactionType());
            transaction1.setTransactionAmount(transaction.getTransactionAmount());
            transaction1.setTransactionCost(transaction.getTransactionCost());
            transaction1.setDate(transaction.getDate());
            transaction1.setTime(transaction.getTime());
            transaction1.setSender(transaction.getSender());
            transaction1.setRecipient(transaction.getRecipient());
            transaction1.setBalance(transaction.getBalance());
            transaction1.setUserAccount(userAccount);

            transactionsToAdd.add(transaction1);
        }

        List<Transaction> addedTransactions = transactionDao.addTransactions(transactionsToAdd);

        List<TransactionDto> processedTransactions = new ArrayList<>();

        for(Transaction transaction : addedTransactions) {
            processedTransactions.add(transactionToTransactionDto(transaction));
        }

        return processedTransactions;
    }
//    @Transactional
    @Override
    public TransactionDto extractTransactionDetails(MessageDto messageDto, Integer userId) {
//        System.out.println("PROCESSING");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        UserAccount userAccount = userAccountDao.getUser(userId);
        TransactionDto transactionDto = new TransactionDto();
        String message = messageDto.getBody();
        String realDate = String.valueOf(messageDto.getDate());
        String realTime = String.valueOf(messageDto.getTime());

        Matcher transactionCodeMatcher = Pattern.compile("\\b\\w{10}\\b").matcher(message);
        if (transactionCodeMatcher.find()) {
            String transactionCode = transactionCodeMatcher.group();
            String transactionType = null;
            Double transactionAmount = null;
            Double transactionCost = null;
            String transactionDate = null;
            String transactionTime = null;
            String sender = null;
            String recipient = null;
            Double balance = null;

            if (message.contains("sent to")) {

                // Parsing transaction amount
                Pattern amountPattern = Pattern.compile("Ksh([\\d ,]+\\.\\d{2})+ sent");
                Matcher amountMatcher = amountPattern.matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Parsing transaction cost
                Pattern costPattern = Pattern.compile("Transaction cost, ?Ksh\\.?\\s*([\\d,]+(?:\\.\\d{2})?)");
                Matcher costMatcher = costPattern.matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", ""));

                // Parsing transaction date and time
//                Pattern dateTimePattern = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2}) at (\\d{1,2}:\\d{2} [AP]M)");
//                Matcher dateTimeMatcher = dateTimePattern.matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                // Parsing recipient
                Pattern recipientPattern = Pattern.compile("sent to (.+?)(?: on )");
                Matcher recipientMatcher = recipientPattern.matcher(message);
                recipientMatcher.find();
                recipient = recipientMatcher.group(1).replace("\u00A0", " ");  // Unicode for non-breaking space

                String str = recipient;

                String regex = "^(?:(?!Safaricom Offers).)+ for account (?!SAFARICOM DATA BUNDLES|Tunukiwa|TUNUKIWA|Talkmore)[a-zA-Z0-9 ]+";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(str);

                boolean matches = matcher.find();

                if(matches) {
                    transactionType = "Pay Bill";
                } else {
                    transactionType = "Send Money";
                }

                // Parsing balance
                Pattern balancePattern = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})");
                Matcher balanceMatcher = balancePattern.matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));

                // Setting sender to "You"
                sender = "You";
            } else if (message.contains("Withdraw Ksh")) {
                transactionType = "Withdraw Cash";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Withdraw Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction cost
                Matcher costMatcher = Pattern.compile("Transaction cost, ?Ksh\\.?\\s*([\\d,]+(?:\\.\\d{2})?)").matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract recipient
                Matcher recipientMatcher = Pattern.compile("from\\s\\d+\\s-\\s[^.]+(?=\\sNew)").matcher(message);

                if (recipientMatcher.find()) {
                    recipient = recipientMatcher.group(0).trim();
                    recipient = "Withdrawal " + recipient;
                } else {
                    recipient = "N/A- Withdrawal from Mpesa-Shop";
                }

                sender = "You";

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
            } else if (message.contains("paid to")) {
                transactionType = "Buy Goods and Services (till)";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh([\\d ,]+\\.\\d{2})+ paid to").matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction cost
                Matcher costMatcher = Pattern.compile("Transaction cost, ?Ksh\\.?\\s*([\\d,]+(?:\\.\\d{2})?)").matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                sender = "You";

                // Extract recipient
                Matcher recipientMatcher = Pattern.compile("paid to (.+?(?=\\.))").matcher(message);
                recipientMatcher.find();
                recipient = recipientMatcher.group(1);

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
            } else if(message.contains("to Hustler Fund")) {
                transactionType = "Hustler Fund";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("You have sent Ksh([\\d ,]+\\.\\d{2})+ to").matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                transactionCost = 0.0;

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4})\\s*at\\s*(\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                sender = "You";

                // Extract recipient
                Matcher recipientMatcher = Pattern.compile("to (.+?) on").matcher(message);
                recipientMatcher.find();
                recipient = recipientMatcher.group(1);

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New\\s*MPESA\\s*balance\\s*is\\s*Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));

            } else if(message.contains("of airtime on")) {
                transactionType = "Airtime";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh([\\d ,]+\\.\\d{2})+ of airtime").matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction cost
                Matcher costMatcher = Pattern.compile("Transaction cost, ?Ksh\\.?\\s*([\\d,]+(?:\\.\\d{2})?)").matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                sender = "You";
                recipient = "Safaricom Airtime";

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
            } else if(message.contains("of airtime for")) {
                transactionType = "Airtime";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh([\\d ,]+\\.\\d{2})+ of airtime").matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction cost
                Matcher costMatcher = Pattern.compile("Transaction cost, ?Ksh([\\d,]+[.]?[ ]?[\\d]{2})").matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                sender = "You";

                // Extract recipient
                Matcher recipientMatcher = Pattern.compile("for (\\d{10})").matcher(message);
                recipientMatcher.find();
                recipient = recipientMatcher.group(1);

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New\\s*balance\\s*is\\s*Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
            } else if(message.contains("from your KCB M-PESA account")) {
                transactionType = "KCB Mpesa account";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("You have transfered Ksh([\\d ,]+\\.\\d{2})+ from").matcher(message);
                amountMatcher.find();
                transactionAmount = Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                transactionCost = 0.00;

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                sender = "KCB M-PESA Account";
                recipient = "You";

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
            } else if(message.contains("transfered to KCB M-PESA account")) {
                transactionType = "KCB Mpesa account";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh([\\d ,]+\\.\\d{2})+ transfered").matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                transactionCost = 0.00;

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                sender = "You";
                recipient = "KCB M-PESA Account";

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
            } else if(message.contains("Your M-Shwari loan has been approved")) {
                String pattern = "([A-Z0-9]+)\\s*Confirmed\\.\\s*Your\\s*M-Shwari\\s*loan\\s*has\\s*been\\s*approved\\s*on\\s*(\\d{1,2}/\\d{1,2}/\\d{2})\\s*(\\d{1,2}:\\d{2}\\s*[AP]M)\\s*and\\s*Ksh([\\d,]+\\.\\d{2})\\s*has\\s*been\\s*deposited\\s*to\\s*your\\s*M-PESA\\s*account\\.\\s*New\\s*M-PESA\\s*balance\\s*is\\s*Ksh([\\d,]+\\.\\d{2})\\s*\\.";

                // Create a Pattern object
                Pattern regexPattern = Pattern.compile(pattern);

                // Create a Matcher object
                Matcher matcher = regexPattern.matcher(message);

                // Check if pattern matches
                if (matcher.find()) {
                    transactionCode = matcher.group(1);
                    transactionType = "Mshwari";
                    transactionDate = realDate;
                    transactionTime = realTime;
                    transactionAmount = Double.parseDouble(matcher.group(4).replace(",", ""));
                    transactionCost = 0.0;
                    balance = Double.parseDouble(matcher.group(5).replace(",", ""));
                    sender = "M-Shwari loan";
                    recipient = "You";
                }
            } else if(message.contains("repaid from M-PESA")) {
                String pattern = "([A-Z0-9]+)\\s*Confirmed\\.\\s*Loan\\s*of\\s*Ksh([\\d,]+\\.\\d{2})\\s*repaid\\s*from\\s*M-PESA\\s*on\\s*(\\d{1,2}/\\d{1,2}/\\d{2})\\s*at\\s*(\\d{1,2}:\\d{2}\\s*[AP]M)\\.\\s*Loan\\s*balance\\s*is\\s*Ksh([\\d,]+\\.\\d{2})\\.\\s*Transaction\\s*cost\\s*Kshs\\s*([\\d,]+\\.\\d{2})";

                // Create a Pattern object
                Pattern regexPattern = Pattern.compile(pattern);

                // Create a Matcher object
                Matcher matcher = regexPattern.matcher(message);

                // Check if pattern matches
                if (matcher.find()) {
                    transactionCode = matcher.group(1);
                    transactionType = "Mshwari";
                    transactionAmount = -1 * Double.parseDouble(matcher.group(2).replace(",", ""));
                    transactionDate = realDate;
                    transactionTime = realTime;
                    balance = Double.parseDouble(matcher.group(5).replace(",", ""));
                    transactionCost = Double.parseDouble(matcher.group(6).replace(",", ""));
                    sender = "You";
                    recipient = "M-Shwari loan";
                }
            } else if(message.contains("from your M-PESA account to KCB M-PESA")) {
                String pattern = "([A-Z0-9]+)\\s*Confirmed\\.\\s*Your\\s*loan\\s*repayment\\s*of\\s*Ksh([\\d,]+\\.\\d{2})\\s*from\\s*your\\s*M-PESA\\s*account\\s*to\\s*KCB\\s*M-PESA\\s*on\\s*(\\d{1,2}/\\d{1,2}/\\d{2})\\s*at\\s*(\\d{1,2}:\\d{2}\\s*[AP]M)\\s*is\\s*successful\\.\\s*Your\\s*M-PESA\\s*balance\\s*is\\s*Ksh([\\d,]+\\.\\d{2})\\.";

                // Create a Pattern object
                Pattern regexPattern = Pattern.compile(pattern);

                // Create a Matcher object
                Matcher matcher = regexPattern.matcher(message);

                // Check if pattern matches
                if (matcher.find()) {
                    transactionCode = matcher.group(1);
                    transactionType = "KCB Mpesa account";
                    transactionAmount = -1 * Double.parseDouble(matcher.group(2).replace(",", ""));
                    transactionDate = realDate;
                    transactionTime = realTime;
                    balance = Double.parseDouble(matcher.group(5).replace(",", ""));
                    transactionCost = 0.0;
                    sender = "You";
                    recipient = "KCB M-PESA loan";
                }
            } else if(message.contains("transfered to Lock Savings")) {
                String patternAmount = "Ksh([\\d ,]+\\.\\d{2})+ transfered";
                String patternDateTime = "on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)";
                String patternBalance = "M-PESA balance is Ksh([\\d ,]+\\.\\d{2})";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile(patternAmount).matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile(patternDateTime).matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract balance
                Matcher balanceMatcher = Pattern.compile(patternBalance).matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));

                transactionType = "Lock savings";
                transactionCost = 0.00;
                sender = "You";
                recipient = "Lock Savings Account";
            } else if(message.contains("transferred to M-Shwari account")) {
                String patternAmount = "Ksh([\\d ,]+\\.\\d{2})+ transferred to";
                String patternCost = "Transaction cost  ?Ksh\\.?\\s*([\\d,]+(?:\\.\\d{2})?)";
                String patternDateTime = "on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)";
                String patternBalance = "M-PESA balance is Ksh([\\d ,]+\\.\\d{2})";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile(patternAmount).matcher(message);
                amountMatcher.find();
                transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction cost
                Matcher costMatcher = Pattern.compile(patternCost).matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile(patternDateTime).matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract balance
                Matcher balanceMatcher = Pattern.compile(patternBalance).matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));

                transactionType = "Mshwari";
                sender = "You";
                recipient = "M-Shwari account";
            } else if(message.contains("transferred from M-Shwari")) {
                String patternAmount = "Ksh([\\d ,]+\\.\\d{2})+ transferred from";
                String patternCost = "Transaction cost Ksh\\.?\\s*([\\d,]+(?:\\.\\d{2})?)";
                String patternDateTime = "on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)";
                String patternBalance = "M-PESA balance is Ksh([\\d ,]+\\.\\d{2})";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile(patternAmount).matcher(message);
                amountMatcher.find();
                transactionAmount = Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction cost
                Matcher costMatcher = Pattern.compile(patternCost).matcher(message);
                costMatcher.find();
                transactionCost = -1 * Double.parseDouble(costMatcher.group(1).replace(",", "").replace(" ", ""));

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile(patternDateTime).matcher(message);
//                dateTimeMatcher.find();
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract balance
                Matcher balanceMatcher = Pattern.compile(patternBalance).matcher(message);
                balanceMatcher.find();
                balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));

                transactionType = "Mshwari";
                sender = "M-Shwari account";
                recipient = "You";
            } else if(message.contains("has been successfully reversed")) {
                transactionAmount = 0.0;
                transactionCost = 0.0;

                // Determine transaction type
                transactionType = "Reversal";


                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh([\\d,]+\\.\\d{2})+ (is debited from|is credited to)").matcher(message);
                if (amountMatcher.find()) {
                    transactionAmount = Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));
                }

                // Set sender and recipient based on message content
                Matcher recipientMatcher = Pattern.compile("to ([^.\\n]+)").matcher(message);
                if (recipientMatcher.find()) {
                    recipient = "You";
                    sender = "Wrong recipient";
                    // transaction_amount = -1 * transaction_amount;
                } else {
                    recipient = "Wrong sender";
                    sender = "You";
                    transactionAmount = -1 * transactionAmount;
                }

                // Extract transaction date and time
//                Matcher dateMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4})[^\\d]*(\\d{1,2}:\\d{2} ?[AP]M)").matcher(message);

                transactionDate = realDate;
                transactionTime = realTime;


                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA account balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                if (balanceMatcher.find()) {
                    balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
                }
            } else if(message.contains("Give Ksh")) {
                transactionType = "Deposit";

                transactionCost = 0.0;

                recipient = "You";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Give Ksh([\\d ,]+\\.\\d{2})+ cash to").matcher(message);
                if (amountMatcher.find()) {
                    transactionAmount = Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));
                }

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("On (\\d{1,2}/\\d{1,2}/\\d{2}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract sender
                Matcher senderMatcher = Pattern.compile("to (.+?) New").matcher(message);
                if (senderMatcher.find()) {
                    sender = senderMatcher.group(1).replace("\u00A0", " ");  // Replace non-breaking space character
                }

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                if (balanceMatcher.find()) {
                    balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
                }
            } else if(message.contains("from Hustler Fund")) {
                transactionType = "Hustler Fund";
                transactionCost = 0.0;
                recipient = "You";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh([\\d ,]+\\.\\d{2})+ from").matcher(message);
                if (amountMatcher.find()) {
                    transactionAmount = Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));
                }

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{4}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract sender
                Matcher senderMatcher = Pattern.compile("You have received Ksh([\\d ,]+\\.\\d{2}) from (.+?) on").matcher(message);
                if (senderMatcher.find()) {
                    sender = senderMatcher.group(2);
                }

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New MPESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                if (balanceMatcher.find()) {
                    balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
                }
            } else if(message.contains("You have received Ksh")) {
                transactionType = "Send Money";
                transactionCost = 0.0;
                recipient = "You";

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("You have received Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                if (amountMatcher.find()) {
                    transactionAmount = Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));
                }

                // Extract transaction date and time
//                Matcher dateTimeMatcher = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2,4}) at (\\d{1,2}:\\d{2} [AP]M)").matcher(message);
                transactionDate = realDate;
                transactionTime = realTime;

                // Extract sender
                Matcher senderMatcher = Pattern.compile("from (.+?)(?: on| for)").matcher(message);
                if (senderMatcher.find()) {
                    sender = senderMatcher.group(1).replace("\u00A0", " ");
                }

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("New\\s*M-PESA\\s*balance\\s*is\\s*Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                if (balanceMatcher.find()) {
                    balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
                }
            } else if(message.contains("partially pay your outstanding Fuliza") || message.contains("fully pay your outstanding Fuliza")) {
                transactionAmount = 0.0;
                sender = "You";
                balance = 0.0;

                // Extract transaction amount
                Matcher amountMatcher = Pattern.compile("Ksh ([\\d ,]+\\.\\d{2})+ from").matcher(message);
                if (amountMatcher.find()) {
                    transactionAmount = -1 * Double.parseDouble(amountMatcher.group(1).replace(",", "").replace(" ", ""));
                }

                // Set transaction type
                transactionType = "Fuliza";

                // Set transaction cost
                transactionCost = 0.0;

                // Extract transaction date and time (assuming real_date and real_time are predefined)

                transactionDate = realDate;
                transactionTime = realTime;

                // Extract recipient
                Matcher recipientMatcher = Pattern.compile("outstanding (.+?(?=\\.))").matcher(message);
                if (recipientMatcher.find()) {
                    recipient = recipientMatcher.group(1);
                }

                // Extract balance
                Matcher balanceMatcher = Pattern.compile("M-PESA balance is Ksh([\\d ,]+\\.\\d{2})").matcher(message);
                if (balanceMatcher.find()) {
                    balance = Double.parseDouble(balanceMatcher.group(1).replace(",", "").replace(" ", ""));
                }
            }

            if (transactionAmount != null) {
                transactionDto.setTransactionCode(transactionCode);
                transactionDto.setTransactionType(transactionType);
                transactionDto.setTransactionAmount(transactionAmount);
                transactionDto.setTransactionCost(transactionCost);
                transactionDto.setDate(LocalDate.parse(transactionDate, formatter));
                transactionDto.setTime(LocalTime.parse(transactionTime));
                transactionDto.setSender(sender);
                transactionDto.setRecipient(recipient);
                transactionDto.setBalance(balance);

                Transaction transaction = new Transaction();
                transaction.setTransactionCode(transactionCode);
                transaction.setTransactionType(transactionType);
                transaction.setTransactionAmount(transactionAmount);
                transaction.setTransactionCost(transactionCost);
                transaction.setDate(LocalDate.parse(transactionDate, formatter));
                transaction.setTime(LocalTime.parse(transactionTime));
                transaction.setSender(sender);
                transaction.setRecipient(recipient);
                transaction.setBalance(balance);
                transaction.setUserAccount(userAccount);
                if(transactionAmount > 0) {
                    transaction.setEntity(transaction.getSender());
                } else if(transactionAmount < 0) {
                    transaction.setEntity(recipient);
                }

                List<String> existingTransactionCodes = transactionDao.getExistingTransactionCodes(userId);

                if(!existingTransactionCodes.contains(transaction.getTransactionCode())) {
                    Message message2 = new Message();
                    message2.setMessage(messageDto.getBody());
                    message2.setDate(LocalDate.parse(messageDto.getDate(), formatter));
                    message2.setTime(LocalTime.parse(messageDto.getTime()));
                    message2.setUserAccount(userAccount);
                    messageDao.addMessage(message2);
                    Transaction transaction1 = transactionDao.addTransaction(transaction);
                    List<TransactionCategory> categories = userAccount.getTransactionCategories();

                    if(!categories.isEmpty()) {
                        for(TransactionCategory transactionCategory : categories) {
                            categoryService.addTransactionToCategory(transaction1, transactionCategory.getId());
                        }
                    }
                }




            }
        }




        return transactionDto;
    }

    @Override
    public String getMessageTransactionCode(MessageDto message) {
        Matcher transactionCodeMatcher = Pattern.compile("\\b\\w{10}\\b").matcher(message.getBody());
        String transactionCode = transactionCodeMatcher.group();

        return transactionCode;
    }
    @Override
    public List<String> getExistingTransactionCodes(Integer userId) {
        return transactionDao.getExistingTransactionCodes(userId);
    }

    @Transactional
    @Override
    public String updateTransaction(TransactionEditDto transactionEditDto) {
        Transaction transaction = transactionDao.getTransaction(transactionEditDto.getTransactionId());
        transaction.setNickName(transactionEditDto.getNickName());

        List<Transaction> transactions = transactionDao.getUserTransactions(transactionEditDto.getUserId(), transactionEditDto.getEntity(), null, null, null, true, "2001-03-06", LocalDate.now().toString());

        List<TransactionCategory> categories = transaction.getCategories();

        if(!categories.isEmpty()) {
            for(TransactionCategory category : categories) {
                List<CategoryKeyword> categoryKeywords = category.getKeywords();
                for(CategoryKeyword keyword : categoryKeywords) {
                    if(keyword.getKeyword().equals(transaction.getSender()) || keyword.getKeyword().equals(transaction.getRecipient())) {
                        keyword.setNickName(transaction.getNickName());
                        categoryDao.updateKeyword(keyword);
                    }
                }
            }
        }

        for(Transaction transaction1 : transactions) {
            transaction1.setNickName(transactionEditDto.getNickName());
            transactionDao.updateTransaction(transaction1);
        }

        return "Transactions updated";
    }

    @Override
    public List<TransactionDto> getTransactions(Integer userId, String entity) {
        List<Transaction> transactions = transactionDao.getTransactions(userId, entity);
        List<TransactionDto> processedTransactions = new ArrayList<>();

        for(Transaction transaction : transactions) {
            processedTransactions.add(transactionToTransactionDto(transaction));
        }
        return processedTransactions;
    }

    @Override
    public Map<Object, Object> getUserTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean latest, String startDate, String endDate) {
        List<Transaction> transactions = transactionDao.getUserTransactions(userId, entity, categoryId, budgetId, transactionType, latest, startDate, endDate);
        List<TransactionDto> transformedTransactions = new ArrayList<>();
        Map<Object, Object> transactionsMap = new HashMap<>();
        Double totalMoneyOut = 0.0;
        Double totalMoneyIn = 0.0;
        for(Transaction transaction : transactions) {
            if(transaction.getTransactionAmount().toString().startsWith("-")) {
                totalMoneyOut = totalMoneyOut + Math.abs(transaction.getTransactionAmount());
            } else {
                totalMoneyIn = totalMoneyIn + Math.abs(transaction.getTransactionAmount());
            }
            transformedTransactions.add(transactionToTransactionDto(transaction));
        }
        transactionsMap.put("totalMoneyIn", totalMoneyIn);
        transactionsMap.put("totalMoneyOut", totalMoneyOut);
        transactionsMap.put("transactions", transformedTransactions);
        return transactionsMap;
    }

    @Override
    public Map<Object, Object> getUserTransactionsSorted(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean moneyIn, Boolean orderByAmount, Boolean ascendingOrder, String startDate, String endDate) {
        List<Object[]> result = transactionDao.getUserTransactionsSorted(userId, entity, categoryId, budgetId, transactionType, moneyIn, orderByAmount, ascendingOrder, startDate, endDate);
        List<Map<String, Object>> transformedResult = new ArrayList<>();
        Map<Object, Object> transactionsMap = new HashMap<>();
        Double totalMoneyIn = 0.0;
        Double totalMoneyOut = 0.0;
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("nickName", row[1]);
            map.put("transactionType", row[2]);
            map.put("times", row[3]);

            if(moneyIn) {
                totalMoneyIn = totalMoneyIn + (Double) row[4];
                map.put("amount", row[4]);
            } else {
                totalMoneyOut = totalMoneyOut + Math.abs((Double) row[4]);
                map.put("amount", "-"+row[4]);
            }

            map.put("transactionCost", row[5]);
            transformedResult.add(map);
        }

        transactionsMap.put("totalMoneyIn", totalMoneyIn);
        transactionsMap.put("totalMoneyOut", totalMoneyOut);

        transactionsMap.put("transactions", transformedResult);

        return transactionsMap;
    }

    @Override
    public Map<Object, Object> getGroupedByDateTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) {
        List<Object[]> result = transactionDao.getGroupedByDateTransactions(userId, entity, categoryId, budgetId, transactionType, startDate, endDate);
        List<Map<String, Object>> transformedResult = new ArrayList<>();
        Map<Object, Object> transactionsMap = new HashMap<>();
        Double totalMoneyIn = 0.0;
        Double totalMoneyOut = 0.0;
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0]);
            map.put("times", row[1]);
            map.put("moneyIn", row[2]);
            map.put("moneyOut", row[3]);

            totalMoneyIn = totalMoneyIn + (Double) row[2];
            totalMoneyOut = totalMoneyOut + Math.abs((Double) row[3]);

            map.put("transactionCost", row[4]);
            transformedResult.add(map);
        }

        transactionsMap.put("totalMoneyIn", totalMoneyIn);
        transactionsMap.put("totalMoneyOut", totalMoneyOut);

        transactionsMap.put("transactions", transformedResult);

        return transactionsMap;
    }

    @Override
    public Map<Object, Object> getUserTransactionsSortedByFrequency(Integer userId, String entity, Integer categoryId, String transactionType, Boolean moneyIn, Boolean ascendingOrder, String startDate, String endDate) {
        List<Object[]> result = transactionDao.getUserTransactionsSortedByFrequency(userId, entity, categoryId, transactionType, moneyIn, ascendingOrder, startDate, endDate);
        List<Map<String, Object>> transformedResult = new ArrayList<>();
        Map<Object, Object> transactionsMap = new HashMap<>();
        Double totalMoneyIn = 0.0;
        Double totalMoneyOut = 0.0;
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("times", row[1]);
            map.put("amount", row[2]);
            if(map.get("amount") instanceof Double) {
                if(moneyIn) {
                    totalMoneyIn = totalMoneyIn + (Double) map.get("amount");
                } else {
                    totalMoneyOut = totalMoneyOut + Math.abs((Double) map.get("amount"));
                }

            }
            transformedResult.add(map);
        }
        if(moneyIn) {
            transactionsMap.put("totalMoneyIn", totalMoneyIn);
        } else {
            transactionsMap.put("totalMoneyOut", totalMoneyOut);
        }

        transactionsMap.put("transactions", transformedResult);

        return transactionsMap;
    }

    @Override
    public Double getCurrentBalance(Integer userId) {
        return transactionDao.getCurrentBalance(userId);
    }

    @Override
    public Map<Object, Object> getExpenditure(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, Boolean moneyIn, Boolean latest, String startDate, String endDate) {
        List<Transaction> transactions = transactionDao.getExpenditure(userId, entity, categoryId, budgetId, transactionType, moneyIn, latest, startDate, endDate);
        List<TransactionDto> transactionDtos = new ArrayList<>();
        Map<Object, Object> transactionsMap = new HashMap<>();


        Double totalMoneyOut = 0.0;
        Double totalMoneyIn = 0.0;
        for(Transaction transaction : transactions) {
            if(transaction.getTransactionAmount().toString().startsWith("-")) {
                totalMoneyOut = totalMoneyOut + Math.abs(transaction.getTransactionAmount());
            } else {
                System.out.println("Sender: "+transaction.getSender()+" Recipient: "+transaction.getRecipient());
                totalMoneyIn = totalMoneyIn + Math.abs(transaction.getTransactionAmount());
            }
            transactionDtos.add(transactionToTransactionDto(transaction));
        }
        transactionsMap.put("totalMoneyIn", totalMoneyIn);
        transactionsMap.put("totalMoneyOut", totalMoneyOut);
        transactionsMap.put("transactions", transactionDtos);
        return transactionsMap;
    }

    @Override
    public Map<Object, Object> getGroupedByEntityTransactions(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) {
        List<Object[]> result = transactionDao.getGroupedByEntityTransactions(userId, entity, categoryId, budgetId, transactionType, startDate, endDate);
        Double totalMoneyIn = 0.0;
        Double totalMoneyOut = 0.0;
        List<Map<String, Object>> transformedResult = new ArrayList<>();
        Map<Object, Object> transactionsMap = new HashMap<>();
        for (Object[] row : result) {
            System.out.println("GROUPED TRANSACTIONS");
            System.out.println(Arrays.toString(row));
            Map<String, Object> map = new HashMap<>();
            map.put("nickName", row[0]);
            map.put("transactionType", row[1]);
            map.put("entity", row[2]);
            map.put("times", row[3]);
            map.put("timesIn", row[4]);
            map.put("timesOut", row[5]);
            map.put("totalIn", row[6]);
            map.put("totalOut", row[7]);
            map.put("transactionCost", row[8]);

            totalMoneyIn = totalMoneyIn + (Double) row[6];
            totalMoneyOut = totalMoneyOut + Math.abs((Double) row[7]);

            transformedResult.add(map);
        }
        transactionsMap.put("totalMoneyIn", totalMoneyIn);
        transactionsMap.put("totalMoneyOut", totalMoneyOut);
        transactionsMap.put("transactions", transformedResult);
        return transactionsMap;
    }

    @Override
    public byte[] generateAllTransactionsReport(Integer userId, String entity, Integer categoryId, Integer budgetId, String transactionType, String startDate, String endDate) throws JRException, ParseException {
        // Path to the JRXML file
//        String jrxmlPath = "/home/mbogo/Desktop/CashLedger/CashLedger/src/main/java/com/app/CashLedger/reportModel/AllTransactionsReportModel.jrxml";

        // Get user account and transactions
        UserAccount userAccount = userAccountDao.getUser(userId);
        List<Transaction> transactions = transactionDao.getUserTransactions(userId, entity, categoryId, budgetId, transactionType, true, startDate, endDate);
        List<AllTransactionsReportModel> allTransactionsReportModel = new ArrayList<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");

        // Parse the input dates
        Date start = inputFormat.parse(startDate);
        Date end = inputFormat.parse(endDate);

        // Format the dates to the desired output format
        String formattedStartDate = outputFormat.format(start);
        String formattedEndDate = outputFormat.format(end);

        Double totalIn = 0.0;
        Double totalOut = 0.0;
        Double totalTransactionCost = 0.0;

        // Process each transaction
        for (Transaction transaction : transactions) {
            List<String> categoryNames = new ArrayList<>();
            String moneyIn = "-";
            String moneyOut = "-";
            String transactionCost = "-";
            if (transaction.getTransactionAmount() > 0) {
                totalIn = totalIn + transaction.getTransactionAmount();
                moneyIn = "Ksh" + transaction.getTransactionAmount();
            } else if (transaction.getTransactionAmount() < 0) {
                totalOut = totalOut + Math.abs(transaction.getTransactionAmount());
                totalTransactionCost = totalTransactionCost + Math.abs(transaction.getTransactionCost());
                moneyOut = "Ksh" + Math.abs(transaction.getTransactionAmount());
                transactionCost = "Ksh" + Math.abs(transaction.getTransactionCost());
            }
            if(!transaction.getCategories().isEmpty()) {
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

        // Create a data source for JasperReports
        JRBeanCollectionDataSource allTransactionsDataSource = new JRBeanCollectionDataSource(allTransactionsReportModel);

        // Parameters for the report
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("owner", userAccount.getFname() + " " + userAccount.getLname() + ", " + userAccount.getPhoneNumber());
        parameters.put("startDate", formattedStartDate);
        parameters.put("endDate", formattedEndDate);
        parameters.put("totalIn", "Ksh" + String.format("%.2f", totalIn));
        parameters.put("totalOut", "Ksh" + String.format("%.2f", totalOut));
        parameters.put("totalTransactionCost", "Ksh" + String.format("%.2f", totalTransactionCost));
        parameters.put("size", transactions.size());
        parameters.put("allTransactionsDataset", allTransactionsDataSource);

        // Corrected path for the JRXML file
        String jrxmlPath = "/templates/AllTransactionsReport.jrxml";

        try (InputStream jrxmlInput = this.getClass().getResourceAsStream(jrxmlPath);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (jrxmlInput == null) {
                throw new IllegalStateException("JRXML file not found at path: " + jrxmlPath);
            }
            JasperReport report = JasperCompileManager.compileReport(jrxmlInput);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            JasperExportManager.exportReportToPdfStream(print, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JRException("Error generating report", e);
        }



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
                .recipient(transaction.getRecipient())
                .nickName(transaction.getNickName())
                .entity(transaction.getEntity())
                .balance(transaction.getBalance())
                .categories(categories)
                .build();
        return transactionDto;
    }
}
