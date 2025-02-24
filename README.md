# Maven Dependencies Analyzer

This project provides a command-line tool to interact with Git repositories and analyze Maven dependencies.

## Setup

1. Clone the repository:

    ```sh
    git clone <repository-url>
    cd maven-deps-analyzer
    ```

2. Build the project:

    ```sh
    ./mvnw clean package
    ```

## Running the CLI

**You need at least Java 17 and Maven 3.9.x installed on your machine.**

### (1) Start Spring Shell CLI with Maven

To start the Spring Shell CLI with Maven, run the following command:

```sh
mvn spring-boot:run
```

Clone a GitLab Repository

```sh
clone --url <repository-url> --directory <local-directory>
```

List Branches of a Local Repository

```sh
list-branches --directory <local-directory>
```

List Commits of a Branch

```sh
list-commits --directory <local-directory> --branch <branch-name>
```

List Maven Dependencies from a Directory

```sh
list-dependencies-dir --directory <local-directory>
```

List Maven Dependencies from a ZIP File

```sh
list-dependencies-zip --directory <target-directory> --zipfile <zip-file-name>
```

List Maven Licenses from a ZIP File

```sh
list-licenses-zip --directory <target-directory> --zipfile <zip-file-name>
```

### (2) Start Spring Shell CLI with Java Command Line

```sh
java -jar target/maven-deps-analyzer-<version>.jar
```

Replace `<version>` with the appropriate version number of the built JAR file.

### (3) Start Directly with Java Command Line Using a Specific Command

To run specific commands using the `java -jar` command, use the following format:

```sh
java -jar target/maven-deps-analyzer-<version>.jar <command> [options]
```

For example, to clone a GitLab repository:

```sh
java -jar target/maven-deps-analyzer-<version>.jar clone --url <repository-url> --directory <local-directory>
```

To list branches of a local repository:

```sh
java -jar target/maven-deps-analyzer-<version>.jar list-branches --directory <local-directory>
```

To list commits of a branch:

```sh
java -jar target/maven-deps-analyzer-<version>.jar list-commits --directory <local-directory> --branch <branch-name>
```

To list Maven dependencies from a directory:

```sh
java -jar target/maven-deps-analyzer-<version>.jar list-dependencies-dir --directory <local-directory>
```

To list Maven dependencies from a ZIP file:

```sh
java -jar target/maven-deps-analyzer-<version>.jar list-dependencies-zip --directory <target-directory> --zipfile <zip-file-name>
```

To list Maven licenses from a ZIP file:

```sh
java -jar target/maven-deps-analyzer-<version>.jar list-licenses-zip --directory <target-directory> --zipfile <zip-file-name>
```

## License

This project is licensed under the Apache License.
