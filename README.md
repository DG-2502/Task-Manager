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
```postgresql
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
spring.datasource.url=postgresql://localhost:5432/task_db
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