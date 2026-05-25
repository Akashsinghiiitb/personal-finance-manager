# Personal Finance Manager

An enterprise-ready, production-grade personal finance management system. Built using **Spring Boot 3.x** and **React (Vite)** with **Tailwind CSS**. The application maintains completely isolated user data, features session-based authentication using secure cookies, offers complex categorization, handles savings goals progress automatically, and generates monthly/yearly cash flow reports.

## Features

- **Isolated User Sessions**: Session-based cookie authentication using secure HTTP-only cookies (`JSESSIONID`).
- **Income & Expense Tracking**: Full CRUD management of transactions with future-date validation.
- **Custom & Global Categories**: System-seeded default categories alongside unique custom user categories.
- **Automated Savings Goals**: Dynamic progress tracking based on user cash flow since goal start.
- **Monthly/Yearly Analytics**: Dynamic breakdown graphs (cash flow trend, category allocations, and net calculations).
- **Modern Premium Dashboard**: Responsive grid layouts, toast feedback notifications, and sidebar navigations.

---

## Tech Stack

### Backend
- **Java 17** & **Spring Boot 3.x**
- **Spring Security** (Session-based, cookie tracking)
- **Spring Data JPA** & **Hibernate**
- **Maven**
- **H2 Database** (Development) & **PostgreSQL** (Production)
- **JUnit 5** & **Mockito** (At least 80% test coverage)

### Frontend
- **React.js (Vite)**
- **Tailwind CSS v3**
- **Axios** (with response interceptors for 401 handling)
- **React Router**
- **Recharts** (Visual graphs)
- **Lucide Icons**

---

## Folder Structure

```text
personal-finance-manager/
├── pom.xml                      # Maven project configuration
├── README.md                    # Setup and API documentation
├── src/
│   ├── main/
│   │   ├── java/com/personalfinance/manager/
│   │   │   ├── config/          # Spring boot configs (Security, CORS, Jackson, Seed)
│   │   │   ├── controller/      # Auth, Categories, Transactions, Goals, Reports Controllers
│   │   │   ├── dto/             # Layer-specific DTOs
│   │   │   ├── entity/          # JPA Entities (User, Category, Transaction, SavingsGoal)
│   │   │   ├── exception/       # Global @ControllerAdvice and exception definitions
│   │   │   ├── mapper/          # Custom Entity-to-DTO conversion mappers
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── security/        # AuthEntryPoint, AccessDeniedHandler, SecurityUtils
│   │   │   └── service/         # Services containing core business logic
│   │   └── resources/
│   │       ├── application.yml  # Shared settings
│   │       ├── application-dev.yml  # Dev profile (H2, frameOptions disabled)
│   │       └── application-prod.yml # Prod profile (PostgreSQL, HTTPS cookies)
│   └── test/                    # JUnit Mockito Service Layer unit tests
└── frontend/                    # Vite + React + Tailwind frontend application
```

---

## API Documentation

### Public Auth Endpoints

#### Register User
- **POST** `/api/auth/register`
- **Request Body**:
  ```json
  {
    "username": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
  }
  ```
- **Response (201 Created)**:
  ```json
  {
    "message": "User registered successfully",
    "userId": 1
  }
  ```

#### Login User
- **POST** `/api/auth/login`
- **Request Body**:
  ```json
  {
    "username": "user@example.com",
    "password": "password123"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "message": "Login successful"
  }
  ```
  *(Sets HTTP-only `JSESSIONID` cookie)*

#### Logout User
- **POST** `/api/auth/logout`
- **Response (200 OK)**:
  ```json
  {
    "message": "Logout successful"
  }
  ```
  *(Invalidates backend session and clears cookie)*

#### Fetch Logged-in User Profile
- **GET** `/api/auth/me`
- **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "username": "user@example.com",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
  }
  ```

---

### Categories API (Authenticated)

#### Get Categories
- **GET** `/api/categories`
- **Response (200 OK)**: Lists default global categories and user custom categories.

#### Create Custom Category
- **POST** `/api/categories`
- **Request Body**:
  ```json
  {
    "name": "Shopping",
    "type": "EXPENSE"
  }
  ```
- **Response (201 Created)**: Category response DTO.

#### Delete Custom Category
- **DELETE** `/api/categories/{name}`
- **Response (200 OK)**: Success message. *(Fails with 400 Bad Request if category is referenced by transactions)*

---

### Transactions API (Authenticated)

#### Log Transaction
- **POST** `/api/transactions`
- **Request Body**:
  ```json
  {
    "amount": 250.50,
    "date": "2026-05-25",
    "category": "Rent",
    "description": "Monthly rent check"
  }
  ```
- **Response (201 Created)**: Created transaction details.

#### List Filtered Transactions
- **GET** `/api/transactions`
- **Query Parameters**:
  - `startDate` (e.g., `2026-05-01`)
  - `endDate` (e.g., `2026-05-31`)
  - `categoryId` (e.g., `3`)
- **Response (200 OK)**: Array of transaction responses sorted newest first.

#### Update Transaction
- **PUT** `/api/transactions/{id}`
- **Request Body**:
  ```json
  {
    "amount": 260.00,
    "category": "Rent",
    "description": "Adjusted rent payment",
    "date": "2026-05-25"
  }
  ```
  *(Note: Date cannot be updated; if sent, it must match the original transaction date)*

#### Delete Transaction
- **DELETE** `/api/transactions/{id}`

---

### Savings Goals API (Authenticated)

#### Create Goal
- **POST** `/api/goals`
- **Request Body**:
  ```json
  {
    "goalName": "Holiday Trip",
    "targetAmount": 5000.00,
    "startDate": "2026-05-01",
    "targetDate": "2026-12-31"
  }
  ```

#### List Goals
- **GET** `/api/goals`
- **Response (200 OK)**: Array of goal items including calculated progress metrics (`currentProgress`, `progressPercentage`, `remainingAmount`).

#### Delete Goal
- **DELETE** `/api/goals/{id}`

---

### Reports API (Authenticated)

#### Get Monthly Report
- **GET** `/api/reports/monthly/{year}/{month}`
- **Response (200 OK)**: Net savings and detailed category breakdowns.

#### Get Yearly Report
- **GET** `/api/reports/yearly/{year}`
- **Response (200 OK)**: Net savings, totals, and month-by-month cash flow trends.

---

## Setup & Running Locally

### Backend Setup
1. Configure Java 17 and Maven.
2. In the project root, launch the boot server:
   ```bash
   mvn spring-boot:run
   ```
3. The server starts on `http://localhost:8080`.
4. H2 Console: Access `http://localhost:8080/h2-console` using database URL `jdbc:h2:mem:financedb` with username `sa` and password `password`.

### Frontend Setup
1. Move to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Launch the development server:
   ```bash
   npm run dev
   ```
4. Open your browser to `http://localhost:5173`. All backend calls are proxied automatically.

---

## Running Tests

### Backend Unit Tests
Run backend tests to verify core logic:
```bash
mvn clean test
```
The test suite utilizes Mockito and JUnit 5, ensuring > 80% business layer coverage.

---

## Deployment Steps

### Backend (Render)
1. Register a PostgreSQL database instance on Render.
2. Set up a Web Service on Render pointing to your backend repo folder.
3. Add the following environment variables:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `DATABASE_URL`: `jdbc:postgresql://<host>:<port>/<dbname>`
   - `SPRING_DATASOURCE_USERNAME`: `<username>`
   - `SPRING_DATASOURCE_PASSWORD`: `<password>`
4. Render builds the Maven project and runs the JAR file.

### Frontend (Vercel / Netlify)
1. Push your code to GitHub.
2. Connect your repository to Vercel/Netlify.
3. Configure the **Build Command** as `npm run build` and **Publish Directory** as `dist` inside the `frontend` project.
4. Set up production domain routing / API forwards as needed.
