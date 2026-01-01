package com.example.app;

import com.example.app.db.Database;
import com.example.app.db.Schema;
import com.example.app.model.Transaction;
import com.example.app.model.User;
import com.example.app.repository.TransactionRepository;
import com.example.app.repository.UserRepository;
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
    UserRepository userRepository = new UserRepository(connection);
    TransactionRepository transactionRepository = new TransactionRepository(connection);

    // Login as user
    String userName = "gustav";
    User user = userRepository.getByUsernameOrCreate(userName);
    System.out.println("User: " + user);
  
    // Create transaction
    long transactionId = transactionRepository.create(user.id(), 4530, true);

    // Retrieve transaction
    Transaction transaction = transactionRepository.get(transactionId);
    System.out.println("User: " + user);
    System.out.println("Transaction: " + transaction);

    // Delete said transaction
    // transactionRepository.delete(transactionId);
  }
}
