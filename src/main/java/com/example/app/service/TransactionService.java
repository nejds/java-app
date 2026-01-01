package com.example.app.service;

import com.example.app.model.Transaction;
import com.example.app.model.User;
import com.example.app.repository.TransactionRepository;
import com.example.app.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;

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

  public Transaction addExpense(User user, int amount) throws SQLException {
    return createTransaction(user, amount, false);
  }

  public Transaction addIncome(User user, int amount) throws SQLException {
    return createTransaction(user, amount, true);
  }

  public boolean removeExpense(User user, long transactionId) throws SQLException {
    return removeByType(user, transactionId, false);
  }

  public boolean removeIncome(User user, long transactionId) throws SQLException {
    return removeByType(user, transactionId, true);
  }

  public List<Transaction> listTransactions(User user) throws SQLException {
    return transactions.listByUser(user.id());
  }

  private boolean removeByType(User user, long transactionId, boolean income) throws SQLException {
    Transaction transaction = transactions.get(transactionId);
    if (transaction == null) {
      return false;
    }
    if (transaction.userId() != user.id() || transaction.income() != income) {
      return false;
    }
    return transactions.delete(transactionId);
  }
}
