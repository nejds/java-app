package com.example.app.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Schema {
  private Schema() {
    // Utility class.
  }

  public static void ensure(Connection connection) throws SQLException {
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
}
