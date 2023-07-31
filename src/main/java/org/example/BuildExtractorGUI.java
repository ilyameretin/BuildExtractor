import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BuildExtractorGUI extends JFrame {
    private JTextField tfsNumberTextField;
    private JTextField destinationTextField;

    public BuildExtractorGUI(String destinationFolderPath) {
        setTitle("Build Extractor");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        JLabel tfsNumberLabel = new JLabel("TFS Number:");
        tfsNumberTextField = new JTextField();
        JLabel destinationLabel = new JLabel("Destination Folder:");
        destinationTextField = new JTextField(destinationFolderPath);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(BuildExtractorGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    destinationTextField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        JButton extractButton = new JButton("Extract Build");
        extractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tfsNumber = tfsNumberTextField.getText();
                String destinationFolderPath = destinationTextField.getText();

                // Копирование папки Major Studio из исходной папки в папку назначения
                String sourceFolderPath = "C:\\Majorsoft\\Major Studio"; // Укажите исходную папку
                try {
                    copyDirectory(sourceFolderPath, destinationFolderPath);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(BuildExtractorGUI.this, "Failed to copy files.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String buildFolderPath = "\\\\str-data.aamajor.local\\DevFile\\TestBuilds";
                String buildFilePath = findBuildFile(tfsNumber, buildFolderPath);
                if (buildFilePath != null) {
                    extractBuild(buildFilePath, destinationFolderPath);
                    JOptionPane.showMessageDialog(BuildExtractorGUI.this, "Build successfully extracted.");
                } else {
                    JOptionPane.showMessageDialog(BuildExtractorGUI.this, "Failed to find build with TFS Number " + tfsNumber, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(tfsNumberLabel);
        panel.add(tfsNumberTextField);
        panel.add(destinationLabel);
        panel.add(destinationTextField);
        panel.add(new JLabel());
        panel.add(browseButton);
        panel.add(new JLabel());
        panel.add(extractButton);

        add(panel);
    }

    private void copyDirectory(String sourceFolderPath, String destinationFolderPath) throws IOException {
        File sourceDirectory = new File(sourceFolderPath);
        File destinationDirectory = new File(destinationFolderPath);

        Files.walk(sourceDirectory.toPath())
                .forEach(source -> {
                    try {
                        Files.copy(source, destinationDirectory.toPath().resolve(sourceDirectory.toPath().relativize(source)), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private String findBuildFile(String tfsNumber, String sourceFolderPath) {
        try {
            List<Path> files = Files.walk(Path.of(sourceFolderPath))
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

    private void extractBuild(String buildFilePath, String destinationFolderPath) {
        File buildFile = new File(buildFilePath);
        String fileExtension = getFileExtension(buildFile);

        if (fileExtension.equalsIgnoreCase("rar")) {
            extractRarBuild(buildFile, destinationFolderPath);
        } else if (fileExtension.equalsIgnoreCase("zip")) {
            extractZipBuild(buildFile, destinationFolderPath);
        } else {
            System.out.println("Unsupported archive format: " + fileExtension);
        }
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private void extractRarBuild(File buildFile, String destinationFolderPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("C:\\Program Files\\WinRAR\\WinRAR.exe", "x", "-o+", buildFile.getAbsolutePath(), destinationFolderPath);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void extractZipBuild(File buildFile, String destinationFolderPath) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String destinationFolderPath = "C:\\DestinationFolder"; // Specify the default destination folder
            new BuildExtractorGUI(destinationFolderPath).setVisible(true);
        });
    }
}
