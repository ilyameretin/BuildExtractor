import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;

public class BuildExtractor {
    public static final String DEFAULT_SOURCE_FOLDER_PATH = "\\\\str-data.aamajor.local\\DevFile\\TestBuilds";

    public static void main(String[] args) {
        String tfsNumber = ""; // Номер задачи из TFS

        String sourceFolderPath = DEFAULT_SOURCE_FOLDER_PATH;
        String destinationFolderPath = ""; // Здесь нужно указать путь назначения

        String buildFileName = findBuildFile(tfsNumber, sourceFolderPath);
        if (buildFileName != null) {
            String buildFilePath = sourceFolderPath + File.separator + tfsNumber + File.separator + buildFileName;
            extractBuild(buildFilePath, destinationFolderPath);
            System.out.println("Билд успешно разархивирован.");
        } else {
            System.out.println("Не удалось найти билд с номером " + tfsNumber);
        }
    }

    public static String findBuildFile(String tfsNumber, String sourceFolderPath) {
        try {
            List<Path> files = Files.walk(Paths.get(sourceFolderPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getParent().endsWith(tfsNumber))
                    .collect(Collectors.toList());

            if (!files.isEmpty()) {
                Path buildFilePath = files.get(0);
                return buildFilePath.getFileName().toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void extractBuild(String buildFilePath, String destinationFolderPath) {
        File buildFile = new File(buildFilePath);
        String fileExtension = getFileExtension(buildFile);

        if (fileExtension.equalsIgnoreCase("rar")) {
            extractRarBuild(buildFile, destinationFolderPath);
        } else if (fileExtension.equalsIgnoreCase("zip")) {
            extractZipBuild(buildFile, destinationFolderPath);
        } else {
            System.out.println("Неподдерживаемый формат архива: " + fileExtension);
        }
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private static void extractRarBuild(File buildFile, String destinationFolderPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("C:\\Program Files\\WinRAR\\WinRAR.exe", "x", "-o+", buildFile.getAbsolutePath(), destinationFolderPath);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void extractZipBuild(File buildFile, String destinationFolderPath) {
        try (ZipFile zipFile = new ZipFile(buildFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    File outputFile = new File(destinationFolderPath, entry.getName());
                    outputFile.getParentFile().mkdirs();

                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
