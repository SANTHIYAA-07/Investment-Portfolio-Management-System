package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PortfolioWebServer {

    static int loggedInUserId = -1;

    public static void main(String[] args) throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(8080),
                        0
                );

        server.createContext(
                "/",
                PortfolioWebServer::home
        );

        server.createContext(
                "/login",
                PortfolioWebServer::login
        );

        server.createContext(
                "/logout",
                PortfolioWebServer::logout
        );

        server.createContext(
                "/addInvestment",
                PortfolioWebServer::addInvestment
        );

        server.createContext(
                "/investments",
                PortfolioWebServer::investments
        );

        server.createContext(
                "/updateInvestment",
                PortfolioWebServer::updateInvestment
        );

        server.createContext(
                "/deleteInvestment",
                PortfolioWebServer::deleteInvestment
        );

        server.createContext(
                "/summary",
                PortfolioWebServer::summary
        );

        server.setExecutor(null);

        System.out.println(
                "===================================="
        );

        System.out.println(
                " INVESTMENT PORTFOLIO WEB SERVER"
        );

        System.out.println(
                "===================================="
        );

        System.out.println(
                "Server started at:"
        );

        System.out.println(
                "http://localhost:8080"
        );

        server.start();
    }


    // =====================================================
    // HOME
    // =====================================================

    static void home(HttpExchange exchange)
            throws IOException {

        try {

            File file =
                    new File(
                            "src/main/resources/index.html"
                    );

            byte[] response =
                    Files.readAllBytes(
                            file.toPath()
                    );

            exchange.getResponseHeaders()
                    .set(
                            "Content-Type",
                            "text/html; charset=UTF-8"
                    );

            exchange.sendResponseHeaders(
                    200,
                    response.length
            );

            OutputStream output =
                    exchange.getResponseBody();

            output.write(response);
            output.close();

        } catch (Exception e) {

            e.printStackTrace();

            sendText(
                    exchange,
                    "Unable to load index.html"
            );
        }
    }


    // =====================================================
    // LOGIN
    // =====================================================

    static void login(HttpExchange exchange)
            throws IOException {

        try {

            String body =
                    readRequestBody(exchange);

            String email =
                    extract(
                            body,
                            "email"
                    );

            String password =
                    extract(
                            body,
                            "password"
                    );

            String hashedPassword =
                    PasswordUtil.hashPassword(
                            password
                    );

            String sql =
                    "SELECT user_id, name " +
                            "FROM users " +
                            "WHERE email = ? " +
                            "AND password = ?";


            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        email
                );

                statement.setString(
                        2,
                        hashedPassword
                );

                ResultSet result =
                        statement.executeQuery();


                if (result.next()) {

                    loggedInUserId =
                            result.getInt(
                                    "user_id"
                            );

                    String name =
                            result.getString(
                                    "name"
                            );

                    sendText(
                            exchange,
                            "Login successful! Welcome, "
                                    + name
                    );

                } else {

                    sendText(
                            exchange,
                            "Invalid email or password."
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendText(
                    exchange,
                    "Login failed: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    static void logout(HttpExchange exchange)
            throws IOException {

        loggedInUserId = -1;

        sendText(
                exchange,
                "Logged out successfully."
        );
    }


    // =====================================================
    // ADD INVESTMENT
    // =====================================================

    static void addInvestment(
            HttpExchange exchange)
            throws IOException {

        if (loggedInUserId == -1) {

            sendText(
                    exchange,
                    "Please login first."
            );

            return;
        }


        try {

            String body =
                    readRequestBody(exchange);


            String assetName =
                    extract(
                            body,
                            "assetName"
                    );

            String assetType =
                    extract(
                            body,
                            "assetType"
                    );

            String quantityText =
                    extract(
                            body,
                            "quantity"
                    );

            String purchasePriceText =
                    extract(
                            body,
                            "purchasePrice"
                    );

            String currentPriceText =
                    extract(
                            body,
                            "currentPrice"
                    );

            String purchaseDate =
                    extract(
                            body,
                            "purchaseDate"
                    );


            if (
                    assetName.isEmpty()
                            || assetType.isEmpty()
                            || quantityText.isEmpty()
                            || purchasePriceText.isEmpty()
                            || currentPriceText.isEmpty()
                            || purchaseDate.isEmpty()
            ) {

                sendText(
                        exchange,
                        "Please fill all fields."
                );

                return;
            }


            double quantity =
                    Double.parseDouble(
                            quantityText
                    );

            double purchasePrice =
                    Double.parseDouble(
                            purchasePriceText
                    );

            double currentPrice =
                    Double.parseDouble(
                            currentPriceText
                    );


            String sql =
                    "INSERT INTO investments " +
                            "(user_id, asset_name, asset_type, " +
                            "quantity, purchase_price, " +
                            "current_price, purchase_date) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";


            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        loggedInUserId
                );

                statement.setString(
                        2,
                        assetName
                );

                statement.setString(
                        3,
                        assetType
                );

                statement.setDouble(
                        4,
                        quantity
                );

                statement.setDouble(
                        5,
                        purchasePrice
                );

                statement.setDouble(
                        6,
                        currentPrice
                );

                statement.setDate(
                        7,
                        Date.valueOf(
                                purchaseDate
                        )
                );


                int rows =
                        statement.executeUpdate();


                if (rows > 0) {

                    sendText(
                            exchange,
                            "Investment added successfully!"
                    );

                } else {

                    sendText(
                            exchange,
                            "Investment was not added."
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendText(
                    exchange,
                    "Failed to add investment: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // VIEW INVESTMENTS
    // =====================================================

    static void investments(
            HttpExchange exchange)
            throws IOException {

        if (loggedInUserId == -1) {

            sendJson(
                    exchange,
                    "[]"
            );

            return;
        }


        String sql =
                "SELECT * FROM investments " +
                        "WHERE user_id = ? " +
                        "ORDER BY investment_id";


        List<String> data =
                new ArrayList<>();


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    loggedInUserId
            );

            ResultSet result =
                    statement.executeQuery();


            while (result.next()) {

                String assetName =
                        result.getString(
                                "asset_name"
                        );

                String assetType =
                        result.getString(
                                "asset_type"
                        );


                String json =
                        "{"
                                + "\"id\":"
                                + result.getInt(
                                "investment_id"
                        )
                                + ","

                                + "\"asset\":\""
                                + escapeJson(
                                assetName
                        )
                                + "\","

                                + "\"type\":\""
                                + escapeJson(
                                assetType
                        )
                                + "\","

                                + "\"quantity\":"
                                + result.getDouble(
                                "quantity"
                        )
                                + ","

                                + "\"purchase\":"
                                + result.getDouble(
                                "purchase_price"
                        )
                                + ","

                                + "\"current\":"
                                + result.getDouble(
                                "current_price"
                        )
                                + ","

                                + "\"date\":\""
                                + result.getDate(
                                "purchase_date"
                        )
                                + "\""

                                + "}";


                data.add(json);
            }


            sendJson(
                    exchange,
                    "["
                            + String.join(
                            ",",
                            data
                    )
                            + "]"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    "[]"
            );
        }
    }


    // =====================================================
    // UPDATE INVESTMENT
    // =====================================================

    static void updateInvestment(
            HttpExchange exchange)
            throws IOException {

        if (loggedInUserId == -1) {

            sendText(
                    exchange,
                    "Please login first."
            );

            return;
        }


        try {

            String body =
                    readRequestBody(exchange);


            String idText =
                    extract(
                            body,
                            "id"
                    );

            String assetName =
                    extract(
                            body,
                            "assetName"
                    );

            String assetType =
                    extract(
                            body,
                            "assetType"
                    );

            String quantityText =
                    extract(
                            body,
                            "quantity"
                    );

            String purchasePriceText =
                    extract(
                            body,
                            "purchasePrice"
                    );

            String currentPriceText =
                    extract(
                            body,
                            "currentPrice"
                    );

            String purchaseDate =
                    extract(
                            body,
                            "purchaseDate"
                    );


            if (
                    idText.isEmpty()
                            || assetName.isEmpty()
                            || assetType.isEmpty()
                            || quantityText.isEmpty()
                            || purchasePriceText.isEmpty()
                            || currentPriceText.isEmpty()
                            || purchaseDate.isEmpty()
            ) {

                sendText(
                        exchange,
                        "Please fill all fields."
                );

                return;
            }


            int investmentId =
                    Integer.parseInt(
                            idText
                    );

            double quantity =
                    Double.parseDouble(
                            quantityText
                    );

            double purchasePrice =
                    Double.parseDouble(
                            purchasePriceText
                    );

            double currentPrice =
                    Double.parseDouble(
                            currentPriceText
                    );


            String sql =
                    "UPDATE investments SET " +
                            "asset_name = ?, " +
                            "asset_type = ?, " +
                            "quantity = ?, " +
                            "purchase_price = ?, " +
                            "current_price = ?, " +
                            "purchase_date = ? " +
                            "WHERE investment_id = ? " +
                            "AND user_id = ?";


            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        assetName
                );

                statement.setString(
                        2,
                        assetType
                );

                statement.setDouble(
                        3,
                        quantity
                );

                statement.setDouble(
                        4,
                        purchasePrice
                );

                statement.setDouble(
                        5,
                        currentPrice
                );

                statement.setDate(
                        6,
                        Date.valueOf(
                                purchaseDate
                        )
                );

                statement.setInt(
                        7,
                        investmentId
                );

                statement.setInt(
                        8,
                        loggedInUserId
                );


                int rows =
                        statement.executeUpdate();


                if (rows > 0) {

                    sendText(
                            exchange,
                            "Investment updated successfully!"
                    );

                } else {

                    sendText(
                            exchange,
                            "Investment not found."
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendText(
                    exchange,
                    "Failed to update investment: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // DELETE INVESTMENT
    // =====================================================

    static void deleteInvestment(
            HttpExchange exchange)
            throws IOException {

        if (loggedInUserId == -1) {

            sendText(
                    exchange,
                    "Please login first."
            );

            return;
        }


        try {

            String body =
                    readRequestBody(exchange);


            String idText =
                    extract(
                            body,
                            "id"
                    );


            if (idText.isEmpty()) {

                sendText(
                        exchange,
                        "Investment ID is required."
                );

                return;
            }


            int investmentId =
                    Integer.parseInt(
                            idText
                    );


            String sql =
                    "DELETE FROM investments " +
                            "WHERE investment_id = ? " +
                            "AND user_id = ?";


            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        investmentId
                );

                statement.setInt(
                        2,
                        loggedInUserId
                );


                int rows =
                        statement.executeUpdate();


                if (rows > 0) {

                    sendText(
                            exchange,
                            "Investment deleted successfully!"
                    );

                } else {

                    sendText(
                            exchange,
                            "Investment not found."
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendText(
                    exchange,
                    "Failed to delete investment: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // PORTFOLIO SUMMARY
    // =====================================================

    static void summary(
            HttpExchange exchange)
            throws IOException {

        if (loggedInUserId == -1) {

            sendJson(
                    exchange,
                    "{\"totalInvested\":0,"
                            + "\"currentValue\":0,"
                            + "\"profitLoss\":0}"
            );

            return;
        }


        String sql =
                "SELECT " +
                        "SUM(quantity * purchase_price) " +
                        "AS total_invested, " +

                        "SUM(quantity * current_price) " +
                        "AS current_value " +

                        "FROM investments " +
                        "WHERE user_id = ?";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    loggedInUserId
            );

            ResultSet result =
                    statement.executeQuery();


            double totalInvested = 0;
            double currentValue = 0;


            if (result.next()) {

                totalInvested =
                        result.getDouble(
                                "total_invested"
                        );

                currentValue =
                        result.getDouble(
                                "current_value"
                        );
            }


            double profitLoss =
                    currentValue -
                            totalInvested;


            String json =
                    "{"
                            + "\"totalInvested\":"
                            + String.format(
                            "%.2f",
                            totalInvested
                    )
                            + ","

                            + "\"currentValue\":"
                            + String.format(
                            "%.2f",
                            currentValue
                    )
                            + ","

                            + "\"profitLoss\":"
                            + String.format(
                            "%.2f",
                            profitLoss
                    )
                            + "}";


            sendJson(
                    exchange,
                    json
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    "{\"totalInvested\":0,"
                            + "\"currentValue\":0,"
                            + "\"profitLoss\":0}"
            );
        }
    }


    // =====================================================
    // READ REQUEST BODY
    // =====================================================

    static String readRequestBody(
            HttpExchange exchange)
            throws IOException {

        InputStream input =
                exchange.getRequestBody();

        return new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }


    // =====================================================
    // EXTRACT JSON VALUE
    // =====================================================

    static String extract(
            String json,
            String key) {

        String search =
                "\"" + key + "\":";

        int start =
                json.indexOf(search);


        if (start == -1) {

            return "";
        }


        start += search.length();


        while (
                start < json.length()
                        && (
                        json.charAt(start) == ' '
                                || json.charAt(start) == '"'
                )
        ) {

            start++;
        }


        int end;


        if (
                start > 0
                        && json.charAt(
                        start - 1
                ) == '"'
        ) {

            end =
                    json.indexOf(
                            '"',
                            start
                    );

        } else {

            end =
                    json.indexOf(
                            ',',
                            start
                    );


            if (end == -1) {

                end =
                        json.indexOf(
                                '}',
                                start
                        );
            }
        }


        if (end == -1) {

            return "";
        }


        return json.substring(
                start,
                end
        ).trim();
    }


    // =====================================================
    // ESCAPE JSON
    // =====================================================

    static String escapeJson(
            String text) {

        if (text == null) {

            return "";
        }

        return text
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }


    // =====================================================
    // SEND TEXT
    // =====================================================

    static void sendText(
            HttpExchange exchange,
            String message)
            throws IOException {

        byte[] response =
                message.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/plain; charset=UTF-8"
                );


        exchange.sendResponseHeaders(
                200,
                response.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(response);

        output.close();
    }


    // =====================================================
    // SEND JSON
    // =====================================================

    static void sendJson(
            HttpExchange exchange,
            String json)
            throws IOException {

        byte[] response =
                json.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );


        exchange.sendResponseHeaders(
                200,
                response.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(response);

        output.close();
    }
}