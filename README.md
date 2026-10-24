# Task Manager
A full stack application for managing your daily tasks and/or projects

## Tech stack
**Backend:** Java 27, Spring Boot 3, Spring Data JPA, PostgreSQL, Flyway, BCrypt, JUnit and Mockito

**Frontend:** Node.js 24, React, Vite

**DevOps:** Docker, GitHub Actions (CI/CD pipeline)

## Features
- Full CRUD operations with filtering/pagination
- Admin panel for admins to manage users and their tasks
- Authentication is session based for now
- Database migration is managed by Flyway

# HOW TO RUN

## Using Docker Desktop

### Set up the environment variables in the `.env` file *(or leave default ones)*

```dotenv
SPRING_DATASOURCE_URL=your_db_url
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=user_password

APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=1234
```

`APP_ADMIN_USERNAME` and `APP_ADMIN_PASSWORD` are used to create the first admin user in a fresh database

### Build and run container

```shell
docker compose up --build
```

### Access the application

- Frontend: http://localhost
- Backend API: http://localhost:8080/api
- Database: `localhost:5432`

## Building the application manually

### Make sure you have JDK 27, Node 24, PostgreSQL and Maven installed

### Setup database
```sql
CREATE USER task_app WITH PASSWORD '1234';
CREATE DATABASE task_db OWNER task_app;
```

### Setup backend

```shell
cd TaskManagerBackend
./mvnw spring-boot:run
```

Make sure to provide local environment variables `DB_URL`, `DB_USER` and `DB_PASSWORD`. Or you can set them in
`application.properties` manually. 
```dotenv
spring.datasource.url=jdbc:postgresql://localhost:5432/task_db
spring.datasource.username=task_app
spring.datasource.password=1234
```

The API is served on the port `8080` by default

### Setup frontend
```shell
cd TaskManagerFrontend
npm install
npm run dev
```

The app will be served at a default Vite port: http://localhost:5173

# RUNNING TESTS
You can run JUnit tests, which use Mockito, to verify that services work correctly
```shell
cd TaskManagerBackend
./mvnw clean test
```
