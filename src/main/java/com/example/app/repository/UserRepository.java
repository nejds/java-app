package com.example.app.repository;

import com.example.app.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class UserRepository {
  private final Connection connection;

  public UserRepository(Connection connection) {
    this.connection = connection;
  }

  public User create(String username) throws SQLException {
    String sql = "INSERT INTO users (username) VALUES (?) RETURNING user_id";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, username);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      long id = resultSet.getLong(1);
      return new User(id, username);
    }
  }

  public User getByUsernameOrCreate(String username) throws SQLException {
    String sql = "SELECT user_id, username FROM users WHERE username = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, username);
    try (statement; ResultSet resultSet = statement.executeQuery()) {
      if (!resultSet.next()) {
        return create(username);
      }
      return new User(resultSet.getLong("user_id"), resultSet.getString("username"));
    }
  }
}
