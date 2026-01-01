package com.example.app.repository;

import com.example.app.model.User;
import java.sql.SQLException;

public interface UserRepository {
  User create(String username) throws SQLException;

  User getByUsernameOrCreate(String username) throws SQLException;

  boolean delete(long userId) throws SQLException;
}
