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
          id BIGSERIAL PRIMARY KEY,
          username TEXT NOT NULL UNIQUE
        )
        """;
    String transactionsSql = """
        CREATE TABLE IF NOT EXISTS transactions (
          id BIGSERIAL PRIMARY KEY,
          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
          amount_cents INTEGER NOT NULL,
          type TEXT NOT NULL CHECK (type IN ('income','expense')),
          created_at TIMESTAMP NOT NULL DEFAULT now()
        )
        """;

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(usersSql);
      statement.executeUpdate(transactionsSql);
    }
  }

  private static long createUser(Connection connection, String username) throws SQLException {
    String sql = "INSERT INTO users (username) VALUES (?) RETURNING id";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, username);
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getLong(1);
      }
    }
  }

  private static User getUser(Connection connection, long id) throws SQLException {
    String sql = "SELECT id, username FROM users WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }
        return new User(resultSet.getLong("id"), resultSet.getString("username"));
      }
    }
  }

  private static boolean deleteUser(Connection connection, long id) throws SQLException {
    String sql = "DELETE FROM users WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      return statement.executeUpdate() == 1;
    }
  }

  private static long createTransaction(Connection connection, long userId, int amountCents, String type)
      throws SQLException {
    String sql = """
        INSERT INTO transactions (user_id, amount_cents, type)
        VALUES (?, ?, ?)
        RETURNING id
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, userId);
      statement.setInt(2, amountCents);
      statement.setString(3, type);
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getLong(1);
      }
    }
  }

  private static Transaction getTransaction(Connection connection, long id) throws SQLException {
    String sql = """
        SELECT id, user_id, amount_cents, type, created_at
        FROM transactions
        WHERE id = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }
        return new Transaction(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getInt("amount_cents"),
            resultSet.getString("type"),
            resultSet.getTimestamp("created_at"));
      }
    }
  }

  private static boolean deleteTransaction(Connection connection, long id) throws SQLException {
    String sql = "DELETE FROM transactions WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      return statement.executeUpdate() == 1;
    }
  }

  private static void runDemo(Connection connection) throws SQLException {
    long userId = createUser(connection, "gustav");
    long transactionId = createTransaction(connection, userId, 200, "income");

    User user = getUser(connection, userId);
    Transaction transaction = getTransaction(connection, transactionId);
    System.out.println("User: " + user);
    System.out.println("Transaction: " + transaction);

    // deleteTransaction(connection, transactionId);
    // deleteUser(connection, userId);
  }

  private record User(long id, String username) {}

  private record Transaction(long id, long userId, int amountCents, String type, Timestamp createdAt) {}
}
