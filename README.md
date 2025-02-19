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


## License

This project is licensed under the Apache License.
