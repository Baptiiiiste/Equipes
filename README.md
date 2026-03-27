# Light Teams

Light version of Microsoft Teams for a Network course project.

## Prerequisites

- JDK 11\+ installed (check with `java -version`)
- Maven 3.6\+ installed (check with `mvn -v`)
- macOS (instructions below)
- Docker Desktop (for local PostgreSQL)
- (Optional) IntelliJ IDEA for development and debugging

## Clone the repository

Use your fork or the original repo:

    git clone git@github.com:Baptiiiiste/Equipes.git
    cd Equipes

## Build and package

Build and create an executable JAR (skip tests if desired):

    mvn clean package -DskipTests

The artifact is typically in `target/` as `artifactId-version.jar`. Replace with the actual name from your `pom.xml`.

## Start PostgreSQL with Docker

1. Copy the env template:

       cp .env.example .env

2. Start PostgreSQL:

       docker compose up -d postgres

The server reads these variables from your shell when it starts:

- `APP_DB_URL` (default: `jdbc:postgresql://localhost:5432/equipes`)
- `APP_DB_USER` (default: `equipes`)
- `APP_DB_PASSWORD` (default: `equipes`)
- `APP_SERVER_PORT` (default: `8080`)
- `APP_AUDIO_UDP_PORT` (default: `APP_SERVER_PORT + 1`, so `8081`)

If these variables are not exported, defaults are used.

## Run the application

Launch from Maven:

    mvn exec:java

Then choose:

- `1` to start server (Flyway migration runs automatically and rooms are loaded from PostgreSQL)
- `2` to start client

## Live audio over UDP

- Audio conversations run inside meetings.
- Signaling (join/leave/start/stop audio) uses the existing TCP packet layer.
- Audio frames are sent in real time over UDP and relayed by the server to meeting participants in the same room.
- To change the relay port, export `APP_AUDIO_UDP_PORT` before starting the server.

## Run from IntelliJ IDEA

1. Open IntelliJ IDEA → `File` → `Open` → select the project `pom.xml`.
2. Wait for Maven import to finish.
3. Create a Run configuration of type `Application`:
    - `Main class`: the class with `public static void main(String[] args)`
    - `Use classpath of module`: select the main module
4. Run or debug the configuration.

## Tests

Run unit tests:

    mvn test

## Project layout

- `src/main/java` : application source code
- `src/test/java` : tests
- `pom.xml` : Maven configuration

## Troubleshooting

- Java version error: install the correct JDK and set `JAVA_HOME`.
- Main class not found: ensure `pom.xml` config for the jar plugin or run with `mvn exec:java -Dexec.mainClass="your.MainClass"`.
- PostgreSQL connection error: confirm `docker compose ps` shows `equipes-postgres` as healthy and verify `.env` values.

Save this content to `README.md`.
