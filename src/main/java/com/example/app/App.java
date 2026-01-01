package com.example.app;

import com.example.app.db.Database;
import com.example.app.db.Schema;
import com.example.app.model.Transaction;
import com.example.app.model.User;
import com.example.app.repository.JdbcTransactionRepository;
import com.example.app.repository.JdbcUserRepository;
import com.example.app.repository.TransactionRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.AnalyticsService;
import com.example.app.service.TransactionService;
import java.sql.Connection;
import java.sql.SQLException;

public final class App {
  private App() {
    // Prevent instantiation.
  }

  public static void main(String[] args) throws SQLException {
    String url = getenv("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
    String user = getenv("DB_USER", "postgres");
    String password = getenv("DB_PASSWORD", "mysecretpassword");

    Database database = new Database(url, user, password);
    try (Connection connection = database.connect()) {
      Schema.ensure(connection);
      runDemo(connection);
    }
  }

  private static String getenv(String key, String defaultValue) {
    String value = System.getenv(key);
    return (value == null || value.isBlank()) ? defaultValue : value;
  }

  private static void runDemo(Connection connection) throws SQLException {
    UserRepository userRepository = new JdbcUserRepository(connection);
    TransactionRepository transactionRepository = new JdbcTransactionRepository(connection);
    TransactionService transactionService = new TransactionService(userRepository, transactionRepository);
    AnalyticsService analyticsService = new AnalyticsService(transactionRepository);

    // Login as user
    String userName = "gustav";
    User user = transactionService.getOrCreateUser(userName);
    System.out.println("User: " + user);
  
    // Create transaction
    Transaction transaction = transactionService.addIncome(user, 4530);

    // Retrieve transaction
    System.out.println("User: " + user);
    System.out.println("Transaction: " + transaction);
    System.out.println("Net balance: " + analyticsService.getNetBalance(user));

    // Delete said transaction
    // transactionService.deleteTransaction(transaction.id());
  }
}
