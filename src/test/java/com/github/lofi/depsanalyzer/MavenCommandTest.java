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

class MavenCommandTest {

    private MavenCommand mavenCommand;

    @BeforeEach
    void setUp() {
        mavenCommand = new MavenCommand();
    }

    @Test
    void get_zip_file_name() {
        String zipFilePath = "target/repo.zip";

        String result = mavenCommand.getZipFileName(zipFilePath);

        assertEquals("repo", result);
    }

    @Test
    void find_pom_file_recursively_with_pom_file() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        Path pomFile = Files.createFile(tempDir.resolve("pom.xml"));

        File result = mavenCommand.findPomFileRecursively(tempDir.toFile());

        assertNotNull(result);

        Files.deleteIfExists(pomFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void find_pom_file_recursively_without_pom_file() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");

        File result = mavenCommand.findPomFileRecursively(tempDir.toFile());

        assertNull(result);

        Files.deleteIfExists(tempDir);
    }

    @Test
    void find_pom_file_recursively_in_subdirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        Path subDir = Files.createDirectory(tempDir.resolve("subDir"));
        Path pomFile = Files.createFile(subDir.resolve("pom.xml"));

        File result = mavenCommand.findPomFileRecursively(tempDir.toFile());

        assertNotNull(result);
        
        Files.deleteIfExists(pomFile);
        Files.deleteIfExists(subDir);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void extract_file_path_from_result_valid_result() {
        String result = "[INFO] Writing third-party file to /path/to/third-party-file.txt";
        String expected = "/path/to/third-party-file.txt";
        String actual = mavenCommand.extractFilePathFromResult(result);
        assertEquals(expected, actual);
    }

    @Test
    void extract_file_path_from_result_no_match() {
        String result = "Some log output\nNo matching line here\nMore log output";
        String actual = mavenCommand.extractFilePathFromResult(result);
        assertNull(actual);
    }

    @Test
    void extract_file_path_from_result_empty_result() {
        String result = "";
        String actual = mavenCommand.extractFilePathFromResult(result);
        assertNull(actual);
    }

    @Test
    void find_pom_file_recursively_in_build_directory() throws IOException {
        // Setup
        Path tempDir = Files.createTempDirectory("testDir");
        Path buildDir = Files.createDirectory(tempDir.resolve("Build_App"));
        Path pomFile = Files.createFile(buildDir.resolve("pom.xml"));

        // Execute
        File result = mavenCommand.findPomFileRecursively(tempDir.toFile());

        // Verify
        assertNotNull(result);
        assertEquals(pomFile.toFile(), result);

        // Cleanup
        Files.deleteIfExists(pomFile);
        Files.deleteIfExists(buildDir);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void find_pom_file_recursively_in_nested_directory() throws IOException {
        // Setup
        Path tempDir = Files.createTempDirectory("testDir");
        Path nestedDir = Files.createDirectory(tempDir.resolve("nested"));
        Path pomFile = Files.createFile(nestedDir.resolve("pom.xml"));

        MavenCommand mavenCommand = new MavenCommand();

        // Execute
        File result = mavenCommand.findPomFileRecursively(tempDir.toFile());

        // Verify
        assertNotNull(result);
        assertEquals(pomFile.toFile(), result);

        // Cleanup
        Files.deleteIfExists(pomFile);
        Files.deleteIfExists(nestedDir);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void find_pom_file_recursively_no_pom_file() throws IOException {
        // Setup
        Path tempDir = Files.createTempDirectory("testDir");

        MavenCommand mavenCommand = new MavenCommand();

        // Execute
        File result = mavenCommand.findPomFileRecursively(tempDir.toFile());

        // Verify
        assertNull(result);

        // Cleanup
        Files.deleteIfExists(tempDir);
    }
}
