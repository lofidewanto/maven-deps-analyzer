package com.github.lofi.depsanalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class GitLabCommand {

    @ShellMethod(key = "clone", value = "Clone a GitLab repository.")
    public String cloneRepo(@ShellOption(value = "--url") String repoUrl,
            @ShellOption(value = "--directory") String directory) {
        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(directory))
                    .call();
            return "Repository successfully cloned to: " + directory;
        } catch (GitAPIException e) {
            return "Error cloning the repository: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-branches", value = "List all branches of a local repository.")
    public String listBranches(@ShellOption(value = "--directory") String directory) {
        try (Git git = Git.open(new File(directory))) {
            StringBuilder branches = new StringBuilder();
            git.branchList().call().forEach(ref -> branches.append(ref.getName()).append("\n"));
            return branches.toString();
        } catch (Exception e) {
            return "Error listing branches: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-commits", value = "List the latest commits of a branch.")
    public String listCommits(@ShellOption(value = "--directory") String directory,
            @ShellOption(value = "--branch") String branch) {
        try (Git git = Git.open(new File(directory))) {
            StringBuilder commits = new StringBuilder();
            git.log().add(git.getRepository().resolve(branch)).call()
                    .forEach(commit -> commits.append(commit.getFullMessage()).append("\n"));
            return commits.toString();
        } catch (Exception e) {
            return "Error listing commits: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-dependencies-dir", value = "List Maven dependencies of a project in a directory from the pom.xml.")
    public String listDependenciesFromDirectory(@ShellOption(value = "--directory") String directory) {
        try {
            return executeMavenDependencyTree(directory);
        } catch (Exception e) {
            return "Error retrieving Maven dependencies: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-dependencies-zip", value = "List Maven dependencies from a ZIP file with pom.xml.")
    public String listDependenciesFromZip(@ShellOption(value = "--zipfile") String zipFilePath,
            @ShellOption(value = "--directory") String extractDirectory) {
        try {
            File destDir = new File(extractDirectory);
            unzip(new File(zipFilePath), destDir);

            String zipFileName = getZipFileName(zipFilePath);

            return executeMavenDependencyTree(extractDirectory + "/" + zipFileName);
        } catch (Exception e) {
            return "Error processing the ZIP file: " + e.getMessage();
        }
    }

    String getZipFileName(String zipFilePath) {
        // Get the name of the zip file from the zipFilePath without the extension
        String zipFileName = zipFilePath.substring(zipFilePath.lastIndexOf('/') + 1, zipFilePath.lastIndexOf('.'));
        return zipFileName;
    }

    private void unzip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        Files.copy(zipInputStream, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private String executeMavenDependencyTree(String directory) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(directory, "pom.xml"));
        request.setGoals(Collections.singletonList("dependency:tree"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
        InvocationResult result = invoker.execute(request);

        if (result.getExitCode() == 0) {
            return "Maven dependencies successfully listed.";
        } else {
            return "Error retrieving Maven dependencies.";
        }
    }
}