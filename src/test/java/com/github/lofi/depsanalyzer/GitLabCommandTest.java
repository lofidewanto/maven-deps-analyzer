package com.github.lofi.depsanalyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitLabCommandTest {

    private GitLabCommand gitLabCommand;

    @BeforeEach
    void setUp() {
        gitLabCommand = new GitLabCommand();
    }

    @Test
    void testGetZipFileName() {
        String zipFilePath = "target/repo.zip";

        String result = gitLabCommand.getZipFileName(zipFilePath);

        assertEquals("repo", result);
    }
}
