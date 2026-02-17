AI-Powered Order Management System (Java + SQLite)
Project Overview

This project demonstrates an AI-assisted workflow for automating order management in Java. It implements a complete order lifecycle with database persistence, inventory management, and interactive CLI features, while leveraging AI-generated code and unit tests.

Key Goals:

Automate repetitive and error-prone order logic

Validate status transitions: Created → Confirmed → Delivered → Cancelled

Manage inventory automatically during order confirmation and cancellation

Use AI tools (Chat AI + IDE AI) to generate code, database schema, and unit tests

Provide a CLI interface for creating and managing multiple orders

Persist all data using SQLite

Project Features

Order Lifecycle

Create, confirm, deliver, and cancel orders

Status validation ensures no invalid transitions

Inventory Management

Check stock before confirmation

Deduct stock on confirmation

Restore stock on cancellation

Database Integration

Stores orders, order items, and inventory

SQLite used for lightweight persistence

CLI Interaction

Enter multiple orders interactively

Display summary report of all orders and remaining inventory

Unit Testing

JUnit 5 tests cover all valid/invalid transitions and inventory rules

Supports in-memory SQLite for testing

Project Structure
project/
│
├─ src/main/java/
│   ├─ Order.java        # Main Order class with DB integration
│   └─ Main.java         # CLI interface for multiple orders
│
├─ src/test/java/
│   └─ OrderTest.java    # Unit tests using JUnit 5
│
├─ orders.db             # SQLite database (auto-generated)
├─ schema.sql            # Optional DB schema file
├─ README.md             # Project documentation
└─ resources/
    └─ order_schema.json # Optional JSON schema used by AI prompts

Getting Started
Prerequisites

Java 11+

Maven (for dependency management)

SQLite JDBC Driver

Maven Dependency:

<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.41.2.1</version>
</dependency>

Running the Project

Compile the project:

mvn compile


Run the CLI program:

mvn exec:java -Dexec.mainClass="Main"


Follow prompts to:

Enter customer name

Enter products (comma-separated)

Enter quantities (comma-separated)

View summary report:

--- Order Summary ---
Order 1: Alice, Status: Confirmed, Products: [Book], Quantities: [2]
Order 2: Bob, Status: Cancelled, Products: [Pen], Quantities: [5]

--- Remaining Inventory ---
Book: 8
Pen: 45
Notebook: 20

Running Unit Tests
mvn test


Tests validate all status transitions, inventory rules, and cancellation logic.

AI Workflow

Chat AI: Generated JSON schema and test scenarios for order logic.

IDE AI: Converted JSON schema into Java Order class, DB operations, and unit tests.

CLI Program: Created interactive interface for creating multiple orders and viewing reports.

Lessons Learned

AI-assisted development can dramatically speed up repetitive coding tasks.

Database integration ensures data persistence across sessions.

Status transition validation prevents logical errors in order processing.

Unit tests help verify business rules and inventory correctness.

Future Improvements

Integrate a web-based frontend for real-time order management

Add user authentication and roles

Expand inventory management with automatic stock alerts

Include reporting & analytics dashboards
