package com.example.app;

import com.example.app.db.Database;
import com.example.app.db.Schema;
import com.example.app.model.Transaction;
import com.example.app.model.User;
import com.example.app.repository.JdbcTransactionRepository;
import com.example.app.repository.JdbcUserRepository;
import com.example.app.repository.TransactionRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.AnalyticsService;
import com.example.app.service.TransactionService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class App {
  private App() {
    // Prevent instantiation.
  }

  public static void main(String[] args) throws SQLException {
    String url = getenv("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
    String user = getenv("DB_USER", "postgres");
    String password = getenv("DB_PASSWORD", "mysecretpassword");

    Database database = new Database(url, user, password);
    try (Connection connection = database.connect()) {
      Schema.ensure(connection);
      runDemo(connection);
    }
  }

  private static String getenv(String key, String defaultValue) {
    String value = System.getenv(key);
    return (value == null || value.isBlank()) ? defaultValue : value;
  }

  private static void runDemo(Connection connection) throws SQLException {
    UserRepository userRepository = new JdbcUserRepository(connection);
    TransactionRepository transactionRepository = new JdbcTransactionRepository(connection);
    TransactionService transactionService = new TransactionService(userRepository, transactionRepository);
    AnalyticsService analyticsService = new AnalyticsService(transactionRepository);

    // Login as user
    String userName = "gustav";
    User user = transactionService.getOrCreateUser(userName);

    try (Scanner scanner = new Scanner(System.in)) {
      boolean running = true;
      while (running) {
        System.out.println();
        System.out.println("Choose a service:");
        System.out.println("1) Add income");
        System.out.println("2) Add expense");
        System.out.println("3) List transactions");
        System.out.println("4) Show net balance");
        System.out.println("5) Remove income");
        System.out.println("6) Remove expense");
        System.out.println("7) Delete user");
        System.out.println("0) Exit");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        int choice;
        try {
          choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
          System.out.println("Invalid selection. Enter a number from the list.");
          continue;
        }

        switch (choice) {
          case 1 -> {
            int amount = readAmount(scanner, "Enter income amount: ");
            transactionService.addIncome(user, amount);
            System.out.println("Income added.");
          }
          case 2 -> {
            int amount = readAmount(scanner, "Enter expense amount: ");
            transactionService.addExpense(user, amount);
            System.out.println("Expense added.");
          }
          case 3 -> {
            transactionService.listTransactions(user).forEach(t -> {
              System.out.println("Transaction: " + t);
            });
          }
          case 4 -> System.out.println("Net balance: " + analyticsService.getNetBalance(user));
          case 5 -> {
            List<Transaction> incomes = listByType(transactionService, user, true);
            if (incomes.isEmpty()) {
              System.out.println("No income transactions to remove.");
              break;
            }
            int index = readIndex(scanner, "Pick income to remove (1-" + incomes.size() + "): ", incomes.size());
            Transaction selected = incomes.get(index - 1);
            boolean removed = transactionService.removeIncome(user, selected.id());
            System.out.println(removed ? "Income removed." : "Failed to remove income.");
          }
          case 6 -> {
            List<Transaction> expenses = listByType(transactionService, user, false);
            if (expenses.isEmpty()) {
              System.out.println("No expense transactions to remove.");
              break;
            }
            int index = readIndex(scanner, "Pick expense to remove (1-" + expenses.size() + "): ", expenses.size());
            Transaction selected = expenses.get(index - 1);
            boolean removed = transactionService.removeExpense(user, selected.id());
            System.out.println(removed ? "Expense removed." : "Failed to remove expense.");
          }
          case 7 -> {
            boolean deleted = transactionService.deleteUser(user.id());
            System.out.println(deleted ? "User deleted." : "User not found.");
          }
          case 0 -> running = false;
          default -> System.out.println("Invalid selection. Enter a number from the list.");
        }
      }
    }
  }

  private static int readAmount(Scanner scanner, String prompt) {
    while (true) {
      System.out.print(prompt);
      String input = scanner.nextLine().trim();
      try {
        return Integer.parseInt(input);
      } catch (NumberFormatException e) {
        System.out.println("Invalid amount. Enter a whole number.");
      }
    }
  }

  private static int readIndex(Scanner scanner, String prompt, int max) {
    while (true) {
      System.out.print(prompt);
      String input = scanner.nextLine().trim();
      int index;
      try {
        index = Integer.parseInt(input);
      } catch (NumberFormatException e) {
        System.out.println("Invalid selection. Enter a number from the list.");
        continue;
      }
      if (index < 1 || index > max) {
        System.out.println("Invalid selection. Enter a number from the list.");
        continue;
      }
      return index;
    }
  }

  private static List<Transaction> listByType(
      TransactionService transactionService, User user, boolean income) throws SQLException {
    List<Transaction> matches = new ArrayList<>();
    int index = 1;
    for (Transaction transaction : transactionService.listTransactions(user)) {
      if (transaction.income() != income) {
        continue;
      }
      matches.add(transaction);
      System.out.println(index + ") " + transaction);
      index++;
    }
    return matches;
  }
}
