# Cinema Tickets Service 🎬

## Overview

Application ID number: 16746194
Reference No: 445853
This project is a solution to the Department for Work and Pensions (DWP) coding exercise for the **Java Software Engineer** role.
It implements a ticket purchasing service that:

* Validates ticket purchase requests
* Enforces business rules
* Calculates total payment
* Reserves seats using external services

The solution focuses on **clean code, testability, and clear separation of concerns**.

---

## Features

* Supports three ticket types:

  * **ADULT (£25)**
  * **CHILD (£15)**
  * **INFANT (£0)**

* Implements all required business rules:

  * Maximum of **25 tickets per purchase**
  * **At least one adult required** when purchasing child/infant tickets
  * **Infants do not get seats** and must sit on an adult’s lap
  * **Number of infants cannot exceed number of adults**

* Integrates with:

  * `TicketPaymentService` (for payments)
  * `SeatReservationService` (for seat allocation)

---

## Design Decisions

### 1. Layered Structure

* **Service Layer** → `TicketServiceImpl`
* **Domain Layer** → `TicketTypeRequest`
* **Exception Handling** → `InvalidPurchaseException`

This keeps responsibilities clearly separated and the code maintainable.

---

### 2. Immutability

`TicketTypeRequest` is implemented as an **immutable object**, ensuring:

* Thread safety
* Predictable behavior
* No unintended side effects

---

### 3. Validation Strategy

Validation is split into:

* Input validation (nulls, invalid values)
* Business rule validation (ticket limits, adult requirements)

This improves readability and makes logic easier to test.

---

### 4. Value Object for Aggregation

A private `PassengerCount` record is used to:

* Aggregate ticket counts
* Encapsulate related logic (total tickets, seats)

This avoids passing multiple primitive values and improves clarity.

---

### 5. Exception Design

A custom exception (`InvalidPurchaseException`) with a `Reason` enum:

* Provides clear failure reasons
* Makes testing and debugging easier
* Avoids generic error messages

---

## Assumptions

* All account IDs greater than zero are valid
* Payment and seat reservation services are reliable and always succeed
* No concurrency concerns are required for this exercise

---

## How to Build and Run

### Prerequisites

* Java 21
* Maven 3.9+

### Build

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

---

## Testing

The solution includes unit tests using:

* **JUnit 5**
* **Mockito**

Tests cover:

* Valid purchase scenarios
* Invalid inputs
* Business rule violations
* Interaction with external services

---

## Project Structure

```
src/
 ├── main/
 │   ├── java/
 │   │   ├── thirdparty/              # External services (provided)
 │   │   └── uk.gov.dwp.uc.pairtest/
 │   │       ├── domain/              # Domain objects
 │   │       ├── exception/           # Custom exceptions
 │   │       └── TicketServiceImpl    # Core business logic
 │
 └── test/
     └── java/
         └── uk.gov.dwp.uc.pairtest/
             └── TicketServiceImplTest
```

---

## Key Considerations

* Code prioritizes **readability over cleverness**
* Business rules are **explicit and easy to follow**
* Dependencies are injected to support **unit testing**
* Solution is designed to be **easy to extend**

---

## Author

Imasha
