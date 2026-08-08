import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSyncTool extends Application {

    private TextField sourceField;
    private TextField destinationField;

    private TableView<FileChange> table;
    private ObservableList<FileChange> changes =
            FXCollections.observableArrayList();

    private ProgressBar progressBar;
    private Label statusLabel;
    private TextArea logArea;

    private Button scanButton;
    private Button syncButton;

    @Override
    public void start(Stage stage) {

        sourceField = new TextField();
        destinationField = new TextField();

        sourceField.setPromptText("Source folder");
        destinationField.setPromptText("Destination folder");

        Button sourceButton =
                new Button("Browse");

        Button destinationButton =
                new Button("Browse");

        sourceButton.setOnAction(e ->
                chooseFolder(stage, sourceField));

        destinationButton.setOnAction(e ->
                chooseFolder(stage, destinationField));

        HBox sourceRow = new HBox(
                10,
                new Label("Source:"),
                sourceField,
                sourceButton
        );

        HBox destinationRow = new HBox(
                10,
                new Label("Destination:"),
                destinationField,
                destinationButton
        );

        HBox.setHgrow(sourceField, Priority.ALWAYS);
        HBox.setHgrow(destinationField, Priority.ALWAYS);

        scanButton = new Button("🔍 Scan");

        syncButton = new Button("🔄 Synchronize");

        syncButton.setDisable(true);

        scanButton.setOnAction(e -> scanFolders());

        syncButton.setOnAction(e -> synchronize());

        HBox actions = new HBox(
                10,
                scanButton,
                syncButton
        );

        actions.setAlignment(Pos.CENTER_LEFT);

        /* TABLE */

        table = new TableView<>();

        TableColumn<FileChange, String> pathColumn =
                new TableColumn<>("File");

        pathColumn.setCellValueFactory(
                data ->
                        data.getValue().pathProperty()
        );

        TableColumn<FileChange, String> typeColumn =
                new TableColumn<>("Change");

        typeColumn.setCellValueFactory(
                data ->
                        data.getValue().typeProperty()
        );

        TableColumn<FileChange, String> sizeColumn =
                new TableColumn<>("Size");

        sizeColumn.setCellValueFactory(
                data ->
                        data.getValue().sizeProperty()
        );

        TableColumn<FileChange, String> modifiedColumn =
                new TableColumn<>("Modified");

        modifiedColumn.setCellValueFactory(
                data ->
                        data.getValue().modifiedProperty()
        );

        pathColumn.setPrefWidth(400);
        typeColumn.setPrefWidth(130);
        sizeColumn.setPrefWidth(120);
        modifiedColumn.setPrefWidth(180);

        table.getColumns().addAll(
                pathColumn,
                typeColumn,
                sizeColumn,
                modifiedColumn
        );

        table.setItems(changes);

        /* PROGRESS */

        progressBar =
                new ProgressBar(0);

        progressBar.setMaxWidth(Double.MAX_VALUE);

        statusLabel =
                new Label("Ready.");

        /* LOG */

        logArea =
                new TextArea();

        logArea.setEditable(false);

        logArea.setPrefRowCount(7);

        /* TOP */

        VBox top = new VBox(
                12,
                sourceRow,
                destinationRow,
                actions
        );

        top.setPadding(new Insets(15));

        /* BOTTOM */

        VBox bottom = new VBox(
                8,
                statusLabel,
                progressBar,
                new Label("Activity Log"),
                logArea
        );

        bottom.setPadding(new Insets(15));

        VBox root = new VBox(
                top,
                table,
                bottom
        );

        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene =
                new Scene(root, 1000, 700);

        stage.setTitle(
                "File Synchronization Tool"
        );

        stage.setScene(scene);

        stage.show();
    }

    /* ==============================
       FOLDER SELECTION
       ============================== */

    private void chooseFolder(
            Stage stage,
            TextField field
    ) {

        DirectoryChooser chooser =
                new DirectoryChooser();

        chooser.setTitle(
                "Select Folder"
        );

        File folder =
                chooser.showDialog(stage);

        if (folder != null) {
            field.setText(
                    folder.getAbsolutePath()
            );
        }
    }

    /* ==============================
       SCAN
       ============================== */

    private void scanFolders() {

        if (!validateFolders())
            return;

        Path source =
                Paths.get(
                        sourceField.getText()
                );

        Path destination =
                Paths.get(
                        destinationField.getText()
                );

        changes.clear();

        syncButton.setDisable(true);

        statusLabel.setText(
                "Scanning folders..."
        );

        log(
                "Scanning source and destination..."
        );

        Thread thread =
                new Thread(() -> {

                    try {

                        Map<String, FileInfo> sourceFiles =
                                buildFileMap(source);

                        Map<String, FileInfo> destinationFiles =
                                buildFileMap(destination);

                        List<FileChange> detected =
                                compare(
                                        sourceFiles,
                                        destinationFiles
                                );

                        Platform.runLater(() -> {

                            changes.addAll(
                                    detected
                            );

                            statusLabel.setText(
                                    detected.size()
                                    + " changes detected."
                            );

                            syncButton.setDisable(
                                    detected.isEmpty()
                            );

                            log(
                                    "Scan complete: "
                                    + detected.size()
                                    + " changes."
                            );

                        });

                    } catch (Exception ex) {

                        Platform.runLater(() ->
                                showError(
                                        "Scan failed",
                                        ex.getMessage()
                                )
                        );
                    }

                });

        thread.setDaemon(true);

        thread.start();
    }

    /* ==============================
       BUILD FILE MAP
       ============================== */

    private Map<String, FileInfo> buildFileMap(
            Path root
    ) throws IOException {

        Map<String, FileInfo> files =
                new HashMap<>();

        if (!Files.exists(root))
            return files;

        Files.walkFileTree(
                root,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attrs
                    ) {

                        String relative =
                                root.relativize(file)
                                        .toString();

                        files.put(
                                relative,
                                new FileInfo(
                                        relative,
                                        attrs.size(),
                                        attrs.lastModifiedTime()
                                                .toMillis()
                                )
                        );

                        return FileVisitResult.CONTINUE;
                    }
                }
        );

        return files;
    }

    /* ==============================
       COMPARE
       ============================== */

    private List<FileChange> compare(
            Map<String, FileInfo> source,
            Map<String, FileInfo> destination
    ) {

        List<FileChange> result =
                new ArrayList<>();

        for (String path : source.keySet()) {

            FileInfo sourceInfo =
                    source.get(path);

            FileInfo destinationInfo =
                    destination.get(path);

            if (destinationInfo == null) {

                result.add(
                        new FileChange(
                                path,
                                "NEW",
                                formatSize(
                                        sourceInfo.size
                                ),
                                formatDate(
                                        sourceInfo.modified
                                )
                        )
                );

            } else if (
                    sourceInfo.size
                            != destinationInfo.size
                    ||
                    Math.abs(
                            sourceInfo.modified
                                    - destinationInfo.modified
                    ) > 2000
            ) {

                result.add(
                        new FileChange(
                                path,
                                "MODIFIED",
                                formatSize(
                                        sourceInfo.size
                                ),
                                formatDate(
                                        sourceInfo.modified
                                )
                        )
                );
            }
        }

        for (String path : destination.keySet()) {

            if (!source.containsKey(path)) {

                FileInfo info =
                        destination.get(path);

                result.add(
                        new FileChange(
                                path,
                                "EXTRA",
                                formatSize(
                                        info.size
                                ),
                                formatDate(
                                        info.modified
                                )
                        )
                );
            }
        }

        result.sort(
                Comparator.comparing(
                        FileChange::getType
                )
        );

        return result;
    }

    /* ==============================
       SYNCHRONIZE
       ============================== */

    private void synchronize() {

        if (!validateFolders())
            return;

        if (changes.isEmpty())
            return;

        Path source =
                Paths.get(
                        sourceField.getText()
                );

        Path destination =
                Paths.get(
                        destinationField.getText()
                );

        scanButton.setDisable(true);
        syncButton.setDisable(true);

        statusLabel.setText(
                "Synchronizing..."
        );

        Thread thread =
                new Thread(() -> {

                    int total =
                            changes.size();

                    AtomicInteger completed =
                            new AtomicInteger();

                    try {

                        for (FileChange change :
                                changes) {

                            Path sourceFile =
                                    source.resolve(
                                            change.getPath()
                                    );

                            Path destinationFile =
                                    destination.resolve(
                                            change.getPath()
                                    );

                            if (
                                    change.getType()
                                            .equals("NEW")
                                    ||
                                    change.getType()
                                            .equals("MODIFIED")
                            ) {

                                Files.createDirectories(
                                        destinationFile
                                                .getParent()
                                );

                                Files.copy(
                                        sourceFile,
                                        destinationFile,
                                        StandardCopyOption
                                                .REPLACE_EXISTING
                                );

                                log(
                                        "Copied: "
                                        + change.getPath()
                                );

                            } else if (
                                    change.getType()
                                            .equals("EXTRA")
                            ) {

                                /*
                                 * We do not automatically
                                 * delete EXTRA files.
                                 *
                                 * This makes synchronization
                                 * safer.
                                 */
                                log(
                                        "Skipped extra file: "
                                        + change.getPath()
                                );
                            }

                            int done =
                                    completed.incrementAndGet();

                            double progress =
                                    (double) done
                                    / total;

                            Platform.runLater(() -> {

                                progressBar
                                        .setProgress(
                                                progress
                                        );

                                statusLabel.setText(
                                        "Synchronizing "
                                        + done
                                        + " / "
                                        + total
                                );
                            });
                        }

                        Platform.runLater(() -> {

                            statusLabel.setText(
                                    "Synchronization complete."
                            );

                            scanButton.setDisable(false);

                            progressBar.setProgress(1);

                            log(
                                    "Synchronization finished."
                            );

                        });

                    } catch (Exception ex) {

                        Platform.runLater(() -> {

                            showError(
                                    "Synchronization failed",
                                    ex.getMessage()
                            );

                            scanButton.setDisable(false);

                        });
                    }

                });

        thread.setDaemon(true);

        thread.start();
    }

    /* ==============================
       VALIDATION
       ============================== */

    private boolean validateFolders() {

        if (
                sourceField.getText()
                        .isBlank()
                ||
                destinationField.getText()
                        .isBlank()
        ) {

            showError(
                    "Missing folders",
                    "Select both folders first."
            );

            return false;
        }

        Path source =
                Paths.get(
                        sourceField.getText()
                );

        Path destination =
                Paths.get(
                        destinationField.getText()
                );

        if (!Files.isDirectory(source)) {

            showError(
                    "Invalid source",
                    "The source folder does not exist."
            );

            return false;
        }

        if (!Files.exists(destination)) {

            try {

                Files.createDirectories(
                        destination
                );

            } catch (IOException e) {

                showError(
                        "Destination error",
                        e.getMessage()
                );

                return false;
            }
        }

        if (!Files.isDirectory(destination)) {

            showError(
                    "Invalid destination",
                    "Destination is not a folder."
            );

            return false;
        }

        if (source.equals(destination)) {

            showError(
                    "Invalid folders",
                    "Source and destination must be different."
            );

            return false;
        }

        return true;
    }

    /* ==============================
       LOG
       ============================== */

    private void log(String message) {

        Platform.runLater(() ->
                logArea.appendText(
                        message + "\n"
                )
        );
    }

    /* ==============================
       FORMATTING
       ============================== */

    private String formatSize(long bytes) {

        if (bytes < 1024)
            return bytes + " B";

        if (bytes < 1024 * 1024)
            return String.format(
                    "%.1f KB",
                    bytes / 1024.0
            );

        if (bytes < 1024 * 1024 * 1024)
            return String.format(
                    "%.1f MB",
                    bytes / (1024.0 * 1024)
            );

        return String.format(
                "%.1f GB",
                bytes / (1024.0 * 1024 * 1024)
        );
    }

    private String formatDate(long time) {

        return new Date(time).toString();
    }

    /* ==============================
       ERROR
       ============================== */

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void main(String[] args) {

        launch(args);
    }

    /* ==============================
       FILE INFO
       ============================== */

    private static class FileInfo {

        String path;
        long size;
        long modified;

        FileInfo(
                String path,
                long size,
                long modified
        ) {

            this.path = path;
            this.size = size;
            this.modified = modified;
        }
    }

    /* ==============================
       FILE CHANGE
       ============================== */

    public static class FileChange {

        private final javafx.beans.property
                .SimpleStringProperty path;

        private final javafx.beans.property
                .SimpleStringProperty type;

        private final javafx.beans.property
                .SimpleStringProperty size;

        private final javafx.beans.property
                .SimpleStringProperty modified;

        public FileChange(
                String path,
                String type,
                String size,
                String modified
        ) {

            this.path =
                    new javafx.beans.property
                            .SimpleStringProperty(path);

            this.type =
                    new javafx.beans.property
                            .SimpleStringProperty(type);

            this.size =
                    new javafx.beans.property
                            .SimpleStringProperty(size);

            this.modified =
                    new javafx.beans.property
                            .SimpleStringProperty(modified);
        }

        public String getPath() {
            return path.get();
        }

        public String getType() {
            return type.get();
        }

        public javafx.beans.property
                .StringProperty pathProperty() {
            return path;
        }

        public javafx.beans.property
                .StringProperty typeProperty() {
            return type;
        }

        public javafx.beans.property
                .StringProperty sizeProperty() {
            return size;
        }

        public javafx.beans.property
                .StringProperty modifiedProperty() {
            return modified;
        }
    }
}
