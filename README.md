# Light Teams

Light version of Microsoft Teams for a Network course project.

## Prerequisites

- JDK 11\+ installed (check with `java -version`)
- Maven 3.6\+ installed (check with `mvn -v`)
- macOS (instructions below)
- (Optional) IntelliJ IDEA for development and debugging

## Clone the repository

Use your fork or the original repo:

    git clone git@github.com:Baptiiiiste/Equipes.git
    cd Equipes

## Build and package

Build and create an executable JAR (skip tests if desired):

    mvn clean package -DskipTests

The artifact is typically in `target/` as `artifactId-version.jar`. Replace with the actual name from your `pom.xml`.

## Run the application

[//]: # (TODO: Define how to run the application \(client and server\) once built.)

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

Save this content to `README.md`.
