package com.example.app.service;

import com.example.app.model.User;
import com.example.app.repository.TransactionRepository;
import java.sql.SQLException;

public final class AnalyticsService {
  private final TransactionRepository transactions;

  public AnalyticsService(TransactionRepository transactions) {
    this.transactions = transactions;
  }

  public int getNetBalance(User user) throws SQLException {
    return transactions.getNetBalance(user.id());
  }
}
