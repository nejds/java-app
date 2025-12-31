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
          userID BIGSERIAL PRIMARY KEY,
          username TEXT NOT NULL UNIQUE
        )
        """;
    String transactionsSql = """
        CREATE TABLE IF NOT EXISTS transactions (
          transactionID BIGSERIAL PRIMARY KEY,
          userID BIGINT NOT NULL REFERENCES users(userID) ON DELETE CASCADE,
          amount INTEGER NOT NULL,
          type TEXT NOT NULL CHECK (type IN ('income','expense')),
          created_at TIMESTAMP NOT NULL DEFAULT now()
        )
        """;

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(usersSql);
      statement.executeUpdate(transactionsSql);
    }
  }

  private static User createUser(Connection connection, String username) throws SQLException {
    String sql = "INSERT INTO users (username) VALUES (?) RETURNING userID";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, username);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      long id = resultSet.getLong(1);
      return new User(id, username);
    }
  }

  private static User getUserByUsername(Connection connection, String username) throws SQLException {
    String sql = "SELECT userID, username FROM users WHERE username = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, username);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      if (!resultSet.next()) {
        return createUser(connection, username);
      }
      return new User(resultSet.getLong("userID"), resultSet.getString("username"));
    }
  }

  private static long createTransaction(Connection connection, long userId, int amountCents, String type)
      throws SQLException {
    String sql = """
        INSERT INTO transactions (userID, amount, type)
        VALUES (?, ?, ?)
        RETURNING transactionID
        """;
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setLong(1, userId);
    statement.setInt(2, amountCents);
    statement.setString(3, type);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getLong(1);
    }
  }

  private static Transaction getTransaction(Connection connection, long id) throws SQLException {
    String sql = """
        SELECT transactionID, userID, amount, type, created_at
        FROM transactions
        WHERE transactionID = ?
        """;
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setLong(1, id);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      if (!resultSet.next()) {
        return null;
      }
      return new Transaction(
          resultSet.getLong("transactionID"),
          resultSet.getLong("userID"),
          resultSet.getInt("amount"),
          resultSet.getString("type"),
          resultSet.getTimestamp("created_at"));
    }
  }

  private static boolean deleteTransaction(Connection connection, long id) throws SQLException {
    String sql = "DELETE FROM transactions WHERE transactionID = ?";
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
    long transactionId = createTransaction(connection, user.id(), 4530, "income");

    // Retrieve transaction
    Transaction transaction = getTransaction(connection, transactionId);
    System.out.println("User: " + user);
    System.out.println("Transaction: " + transaction);

    // Delete said transaction
    // deleteTransaction(connection, transactionId);
  }

  private record User(long id, String username) {}

  private record Transaction(long id, long userId, int amountCents, String type, Timestamp createdAt) {}
}
