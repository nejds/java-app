package com.example.app.repository;

import com.example.app.model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JdbcTransactionRepository implements TransactionRepository {
  private final Connection connection;

  public JdbcTransactionRepository(Connection connection) {
    this.connection = connection;
  }

  @Override
  public long create(long userId, int amount, boolean income) throws SQLException {
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

  @Override
  public Transaction get(long id) throws SQLException {
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

  @Override
  public boolean delete(long id) throws SQLException {
    String sql = "DELETE FROM transactions WHERE transaction_id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setLong(1, id);
    try (statement) {
      return statement.executeUpdate() == 1;
    }
  }
}
