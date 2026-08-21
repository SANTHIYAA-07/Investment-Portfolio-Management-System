# Investment Portfolio Management System

A Java-based application for managing and tracking investment portfolios using Java, MySQL, JDBC, HTML, CSS, and JavaScript.

## Features

- User registration and login
- Password hashing for user authentication
- Add investments
- View investments
- Update investments
- Delete investments
- Portfolio summary
- Total invested amount calculation
- Current portfolio value calculation
- Profit/Loss calculation
- MySQL database integration
- Web-based interface

## Technologies Used

- Java
- MySQL
- JDBC
- HTML
- CSS
- JavaScript
- Maven
- IntelliJ IDEA
- MySQL Workbench

## Database

The application uses MySQL to store user and investment information.

### Users Table

Stores:

- User ID
- Name
- Email
- Password

### Investments Table

Stores:

- Investment ID
- User ID
- Asset Name
- Asset Type
- Quantity
- Purchase Price
- Current Price
- Purchase Date

The `investments` table is related to the `users` table using `user_id`.

## Main Functionalities

### 1. User Authentication

Users can register and log in to the system. Passwords are hashed before being stored in the database.

### 2. Investment Management

Logged-in users can:

- Add a new investment
- View their investments
- Update investment details
- Delete investments

### 3. Portfolio Summary

The system calculates:

- Total Invested Amount
- Current Portfolio Value
- Profit/Loss

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── org/example/
    │       ├── DBConnection.java
    │       ├── Investment.java
    │       ├── Main.java
    │       ├── PasswordUtil.java
    │       ├── PortfolioApp.java
    │       ├── PortfolioWebServer.java
    │       ├── TestConnection.java
    │       └── User.java
    │
    └── resources/
        └── index.html

pom.xml
