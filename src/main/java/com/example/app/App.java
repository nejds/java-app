package com.example.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public final class App {
  private App() {
    // Prevent instantiation.
  }

  public static void main(String[] args) throws SQLException {
    String url = getenv("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
    String user = getenv("DB_USER", "postgres");
    String password = getenv("DB_PASSWORD", "mysecretpassword");

    try (Connection connection = DriverManager.getConnection(url, user, password)) {
      createTables(connection);
      runDemo(connection);
    }
  }

  private static String getenv(String key, String defaultValue) {
    String value = System.getenv(key);
    return (value == null || value.isBlank()) ? defaultValue : value;
  }

  private static void createTables(Connection connection) throws SQLException {
    String usersSql = """
        CREATE TABLE IF NOT EXISTS users (
          user_id BIGSERIAL PRIMARY KEY,
          username TEXT NOT NULL UNIQUE
        )
        """;
    String transactionsSql = """
        CREATE TABLE IF NOT EXISTS transactions (
          transaction_id BIGSERIAL PRIMARY KEY,
          user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
          amount INTEGER NOT NULL,
          income BOOLEAN NOT NULL,
          created_at TIMESTAMP NOT NULL DEFAULT now()
        )
        """;

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(usersSql);
      statement.executeUpdate(transactionsSql);
    }
  }

  private static User createUser(Connection connection, String username) throws SQLException {
    String sql = "INSERT INTO users (username) VALUES (?) RETURNING user_id";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, username);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      long id = resultSet.getLong(1);
      return new User(id, username);
    }
  }

  private static User getUserByUsername(Connection connection, String username) throws SQLException {
    String sql = "SELECT user_id, username FROM users WHERE username = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, username);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      if (!resultSet.next()) {
        return createUser(connection, username);
      }
      return new User(resultSet.getLong("user_id"), resultSet.getString("username"));
    }
  }

  private static long createTransaction(Connection connection, long userId, int amount, boolean income)
      throws SQLException {
    String sql = """
        INSERT INTO transactions (user_id, amount, income)
        VALUES (?, ?, ?)
        RETURNING transaction_id
        """;
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setLong(1, userId);
    statement.setInt(2, amount);
    statement.setBoolean(3, income);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getLong(1);
    }
  }

  private static Transaction getTransaction(Connection connection, long id) throws SQLException {
    String sql = """
        SELECT transaction_id, user_id, amount, income, created_at
        FROM transactions
        WHERE transaction_id = ?
        """;
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setLong(1, id);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      if (!resultSet.next()) {
        return null;
      }
      return new Transaction(
          resultSet.getLong("transaction_id"),
          resultSet.getLong("user_id"),
          resultSet.getInt("amount"),
          resultSet.getBoolean("income"),
          resultSet.getTimestamp("created_at"));
    }
  }

  private static boolean deleteTransaction(Connection connection, long id) throws SQLException {
    String sql = "DELETE FROM transactions WHERE transaction_id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setLong(1, id);
    try (statement) {
      return statement.executeUpdate() == 1;
    }
  }

  private static void runDemo(Connection connection) throws SQLException {

    // Login as user
    String userName = "gustav";
    User user = getUserByUsername(connection, userName);
    System.out.println("User: " + user);
  
    // Create transaction
    long transactionId = createTransaction(connection, user.id(), 4530, true);

    // Retrieve transaction
    Transaction transaction = getTransaction(connection, transactionId);
    System.out.println("User: " + user);
    System.out.println("Transaction: " + transaction);

    // Delete said transaction
    // deleteTransaction(connection, transactionId);
  }

  private record User(long id, String username) {}

  private record Transaction(long id, long userId, int amount, boolean income, Timestamp createdAt) {}
}
