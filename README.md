# java-app

Basic Java application skeleton using Gradle.

## Run

```bash
./gradlew -q -x test build
DB_URL=jdbc:postgresql://localhost:5432/postgres \
DB_USER=postgres \
DB_PASSWORD=mysecretpassword \
java -cp build/libs/java-app-0.1.0-SNAPSHOT.jar com.example.app.App
```

Notes:
- `users.id` and `transactions.id` use `BIGSERIAL`, which auto-increments in PostgreSQL.

## Test

```bash
./gradlew test
```
