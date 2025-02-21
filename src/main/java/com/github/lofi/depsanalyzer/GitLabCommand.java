package com.github.lofi.depsanalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ShellComponent
public class GitLabCommand {

    private static final Logger logger = LoggerFactory.getLogger(GitLabCommand.class);

    @ShellMethod(key = "clone", value = "Clone a GitLab repository.")
    public String cloneRepo(@ShellOption(value = "--url") String repoUrl,
            @ShellOption(value = "--directory") String directory) {
        try {
            logger.info("Cloning repository from URL: {}", repoUrl);
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(directory))
                    .call();
            logger.info("Repository successfully cloned to: {}", directory);
            return "Repository successfully cloned to: " + directory;
        } catch (GitAPIException e) {
            logger.error("Error cloning the repository: {}", e.getMessage(), e);
            return "Error cloning the repository: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-branches", value = "List all branches of a local repository.")
    public String listBranches(@ShellOption(value = "--directory") String directory) {
        try (Git git = Git.open(new File(directory))) {
            logger.info("Listing branches for repository in directory: {}", directory);
            StringBuilder branches = new StringBuilder();
            git.branchList().call().forEach(ref -> branches.append(ref.getName()).append("\n"));
            return branches.toString();
        } catch (Exception e) {
            logger.error("Error listing branches: {}", e.getMessage(), e);
            return "Error listing branches: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-commits", value = "List the latest commits of a branch.")
    public String listCommits(@ShellOption(value = "--directory") String directory,
            @ShellOption(value = "--branch") String branch) {
        try (Git git = Git.open(new File(directory))) {
            logger.info("Listing commits for branch: {} in directory: {}", branch, directory);
            StringBuilder commits = new StringBuilder();
            git.log().add(git.getRepository().resolve(branch)).call()
                    .forEach(commit -> commits.append(commit.getFullMessage()).append("\n"));
            return commits.toString();
        } catch (Exception e) {
            logger.error("Error listing commits: {}", e.getMessage(), e);
            return "Error listing commits: " + e.getMessage();
        }
    }

}