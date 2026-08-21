package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class PortfolioApp {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n====================================");
            System.out.println(" INVESTMENT PORTFOLIO MANAGEMENT");
            System.out.println("====================================");

            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    register();
                    break;

                case 2:
                    login();
                    break;

                case 3:
                    System.out.println("Thank you!");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- REGISTER ----------------

    static void register() {

        System.out.println("\n--------- USER REGISTRATION ---------");

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        String hashedPassword = PasswordUtil.hashPassword(password);

        String sql =
                "INSERT INTO users(name, email, password) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);

            statement.executeUpdate();

            System.out.println("\nRegistration successful!");

        } catch (Exception e) {

            System.out.println("\nRegistration failed!");

            if (e.getMessage() != null &&
                    e.getMessage().contains("Duplicate")) {

                System.out.println(
                        "Email already registered. Please use another email."
                );

            } else {
                e.printStackTrace();
            }
        }
    }

    // ---------------- LOGIN ----------------

    static void login() {

        System.out.println("\n------------- LOGIN -------------");

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        String hashedPassword = PasswordUtil.hashPassword(password);

        String sql =
                "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, hashedPassword);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                int userId = result.getInt("user_id");
                String name = result.getString("name");

                System.out.println("\nLogin successful!");
                System.out.println("Welcome, " + name);

                userMenu(userId);

            } else {

                System.out.println("\nInvalid email or password.");
            }

        } catch (Exception e) {

            System.out.println("\nLogin failed!");
            e.printStackTrace();
        }
    }

    // ---------------- USER MENU ----------------

    static void userMenu(int userId) {

        while (true) {

            System.out.println("\n====================================");
            System.out.println("              USER MENU");
            System.out.println("====================================");

            System.out.println("1. Add Investment");
            System.out.println("2. View Investments");
            System.out.println("3. Update Investment");
            System.out.println("4. Delete Investment");
            System.out.println("5. Portfolio Summary");
            System.out.println("6. Logout");

            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    addInvestment(userId);
                    break;

                case 2:
                    viewInvestments(userId);
                    break;

                case 3:
                    updateInvestment(userId);
                    break;

                case 4:
                    deleteInvestment(userId);
                    break;

                case 5:
                    portfolioSummary(userId);
                    break;

                case 6:
                    System.out.println(
                            "Logged out successfully."
                    );
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- ADD INVESTMENT ----------------

    static void addInvestment(int userId) {

        System.out.println("\n--------- ADD INVESTMENT ---------");

        System.out.print("Enter asset name: ");
        String assetName = scanner.nextLine();

        System.out.print("Enter asset type: ");
        String assetType = scanner.nextLine();

        System.out.print("Enter quantity: ");
        double quantity = scanner.nextDouble();

        System.out.print("Enter purchase price: ");
        double purchasePrice = scanner.nextDouble();

        System.out.print("Enter current price: ");
        double currentPrice = scanner.nextDouble();

        scanner.nextLine();

        System.out.print("Enter purchase date (YYYY-MM-DD): ");
        String purchaseDate = scanner.nextLine();

        String sql =
                "INSERT INTO investments " +
                        "(user_id, asset_name, asset_type, quantity, " +
                        "purchase_price, current_price, purchase_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, assetName);
            statement.setString(3, assetType);
            statement.setDouble(4, quantity);
            statement.setDouble(5, purchasePrice);
            statement.setDouble(6, currentPrice);
            statement.setDate(
                    7,
                    java.sql.Date.valueOf(purchaseDate)
            );

            statement.executeUpdate();

            System.out.println(
                    "\nInvestment added successfully!"
            );

        } catch (Exception e) {

            System.out.println(
                    "\nFailed to add investment."
            );

            e.printStackTrace();
        }
    }

    // ---------------- VIEW INVESTMENTS ----------------

    static void viewInvestments(int userId) {

        System.out.println("\n--------- MY INVESTMENTS ---------");

        String sql =
                "SELECT * FROM investments WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result = statement.executeQuery();

            System.out.printf(
                    "%-5s %-15s %-12s %-10s %-15s %-15s %-15s%n",
                    "ID",
                    "Asset",
                    "Type",
                    "Quantity",
                    "Purchase",
                    "Current",
                    "Date"
            );

            System.out.println(
                    "--------------------------------------------------------------------------------"
            );

            boolean found = false;

            while (result.next()) {

                found = true;

                System.out.printf(
                        "%-5d %-15s %-12s %-10.2f %-15.2f %-15.2f %-15s%n",
                        result.getInt("investment_id"),
                        result.getString("asset_name"),
                        result.getString("asset_type"),
                        result.getDouble("quantity"),
                        result.getDouble("purchase_price"),
                        result.getDouble("current_price"),
                        result.getDate("purchase_date")
                );
            }

            if (!found) {
                System.out.println("No investments found.");
            }

        } catch (Exception e) {

            System.out.println(
                    "\nFailed to view investments."
            );

            e.printStackTrace();
        }
    }

    // ---------------- UPDATE INVESTMENT ----------------

    static void updateInvestment(int userId) {

        System.out.println("\n--------- UPDATE INVESTMENT ---------");

        viewInvestments(userId);

        System.out.print("\nEnter Investment ID to update: ");
        int investmentId = scanner.nextInt();

        scanner.nextLine();

        System.out.print("Enter new asset name: ");
        String assetName = scanner.nextLine();

        System.out.print("Enter new asset type: ");
        String assetType = scanner.nextLine();

        System.out.print("Enter new quantity: ");
        double quantity = scanner.nextDouble();

        System.out.print("Enter new purchase price: ");
        double purchasePrice = scanner.nextDouble();

        System.out.print("Enter new current price: ");
        double currentPrice = scanner.nextDouble();

        scanner.nextLine();

        System.out.print(
                "Enter new purchase date (YYYY-MM-DD): "
        );

        String purchaseDate = scanner.nextLine();

        String sql =
                "UPDATE investments SET " +
                        "asset_name = ?, " +
                        "asset_type = ?, " +
                        "quantity = ?, " +
                        "purchase_price = ?, " +
                        "current_price = ?, " +
                        "purchase_date = ? " +
                        "WHERE investment_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, assetName);
            statement.setString(2, assetType);
            statement.setDouble(3, quantity);
            statement.setDouble(4, purchasePrice);
            statement.setDouble(5, currentPrice);
            statement.setDate(
                    6,
                    java.sql.Date.valueOf(purchaseDate)
            );
            statement.setInt(7, investmentId);
            statement.setInt(8, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "\nInvestment updated successfully!"
                );

            } else {

                System.out.println(
                        "\nInvestment not found."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "\nFailed to update investment."
            );

            e.printStackTrace();
        }
    }

    // ---------------- DELETE INVESTMENT ----------------

    static void deleteInvestment(int userId) {

        System.out.println("\n--------- DELETE INVESTMENT ---------");

        viewInvestments(userId);

        System.out.print("\nEnter Investment ID to delete: ");
        int investmentId = scanner.nextInt();

        scanner.nextLine();

        String sql =
                "DELETE FROM investments " +
                        "WHERE investment_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, investmentId);
            statement.setInt(2, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "\nInvestment deleted successfully!"
                );

            } else {

                System.out.println(
                        "\nInvestment not found."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "\nFailed to delete investment."
            );

            e.printStackTrace();
        }
    }

    // ---------------- PORTFOLIO SUMMARY ----------------

    static void portfolioSummary(int userId) {

        System.out.println("\n--------- PORTFOLIO SUMMARY ---------");

        String sql =
                "SELECT " +
                        "SUM(quantity * purchase_price) AS total_invested, " +
                        "SUM(quantity * current_price) AS current_value " +
                        "FROM investments " +
                        "WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                double totalInvested =
                        result.getDouble("total_invested");

                double currentValue =
                        result.getDouble("current_value");

                double profitLoss =
                        currentValue - totalInvested;

                System.out.println(
                        "Total Invested : " +
                                String.format("%.2f", totalInvested)
                );

                System.out.println(
                        "Current Value  : " +
                                String.format("%.2f", currentValue)
                );

                System.out.println(
                        "Profit / Loss  : " +
                                String.format("%.2f", profitLoss)
                );

            }

        } catch (Exception e) {

            System.out.println(
                    "\nFailed to calculate portfolio summary."
            );

            e.printStackTrace();
        }
    }
}