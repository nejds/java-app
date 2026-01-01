package com.example.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
  private final String url;
  private final String user;
  private final String password;

  public Database(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;
  }

  public Connection connect() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }
}
