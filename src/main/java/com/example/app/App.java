package com.example.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class App {
  private App() {
    // Prevent instantiation.
  }

  public static void main(String[] args) {
    String url = getenv("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
    String user = getenv("DB_USER", "postgres");
    String password = getenv("DB_PASSWORD", "mysecretpassword");

    try (Connection connection = DriverManager.getConnection(url, user, password)) {
      System.out.println("Connected to PostgreSQL.");
    } catch (SQLException ex) {
      System.err.println("Failed to connect to PostgreSQL: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  private static String getenv(String key, String defaultValue) {
    String value = System.getenv(key);
    return (value == null || value.isBlank()) ? defaultValue : value;
  }
}
