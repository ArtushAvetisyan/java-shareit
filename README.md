# ShareIt — Collaborative Item Sharing & Booking Platform

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![Spring Data JPA](https://img.shields.io/badge/Spring-Data%20JPA-green?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-blue?style=flat-square&logo=postgresql)
![Microservice Architecture](https://img.shields.io/badge/Architecture-Microservices%20%2F%20Gateway-purple?style=flat-square)
![Maven Multi-Module](https://img.shields.io/badge/Maven-Multi--Module-red?style=flat-square)

**ShareIt** — is a microservice platform for item sharing (sharing economy), allowing users to publish listings for available items, find needed items, book them for specific dates, leave reviews, and create requests to find rare items.

---

## 📋 Table of Contents
- [About the Service](#-about-the-service)
- [Architecture](#-architecture)
- [Service and Module Composition](#-service-and-module-composition)
- [Technology Stack](#-technology-stack)
- [Database Structure](#-database-structure)
- [Business Logic and REST API](#-business-logic-and-rest-api)
- [Testing](#-testing)
- [Launch Instructions](#-launch-instructions)

---

## 📖 About the Service

The **ShareIt** service solves the problem of a temporary need for items and tools without the need to purchase them:
1. **Item Sharing and Management**: Users can publish items, provide their descriptions, and set their availability status for rental.
2. **Booking with Confirmation**: Renting items for a selected period with automatic date blocking and confirmation from the owner.
3. **Feedback and Comments**: The ability to leave reviews and ratings after the rental period has ended.
4. **Item Requests**: Creating requests to find items that are not yet in the system, and the ability for other users to respond to these requests.

---

## 🏗 Architecture

The system is designed using a two-level microservice architecture with a multi-module Maven project (**Multi-Module Project**):

1. **`shareIt-gateway` (port `8080`)**:
    - A lightweight entry gateway for initial validation of all incoming user data (without accessing the database).
    - Protects the main server from invalid requests, duplicates, and unnecessary load.
    - Proxies requests to the main server via HTTP/REST using `BaseClient` and `RestTemplate`.
2. **`shareIt-server` (port `9090`)**:
    - The server for core business logic and data processing.
    - Includes JPA repositories, services for processing bookings, searches, and creating reviews.
3. **Code Organization (Feature Layout)**:
    - Inside the modules, the code is structured by business features: `item`, `booking`, `request`, and `user` packages.
4. **DTO and Mapper Pattern**:
    - Complete separation of internal JPA entities and external DTO objects transmitted via the REST API.

---

## 🧩 Service and Module Composition

### 1. Multi-Module Project Modules
| Module | Port | Purpose and Functionality |
| :--- | :--- | :--- |
| **`shareIt-gateway`** | `8080` | Input data validation, filtering of invalid requests, proxying HTTP requests to `shareIt-server`. |
| **`shareIt-server`** | `9090` | Data storage, processing of JPA entities, execution of business rules for bookings, requests, and reviews. |

### 2. Business Packages (Feature Packages)
| Package | Description | Main Functionality |
| :--- | :--- | :--- |
| **`user`** | Users | Registration, updating, and management of user profiles. |
| **`item`** | Items and Reviews | Item management, keyword search, viewing item bookings, adding comments. |
| **`booking`** | Booking | Creating bookings, changing statuses (`APPROVED`, `REJECTED`), filtering by state (`ALL`, `CURRENT`, `PAST`, `FUTURE`, `WAITING`, `REJECTED`). |
| **`request`** | Item Requests | Creating requests for missing items, viewing other users' requests, and adding items in response to them. |

---

## 🛠 Technology Stack

- **Programming Language**: Java 21
- **Framework**: Spring Boot 3.x (Spring Web, Spring Data JPA, Bean Validation)
- **Project Build**: Maven Multi-Module (`pom.xml`)
- **Database**: PostgreSQL, Hibernate / JPA
- **Inter-service Communication**: REST API (`RestTemplate` / `BaseClient`)
- **Testing**: JUnit 5, Mockito, MockMVC, `@JsonTest`, `@SpringBootTest`, Postman

---

## 🗄 Database Structure

The database schema is automatically deployed from the `schema.sql` file in `shareIt-server`:

* **`users`**: System users (`id`, `name`, `email` with a `UNIQUE` constraint).
* **`items`**: Item catalog (`id`, `name`, `description`, `is_available`, `owner_id`, `request_id`).
* **`bookings`**: Booking requests (`id`, `start_date`, `end_date`, `item_id`, `booker_id`, `status`).
* **`comments`**: Renters' reviews of items (`id`, `text`, `item_id`, `author_id`, `created`).
* **`requests`**: Requests to add rare items (`id`, `description`, `requestor_id`, `created`).

---

## 💡 Business Logic and REST API

For all requests related to operations of a specific user, the mandatory HTTP header **`X-Sharer-User-Id`** is passed.

### 1. Users (`/users`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/users` | Creating a new user. |
| `PATCH` | `/users/{userId}` | Updating user data. |
| `GET` | `/users/{userId}` | Retrieving a user profile by ID. |
| `GET` | `/users` | Retrieving the list of all users. |
| `DELETE` | `/users/{userId}` | Deleting a user. |

### 2. Items and Comments (`/items`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/items` | Adding a new item (with an optional `requestId`). |
| `PATCH` | `/items/{itemId}` | Editing an item (by the owner only). |
| `GET` | `/items/{itemId}` | Viewing information about an item, its reviews, and booking dates. |
| `GET` | `/items` | Viewing all of the owner's items. |
| `GET` | `/items/search?text={text}` | Searching for items available for rental by text in the name/description. |
| `POST` | `/items/{itemId}/comment` | Adding a review by a user who rented the item in the past. |

### 3. Booking (`/bookings`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/bookings` | Creating a booking request (status `WAITING`). |
| `PATCH` | `/bookings/{bookingId}?approved={true/false}` | Confirming or rejecting a booking by the item owner. |
| `GET` | `/bookings/{bookingId}` | Retrieving booking information (by the author or owner). |
| `GET` | `/bookings?state={state}` | Retrieving the list of all bookings of the current renter. |
| `GET` | `/bookings/owner?state={state}` | Retrieving the list of bookings for the owner's items. |

*Possible `state` values*: `ALL` (default), `CURRENT`, `PAST`, `FUTURE`, `WAITING`, `REJECTED`.

### 4. Item Requests (`/requests`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/requests` | Creating a new request for a missing item. |
| `GET` | `/requests` | Retrieving the list of one's own requests together with responses to them. |
| `GET` | `/requests/all` | Paginated viewing of requests created by other users. |
| `GET` | `/requests/{requestId}` | Retrieving data about an individual request and its responses. |

---

## 🧪 Testing

The project is fully covered by a comprehensive set of multi-level tests:

1. **Integration Tests**: Verify end-to-end interaction of the service layer with the PostgreSQL database for key business methods (for example, retrieving all items owned by a user).
2. **Controller WEB Tests (MockMVC)**: Cover REST endpoints of the presentation layer with services isolated via `@MockBean`.
3. **JSON Serialization Tests (`@JsonTest`)**: Verify correct formatting and serialization/deserialization of complex DTO objects (for example, timestamps and nested entities).
4. **Postman E2E Tests**: Automated verification of the entire system's functionality through an API collection.
