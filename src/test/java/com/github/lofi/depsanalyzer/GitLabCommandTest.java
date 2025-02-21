package com.github.lofi.depsanalyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GitLabCommandTest {

    private GitLabCommand gitLabCommand;

    @BeforeEach
    void setUp() {
        gitLabCommand = new GitLabCommand();
    }

    @Test
    void get_zip_file_name() {
        String zipFilePath = "target/repo.zip";

        String result = gitLabCommand.getZipFileName(zipFilePath);

        assertEquals("repo", result);
    }

    @Test
    void find_pom_file_recursively_with_pom_file() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        Path pomFile = Files.createFile(tempDir.resolve("pom.xml"));

        File result = gitLabCommand.findPomFileRecursively(tempDir.toFile());

        assertNotNull(result);

        Files.deleteIfExists(pomFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void find_pom_file_recursively_without_pom_file() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");

        File result = gitLabCommand.findPomFileRecursively(tempDir.toFile());

        assertNull(result);

        Files.deleteIfExists(tempDir);
    }

    @Test
    void find_pom_file_recursively_in_subdirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        Path subDir = Files.createDirectory(tempDir.resolve("subDir"));
        Path pomFile = Files.createFile(subDir.resolve("pom.xml"));

        File result = gitLabCommand.findPomFileRecursively(tempDir.toFile());

        assertNotNull(result);
        
        Files.deleteIfExists(pomFile);
        Files.deleteIfExists(subDir);
        Files.deleteIfExists(tempDir);
    }
}
