# Light Teams

Light version of Microsoft Teams for a Network course project. **macOS only.**

## Features

- **Team Management**: Create and manage team rooms
- **Real-time Chat**: Send and receive messages instantly within rooms
- **Live Audio Calls**: Crystal-clear audio conversations with multiple participants using UDP streaming
- **Screen Sharing**: Share your screen with team members in meetings (only one active share per room)
- **User Interface**: Intuitive Swing-based GUI for seamless collaboration
- **Persistent Storage**: PostgreSQL database for chat history and room management

## Prerequisites (macOS)

- **macOS**
- **JDK 21+** (check with `java -version`)
- **Maven 3.9+** (check with `mvn -v`)
- **Docker Desktop** (for PostgreSQL database)
- _(Optional) IntelliJ IDEA for development and debugging_

### Quick verification (copy/paste)

```bash
java -version
mvn -v
docker --version
docker compose version
```

All commands should return version info without errors.

### Installation (via Homebrew)

If you don't have Java or Maven:

```bash
# Install Java
brew install openjdk@21
# Link it to your PATH if needed
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Install Maven
brew install maven

# Verify installation
java -version
mvn -v
```

Install **Docker Desktop** from [docker.com](https://www.docker.com/products/docker-desktop).

## Clone the repository

Use your fork or the original repo:

    git clone git@github.com:Baptiiiiste/Equipes.git
    cd Equipes

## Build and package

Build and create an executable JAR (skip tests if desired):

    mvn clean package -DskipTests

The artifact is typically in `target/` as `artifactId-version.jar`. Replace with the actual name from your `pom.xml`.

## Run the application

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

Verify the database is running:

```bash
docker compose ps
```

You should see `equipes-postgres` with status `healthy`.

### 2. Build the project

```bash
mvn clean package -DskipTests
```

### 3. Start the server and client

```bash
mvn exec:java
```

You'll be prompted:

- **Option 1**: Start the server (handles database migrations and room management)
- **Option 2**: Start the client (GUI application)

Run the **server first** in one terminal, then **client(s)** in other terminal(s).

### Environment variables (optional)

The server reads these from your shell (defaults work for local development):

```bash
APP_DB_URL=jdbc:postgresql://localhost:5432/equipes
APP_DB_USER=equipes
APP_DB_PASSWORD=equipes
APP_SERVER_PORT=8080
APP_AUDIO_UDP_PORT=8081
```

To override, export them before running:

```bash
export APP_SERVER_PORT=9090
mvn exec:java
```
## Troubleshooting

- Java version error: install the correct JDK and set `JAVA_HOME`.
- Main class not found: ensure `pom.xml` config for the jar plugin or run with `mvn exec:java -Dexec.mainClass="your.MainClass"`.
- PostgreSQL connection error: confirm `docker compose ps` shows `equipes-postgres` as healthy and verify `.env` values.
