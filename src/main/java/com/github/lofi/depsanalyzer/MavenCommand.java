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
import java.util.ArrayList;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ShellComponent
public class MavenCommand {

    private static final Logger logger = LoggerFactory.getLogger(MavenCommand.class);

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
            String outputFilePath = extractDirectory + "/" + zipFileName + "-maven.txt";

            String result = executeMavenLicenseList(extractDirectory + "/" + zipFileName);

            if (!outputFilePath.isEmpty()) {
                saveToFile(result, outputFilePath);
                logger.info("Maven successfully executed and the result of Maven is saved to: {}", outputFilePath);
            }

            ArrayList<String> resultFilePaths = extractFilePathFromResult(result);
            
            // Process each found license file
            int fileCount = 0;
            for (String resultFilePath : resultFilePaths) {
                String extractFilename = extractDirectory + "/" + zipFileName + "-licenses-" + (++fileCount) + ".txt";
                Files.copy(Paths.get(resultFilePath), Paths.get(extractFilename), 
                    StandardCopyOption.REPLACE_EXISTING);
                logger.info("Result file copied from: {} to: {}", resultFilePath, extractFilename);
            }

            return String.format("Licenses successfully listed and saved %d files to: %s", 
                fileCount, extractDirectory);
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

        logger.info("Executing Maven license:add-third-party for project in directory: {}", 
            pomFile.getParentFile().getAbsolutePath());
        
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

        logger.info("Executing Maven dependency:tree for project in directory: {}", 
            pomFile.getParentFile().getAbsolutePath());

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
        // Check for directories starting with "build" (case-insensitive)
        File[] buildDirs = directory.listFiles((dir, name) -> name.toLowerCase().startsWith("build"));
        if (buildDirs != null) {
            for (File buildDir : buildDirs) {
                File pomFile = new File(buildDir, "pom.xml");
                if (pomFile.exists()) {
                    return pomFile;
                }
            }
        }

        // If no "build" directories found, search everywhere
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

    ArrayList<String> extractFilePathFromResult(String result) {
        ArrayList<String> paths = new ArrayList<>();
        String searchString = "Writing third-party file to ";
        int lastIndex = 0;
        
        while ((lastIndex = result.indexOf(searchString, lastIndex)) != -1) {
            int startIndex = lastIndex + searchString.length();
            int endIndex = result.indexOf("\n", startIndex);
            
            if (endIndex == -1) {
                // Handle case where this is the last line
                String path = result.substring(startIndex).trim();
                if (!path.isEmpty()) {
                    paths.add(path);
                }
                break;
            }
            
            String path = result.substring(startIndex, endIndex).trim();
            if (!path.isEmpty()) {
                paths.add(path);
            }
            
            lastIndex = endIndex;
        }
        
        return paths;
    }
}