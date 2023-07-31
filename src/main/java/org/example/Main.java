import javax.swing.*;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                String destinationFolderPath = fileChooser.getSelectedFile().getPath();

                // Копирование папки Major Studio из исходной папки в папку назначения
                String sourceFolderPath = "C:\\Majorsoft\\Major Studio"; // Укажите исходную папку
                copyDirectory(sourceFolderPath, destinationFolderPath);

                new BuildExtractorGUI(destinationFolderPath).setVisible(true);
            }
        });
    }

    private static void copyDirectory(String sourceFolderPath, String destinationFolderPath) {
        File sourceDirectory = new File(sourceFolderPath);
        File destinationDirectory = new File(destinationFolderPath);

        try {
            FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
