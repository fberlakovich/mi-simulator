package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Manages file operations for the MI simulator GUI.
 * Handles opening, saving, and tracking the current file state.
 */
public class FileManager {

    private final JFileChooser fileChooser;
    private final Component parentComponent;
    private File currentFile;
    private String lastSavedContent = "";

    /**
     * Creates a new FileManager.
     *
     * @param parentComponent the parent component for dialogs
     */
    public FileManager(Component parentComponent) {
        this.parentComponent = parentComponent;
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileFilter(new FileNameExtensionFilter("MI-File", "mi"));
    }

    /**
     * Result of a file operation.
     */
    public static class FileResult {
        private final boolean success;
        private final String content;
        private final String errorMessage;

        private FileResult(boolean success, String content, String errorMessage) {
            this.success = success;
            this.content = content;
            this.errorMessage = errorMessage;
        }

        public static FileResult success(String content) {
            return new FileResult(true, content, null);
        }

        public static FileResult failure(String errorMessage) {
            return new FileResult(false, null, errorMessage);
        }

        public static FileResult cancelled() {
            return new FileResult(false, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isCancelled() {
            return !success && errorMessage == null;
        }

        public String getContent() {
            return content;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Opens a file using a file chooser dialog.
     *
     * @return the result containing the file content or error information
     */
    public FileResult openFile() {
        int returnVal = fileChooser.showOpenDialog(parentComponent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return FileResult.cancelled();
        }
        return openFile(fileChooser.getSelectedFile());
    }

    /**
     * Opens a specific file.
     *
     * @param file the file to open
     * @return the result containing the file content or error information
     */
    public FileResult openFile(File file) {
        try {
            String content = readFile(file);
            this.currentFile = file;
            this.lastSavedContent = content;
            return FileResult.success(content);
        } catch (IOException e) {
            return FileResult.failure(CONSTANTS.ERROR_OPENFILE);
        }
    }

    /**
     * Saves content to the current file, or prompts for a file if none is set.
     *
     * @param content the content to save
     * @return the result of the save operation
     */
    public FileResult save(String content) {
        if (currentFile == null) {
            return saveAs(content);
        }
        return saveToFile(currentFile, content);
    }

    /**
     * Saves content to a new file chosen by the user.
     *
     * @param content the content to save
     * @return the result of the save operation
     */
    public FileResult saveAs(String content) {
        int returnVal = fileChooser.showSaveDialog(parentComponent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return FileResult.cancelled();
        }
        File selectedFile = fileChooser.getSelectedFile();
        return saveToFile(selectedFile, content);
    }

    /**
     * Saves content to a specific file.
     *
     * @param file the file to save to
     * @param content the content to save
     * @return the result of the save operation
     */
    private FileResult saveToFile(File file, String content) {
        try {
            writeFile(file, content);
            this.currentFile = file;
            this.lastSavedContent = content;
            return FileResult.success(content);
        } catch (IOException e) {
            return FileResult.failure(CONSTANTS.ERROR_SAVEFILE);
        }
    }

    /**
     * Checks if there are unsaved changes.
     *
     * @param currentContent the current content to check against last saved
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges(String currentContent) {
        return !currentContent.equals(lastSavedContent);
    }

    /**
     * Prompts user to save if there are unsaved changes.
     *
     * @param currentContent the current content
     * @return true if the operation can continue (saved, discarded, or no changes),
     *         false if the user cancelled
     */
    public boolean promptSaveIfNeeded(String currentContent) {
        if (!hasUnsavedChanges(currentContent)) {
            return true;
        }

        String fileName = currentFile != null ? currentFile.getName() : "unbenannt.mi";
        int option = JOptionPane.showConfirmDialog(parentComponent,
                "Möchten Sie die Änderungen in " + fileName + " speichern?",
                "Änderungen speichern?",
                JOptionPane.YES_NO_CANCEL_OPTION);

        switch (option) {
            case JOptionPane.YES_OPTION:
                FileResult result = save(currentContent);
                return result.isSuccess();
            case JOptionPane.NO_OPTION:
                return true;
            case JOptionPane.CANCEL_OPTION:
            default:
                return false;
        }
    }

    /**
     * Resets to a new file state (no current file).
     */
    public void newFile() {
        currentFile = null;
        lastSavedContent = "";
        fileChooser.setSelectedFile(new File(""));
    }

    /**
     * Gets the current file, if any.
     *
     * @return an Optional containing the current file, or empty if none
     */
    public Optional<File> getCurrentFile() {
        return Optional.ofNullable(currentFile);
    }

    /**
     * Gets the current file name for display.
     *
     * @return the file name or "unbenannt.mi" if no file is set
     */
    public String getCurrentFileName() {
        return currentFile != null ? currentFile.getName() : "unbenannt.mi";
    }

    /**
     * Updates the last saved content marker (used after loading).
     *
     * @param content the content to mark as saved
     */
    public void markAsSaved(String content) {
        this.lastSavedContent = content;
    }

    /**
     * Reads a file's content.
     */
    private String readFile(File file) throws IOException {
        // Read byte by byte to match original behavior for character encoding
        StringBuilder content = new StringBuilder();
        try (FileInputStream in = new FileInputStream(file)) {
            int ch;
            while ((ch = in.read()) != -1) {
                content.append((char) ch);
            }
        }
        return content.toString();
    }

    /**
     * Writes content to a file.
     */
    private void writeFile(File file, String content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
