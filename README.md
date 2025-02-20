# Maven Dependencies Analyzer
This project provides a command-line tool to interact with GitLab repositories and analyze Maven dependencies.


## Setup

1. Clone the repository:
    ```sh
    git clone <repository-url>
    cd maven-deps-analyzer
    ```

2. Ensure you have Maven and Java installed. Set the `MAVEN_HOME` environment variable:
    ```sh
    export MAVEN_HOME=/path/to/your/maven
    ```

3. Build the project:
    ```sh
    mvn clean install
    ```


## Running the CLI

To start the Spring Shell CLI, run the following command:

```sh
mvn spring-boot:run
```

## Usage

### Clone a GitLab Repository

```sh
clone --url <repository-url> --directory <local-directory>
```

### List Branches of a Local Repository

```sh
list-branches --directory <local-directory>
```

### List Commits of a Branch

```sh
list-commits --directory <local-directory> --branch <branch-name>
```

### List Maven Dependencies from a Directory

```sh
list-dependencies-dir --directory <local-directory>
```

### List Maven Dependencies from a ZIP File

```sh
list-dependencies-zip --directory <target-directory> --zipfile <zip-file-name>
```


## Alternatively, you can run the CLI directly using the Java command line:

```sh
java -jar target/maven-deps-analyzer-<version>.jar
```

Replace `<version>` with the appropriate version number of the built JAR file.

### To run specific commands using the `java -jar` command, use the following format:

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


## License

This project is licensed under the Apache License.
