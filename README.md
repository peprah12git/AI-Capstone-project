# Order Management System

A Spring Boot application for managing orders with inventory tracking, status transitions, and SQLite database persistence.

## Features

- **Order Management**: Create, confirm, deliver, and cancel orders
- **Inventory Management**: Track product stock and reserve/return inventory
- **Status Transitions**: Enforce valid order state transitions (Created → Confirmed → Delivered/Cancelled)
- **Database Persistence**: SQLite database for orders and inventory
- **Comprehensive Testing**: JUnit 5 tests with in-memory database

## Project Structure

```
src/
├── main/java/com/AI/
│   ├── AiLabApplication.java              # Spring Boot entry point
│   ├── Order.java                         # Original Order class
│   ├── OrderWithInventory.java            # Order with inventory management
│   ├── OrderStatus.java                   # Order status enum
│   ├── DatabaseInitializer.java           # Database setup and initialization
│   ├── DatabaseInventoryService.java      # SQLite-backed inventory service
│   ├── SimpleInventoryService.java        # In-memory inventory service
│   ├── InventoryService.java              # Inventory service interface
│   ├── OrderRepository.java               # Order persistence layer
│   ├── OrderStatusValidator.java          # Status transition validation
│   ├── InsufficientInventoryException.java
│   ├── InvalidStatusTransitionException.java
│   └── OrderManagementDemo.java           # Demo application
└── test/java/com/AI/
    ├── OrderDatabaseTest.java             # Integration tests with SQLite
    ├── OrderWithInventoryTest.java        # Unit tests with mocks
    ├── OrderStatusValidatorTest.java      # Validator tests
    └── DatabaseIntegrationTest.java       # Database integration tests
```

## Order Status Transitions

Valid transitions:
- **CREATED** → CONFIRMED or CANCELLED
- **CONFIRMED** → DELIVERED or CANCELLED
- **DELIVERED** → (terminal state)
- **CANCELLED** → (terminal state)

Rules:
- Cannot deliver before confirming
- Cannot cancel after delivery
- Inventory must be sufficient to confirm

## Database Schema

### inventory
```sql
CREATE TABLE inventory (
  product TEXT PRIMARY KEY,
  stock INTEGER NOT NULL
)
```

### orders
```sql
CREATE TABLE orders (
  order_id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_name TEXT NOT NULL,
  status TEXT NOT NULL
)
```

### order_items
```sql
CREATE TABLE order_items (
  order_id INTEGER NOT NULL,
  product TEXT NOT NULL,
  quantity INTEGER NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  FOREIGN KEY (product) REFERENCES inventory(product)
)
```

## Initial Inventory

- Book: 10 units
- Pen: 50 units
- Notebook: 20 units

## Usage

### Running the Application

```bash
mvn spring-boot:run
```

### Running Tests

```bash
mvn test
```

### Running Demo

```bash
java -cp target/demo-0.0.1-SNAPSHOT.jar com.AI.OrderManagementDemo
```

## Key Classes

### OrderWithInventory
Main order class with inventory management:
- `confirmOrder()` - Reserves inventory and confirms order
- `deliverOrder()` - Marks order as delivered
- `cancelOrder()` - Cancels order and returns inventory if confirmed

### DatabaseInventoryService
SQLite-backed inventory operations:
- `checkStock()` - Validates sufficient inventory
- `reserveStock()` - Deducts from inventory
- `returnStock()` - Returns to inventory

### OrderRepository
Persists orders to database:
- `saveOrder()` - Saves order and items
- `updateOrderStatus()` - Updates order status
- `getOrderProducts()` - Retrieves order products

### OrderStatusValidator
Validates status transitions:
- `validateTransition()` - Throws exception for invalid transitions
- `isTransitionAllowed()` - Returns boolean
- `getAllowedNextStates()` - Returns valid next states

## Exception Handling

- `InsufficientInventoryException` - Thrown when stock is insufficient
- `InvalidStatusTransitionException` - Thrown for invalid state transitions
- `IllegalArgumentException` - Thrown for invalid input

## Testing

12 comprehensive JUnit 5 tests covering:
- Valid order lifecycle transitions
- Invalid transition prevention
- Inventory validation
- Stock restoration on cancellation
- Multi-product orders
- Order persistence
- Concurrent order processing
- Stock boundary conditions

Tests use in-memory SQLite database for isolation and speed.

## Dependencies

- Spring Boot 4.0.2
- JUnit 5
- SQLite JDBC
- Mockito (for unit tests)

## Configuration

Database auto-configuration is excluded in `application.properties`:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

This allows custom JDBC management with SQLite.

