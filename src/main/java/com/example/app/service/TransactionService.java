package com.example.app.service;

import com.example.app.model.Transaction;
import com.example.app.model.User;
import com.example.app.repository.TransactionRepository;
import com.example.app.repository.UserRepository;
import java.sql.SQLException;

public final class TransactionService {
  private final UserRepository users;
  private final TransactionRepository transactions;

  public TransactionService(UserRepository users, TransactionRepository transactions) {
    this.users = users;
    this.transactions = transactions;
  }

  public User getOrCreateUser(String username) throws SQLException {
    return users.getByUsernameOrCreate(username);
  }

  public Transaction createTransaction(User user, int amount, boolean income) throws SQLException {
    long id = transactions.create(user.id(), amount, income);
    return transactions.get(id);
  }

  public boolean deleteTransaction(long id) throws SQLException {
    return transactions.delete(id);
  }
}
