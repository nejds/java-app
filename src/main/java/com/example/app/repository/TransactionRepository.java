package com.example.app.repository;

import com.example.app.model.Transaction;
import java.sql.SQLException;
import java.util.List;

public interface TransactionRepository {
  long create(long userId, int amount, boolean income) throws SQLException;

  Transaction get(long id) throws SQLException;

  boolean delete(long id) throws SQLException;

  int getNetBalance(long userId) throws SQLException;

  List<Transaction> listByUser(long userId) throws SQLException;
}
