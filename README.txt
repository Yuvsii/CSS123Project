Java Inventory & Transaction Management System
Course Project: Data Structures and Algorithms
Mapúa University - Makati

Program Explanation
This system is a robust, role-based management application designed to handle real-time inventory tracking and user transactions.

Role-Based Access Control (RBAC): The system dynamically identifies users as either an Admin or a Customer using Polymorphism.

Admin Features: Full CRUD (Create, Read, Update, Delete) capabilities to manage products and monitor database records.

Customer Features: An interface where users can browse catalog items, top up their digital wallet (budget), and execute secure purchases.

Transactional Integrity: Implements Custom Exceptions (such as InvalidQuantityException) to prevent illegal operations like overselling stock or spending beyond a wallet limit.

System Architecture
The project follows a Three-Tier Architecture to ensure a clean separation of concerns:

Presentation Layer: A Java Swing GUI (MainFrame.java) for an event-driven user interface.

Logic Layer (DAO): Inventory.java acts as the Data Access Object, enforcing business rules and logic.

Data Layer: A persistent MySQL Database connected via JDBC.

Security: Utilizes PreparedStatements with ? placeholders to sanitize all inputs and neutralize SQL Injection vulnerabilities.

Repository Structure
/src: Contains all Java source code files (.java).

/database: Contains the .sql file required to rebuild the database schema and sample data.

/screenshots: Visual evidence of the application UI and MySQL database schema.

Setup Instructions
Database Setup:

Start Apache and MySQL via the XAMPP Control Panel.

Navigate to phpMyAdmin and create a database named cardinal_inventory_db.

Import the SQL file located in the /database folder.

Java Execution:

Import the project into your IDE (Eclipse, VS Code, etc.).

Add the mysql-connector-java.jar to your project's Build Path.

Run MainFrame.java to launch the application.

Technologies Used
Language: Java SE

GUI Library: Java Swing / AWT

Database: MySQL (via XAMPP)

API: JDBC (Java Database Connectivity)