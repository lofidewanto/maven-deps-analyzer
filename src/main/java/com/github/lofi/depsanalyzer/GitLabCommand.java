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

    @ShellMethod(key = "list-dependencies-dir", value = "List Maven dependencies of a project in a directory from the pom.xml.")
    public String listDependenciesFromDirectory(@ShellOption(value = "--directory") String directory) {
        try {
            logger.info("Listing dependencies for project in directory: {}", directory);
            return executeMavenDependencyTree(directory);
        } catch (Exception e) {
            logger.error("Error retrieving Maven dependencies: {}", e.getMessage(), e);
            return "Error retrieving Maven dependencies: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-dependencies-zip", value = "List Maven dependencies from a ZIP file with pom.xml.")
    public String listDependenciesFromZip(@ShellOption(value = "--zipfile") String zipFilePath,
            @ShellOption(value = "--directory") String extractDirectory) {
        try {
            logger.info("Listing dependencies from ZIP file: {} into directory: {}", zipFilePath, extractDirectory);
            File destDir = new File(extractDirectory);
            unzip(new File(zipFilePath), destDir);

            String zipFileName = getZipFileName(zipFilePath);

            return executeMavenDependencyTree(extractDirectory + "/" + zipFileName);
        } catch (Exception e) {
            logger.error("Error processing the ZIP file: {}", e.getMessage(), e);
            return "Error processing the ZIP file: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list-licenses-zip", value = "List licenses of Maven dependencies from a ZIP file with pom.xml.")
    public String listDependencyLicenses(@ShellOption(value = "--zipfile") String zipFilePath,
            @ShellOption(value = "--directory") String extractDirectory) {
        try {
            logger.info("Listing licenses from ZIP file: {} into directory: {}", zipFilePath, extractDirectory);
            File destDir = new File(extractDirectory);
            unzip(new File(zipFilePath), destDir);

            String zipFileName = getZipFileName(zipFilePath);
            String outputFilePath = extractDirectory + "/" + zipFileName + ".txt";

            String result = executeMavenLicenseList(extractDirectory + "/" + zipFileName);

            if (!outputFilePath.isEmpty()) {
                saveToFile(result, outputFilePath);
                logger.info("Licenses successfully listed and saved to: {}", outputFilePath);
            }

            String resultFilePath = extractFilePathFromResult(result);

            Files.copy(Paths.get(resultFilePath), Paths.get(extractDirectory + "/" + zipFileName + "-result.txt"), 
                StandardCopyOption.REPLACE_EXISTING);
            logger.info("Result file copied to: {}", extractDirectory + "/" + zipFileName + "-result.txt");

            return "Licenses successfully listed and saved to: " + outputFilePath;
        } catch (Exception e) {
            logger.error("Error retrieving Maven dependency licenses: {}", e.getMessage(), e);
            return "Error retrieving Maven dependency licenses: " + e.getMessage();
        }
    }

    private String executeMavenLicenseList(String directory) throws MavenInvocationException {
        File pomFile = new File(directory, "pom.xml");
        if (!pomFile.exists()) {
            pomFile = findPomFileRecursively(new File(directory));
        }

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Collections.singletonList("license:add-third-party"));

        StringBuilder output = new StringBuilder();
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
        invoker.setWorkingDirectory(pomFile.getParentFile());

        logger.info("Executing Maven license:add-third-party for project in directory: {}", directory);
        
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String line) {
                output.append(line).append("\n");
            }
        });

        InvocationResult result = invoker.execute(request);

        if (result.getExitCode() == 0) {
            return output.toString();
        } else {
            return "Error retrieving Maven dependency licenses: " + output.toString();
        }
    }

    private String executeMavenDependencyTree(String directory) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();

        File pomFile = new File(directory, "pom.xml");
        if (!pomFile.exists()) {
            pomFile = findPomFileRecursively(new File(directory));
        }
        request.setPomFile(pomFile);
        
        request.setGoals(Collections.singletonList("dependency:tree"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
        invoker.setWorkingDirectory(pomFile.getParentFile());

        logger.info("Executing Maven dependency:tree for project in directory: {}", directory);

        InvocationResult result = invoker.execute(request);

        if (result.getExitCode() == 0) {
            return "Maven dependencies successfully listed.";
        } else {
            return "Error retrieving Maven dependencies.";
        }
    }

    void saveToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    File findPomFileRecursively(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File pomFile = findPomFileRecursively(file);
                    if (pomFile != null) {
                        return pomFile;
                    }
                } else if (file.getName().equals("pom.xml")) {
                    return file;
                }
            }
        }
        return null;
    }

    String getZipFileName(String zipFilePath) {
        // Get the name of the zip file from the zipFilePath without the extension
        String zipFileName = zipFilePath.substring(zipFilePath.lastIndexOf('/') + 1, zipFilePath.lastIndexOf('.'));
        return zipFileName;
    }

    void unzip(File zipFile, File destDir) throws IOException {
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

    String extractFilePathFromResult(String result) {
        String searchString = "Writing third-party file to: ";
        int startIndex = result.indexOf(searchString);
        if (startIndex != -1) {
            int endIndex = result.indexOf("\n", startIndex);
            if (endIndex != -1) {
                return result.substring(startIndex + searchString.length(), endIndex).trim();
            }
        }
        return null;
    }
}