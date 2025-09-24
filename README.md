# java_spring_auth_service

This project is a robust authentication and account management solution built with **Java** and **Spring**, utilizing a **microservices** architecture. It integrates features such as **JWT authentication**, **Refresh Token generation**, **account management**, **email sending**, and **secret management** with **Hashicorp Vault**, along with advanced security and scalability features.

## 🛠️ Technologies Used

- **Java**: The main programming language for the project.
- **Spring**: Framework for building scalable and robust applications with a focus on microservices.
- **Hibernate**: ORM framework for efficient database interaction.
- **Flyway**: Tool for database versioning and migration.
- **Lombok**: Library that reduces boilerplate code in Java (like getters, setters, toString, etc.).
- **API Gateway (NGINX)**: Manages traffic and routing of microservices.
- **Microservices**: Decoupled architecture for better scalability and maintainability.
- **PostgreSQL**: Relational database for data persistence.
- **Redis**: Caching system to optimize performance.
- **Kafka**: Messaging system for asynchronous communication between microservices.
- **Logs**: Logging system for monitoring and tracing the application.
- **Error Handler**: Robust error management for stability and reliability.
- **Documentation**: The service is documented via **Swagger** and **Redocly**, allowing API interactivity.
- **Input Validation**: Server-side validation to ensure data integrity.
- **Docker**: Containerization of the application for easy deployment and consistent environments.
- **Docker Compose**: Orchestration of multiple containers, simplifying service execution.
- **Internationalization**: Server-side management for translating content into different languages.
- **Rate Limiter (DDOS Protection)**: Protection against Distributed Denial of Service (DDOS) attacks through request limiting.
- **Account Management Service**: A microservice for handling user accounts.
- **Email Service**: A dedicated microservice for sending emails (e.g., account confirmation, password reset).
- **Authentication with JWT and Refresh Token**: Secure authentication with **JWT** and session renewal via **Refresh Token**.
- **Advanced Encryption**: Protection of sensitive data using modern encryption techniques.
- **Hashicorp Vault**: Secure management of secrets like API keys and passwords.
- **Performance**: Optimizations for ensuring high application performance.

## 🚀 Features

### 1. **Authentication and Authorization**
- Secure authentication with **JWT (JSON Web Token)**.
- **Refresh Token** for secure session renewal.
- **Role-based access control** (RBAC) for fine-grained authorization.

### 2. **Account Management**
- **Create, update, and delete user accounts**.
- **Password recovery** and **reset via email**.
- **Secure password storage** with strong encryption.

### 3. **Email Service**
- A dedicated **email service** for sending various emails like account confirmation and password recovery.

### 4. **Scalability and Resilience**
- **Microservices architecture** for enhanced scalability and maintainability.
- **Kafka** for asynchronous communication between microservices.
- **Redis** for caching and improving response times.

### 5. **Security**
- Protection against **DDOS** with a **Rate Limiter**.
- **Advanced encryption** techniques for protecting sensitive data.
- **Hashicorp Vault** for secure secret management.
- **JWT** and **Refresh Tokens** for secure user sessions.

### 6. **API Documentation**
- The API is fully documented using **Swagger** and **Redocly**, making it easy to test and understand the endpoints.

### 7. **Error Management**
- Robust **error handling** system with proper exception capture and responses for client requests.

## 🔧 How to Run the Project

### Prerequisites

1. **Java 17** or higher.
2. **Docker** and **Docker Compose**.
3. **PostgreSQL** (or use the Docker Compose configuration).
4. **Redis** (or use the Docker Compose configuration).
5. **Kafka** (optional, if you're using messaging between microservices).

### Step 1: Clone the Repository
### Step 2: Set up Environment Variables
### Step 3: Start Docker Containers

## 📚 API Documentation
The API is fully documented using **Swagger** and **Redocly**, providing easy access to all available endpoints and their usage.

## 🔒 Security
This project includes multiple layers of security, such as **JWT authentication**, **Rate Limiter** for protection against attacks, and **Hashicorp Vault** for secure management of secrets. The application follows best practices for securing sensitive data and ensuring integrity.

## 🐳 Containerization
With **Docker** and **Docker Compose**, all services can be run in isolated containers, ensuring consistency across different environments and simplifying deployment.

## ⚡ Performance
The project is optimized for performance, utilizing **Redis** for caching, **Rate Limiter** for traffic control, and a **microservices architecture** for horizontal scalability.
