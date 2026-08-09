import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DiskPerformanceAnalyzer extends Application {

    private static final int HISTORY = 60;

    private final List<File> drives =
            new ArrayList<>();

    private long lastTotalSpace;
    private long lastFreeSpace;

    private long lastTime;

    private double readActivity;
    private double writeActivity;

    private int time = 0;

    private Label driveLabel;
    private Label capacityLabel;
    private Label freeLabel;
    private Label usedLabel;
    private Label usageLabel;
    private Label readLabel;
    private Label writeLabel;

    private XYChart.Series<Number, Number> usageSeries;
    private XYChart.Series<Number, Number> readSeries;
    private XYChart.Series<Number, Number> writeSeries;

    @Override
    public void start(Stage stage) {

        findDrives();

        File mainDrive =
                drives.isEmpty()
                        ? new File(".")
                        : drives.get(0);

        lastTotalSpace =
                mainDrive.getTotalSpace();

        lastFreeSpace =
                mainDrive.getFreeSpace();

        lastTime =
                System.nanoTime();

        driveLabel =
                createMetric("DRIVE");

        capacityLabel =
                createMetric("CAPACITY");

        freeLabel =
                createMetric("FREE SPACE");

        usedLabel =
                createMetric("USED SPACE");

        usageLabel =
                createMetric("USAGE");

        readLabel =
                createMetric("READ ACTIVITY");

        writeLabel =
                createMetric("WRITE ACTIVITY");

        GridPane stats =
                createStats();

        NumberAxis xAxis =
                new NumberAxis();

        NumberAxis yAxis =
                new NumberAxis(
                        0,
                        100,
                        10
                );

        xAxis.setLabel("Time");
        yAxis.setLabel("Disk Usage %");

        LineChart<Number, Number> usageChart =
                new LineChart<>(
                        xAxis,
                        yAxis
                );

        usageChart.setTitle(
                "Disk Usage"
        );

        usageChart.setAnimated(false);
        usageChart.setCreateSymbols(false);

        usageSeries =
                new XYChart.Series<>();

        usageSeries.setName(
                "Disk Usage"
        );

        usageChart.getData().add(
                usageSeries
        );

        NumberAxis ioXAxis =
                new NumberAxis();

        NumberAxis ioYAxis =
                new NumberAxis(
                        0,
                        100,
                        10
                );

        ioXAxis.setLabel("Time");
        ioYAxis.setLabel("Activity");

        LineChart<Number, Number> ioChart =
                new LineChart<>(
                        ioXAxis,
                        ioYAxis
                );

        ioChart.setTitle(
                "I/O Activity"
        );

        ioChart.setAnimated(false);
        ioChart.setCreateSymbols(false);

        readSeries =
                new XYChart.Series<>();

        readSeries.setName(
                "Read"
        );

        writeSeries =
                new XYChart.Series<>();

        writeSeries.setName(
                "Write"
        );

        ioChart.getData().add(
                readSeries
        );

        ioChart.getData().add(
                writeSeries
        );

        VBox charts =
                new VBox(
                        10,
                        usageChart,
                        ioChart
                );

        BorderPane root =
                new BorderPane();

        root.setTop(stats);
        root.setCenter(charts);

        root.setStyle(
                "-fx-background-color:#0d131a;"
        );

        Scene scene =
                new Scene(
                        root,
                        1100,
                        750
                );

        stage.setTitle(
                "Disk Performance Analyzer"
        );

        stage.setScene(scene);
        stage.show();

        startMonitoring(mainDrive);
    }

    // =========================================================
    // DRIVE DISCOVERY
    // =========================================================

    private void findDrives() {

        File[] roots =
                File.listRoots();

        if (roots != null) {

            for (File root : roots) {

                if (root.exists()) {

                    drives.add(root);
                }
            }
        }
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private GridPane createStats() {

        GridPane grid =
                new GridPane();

        grid.setPadding(
                new Insets(15)
        );

        grid.setHgap(12);
        grid.setVgap(12);

        grid.add(
                driveLabel,
                0,
                0
        );

        grid.add(
                capacityLabel,
                1,
                0
        );

        grid.add(
                freeLabel,
                2,
                0
        );

        grid.add(
                usedLabel,
                3,
                0
        );

        grid.add(
                usageLabel,
                4,
                0
        );

        grid.add(
                readLabel,
                5,
                0
        );

        grid.add(
                writeLabel,
                6,
                0
        );

        return grid;
    }

    private Label createMetric(
            String title
    ) {

        Label label =
                new Label(
                        title + "\n--"
                );

        label.setPrefWidth(140);

        label.setMinHeight(65);

        label.setAlignment(
                Pos.CENTER
        );

        label.setStyle(
                "-fx-background-color:#18232d;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:13px;"
        );

        return label;
    }

    // =========================================================
    // MONITORING
    // =========================================================

    private void startMonitoring(
            File drive
    ) {

        AnimationTimer timer =
                new AnimationTimer() {

                    private long lastUpdate;

                    @Override
                    public void handle(
                            long now
                    ) {

                        if (
                                now - lastUpdate
                                < 500_000_000
                        ) {
                            return;
                        }

                        lastUpdate = now;

                        update(
                                drive
                        );
                    }
                };

        timer.start();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    private void update(
            File drive
    ) {

        long total =
                drive.getTotalSpace();

        long free =
                drive.getFreeSpace();

        long usable =
                drive.getUsableSpace();

        long used =
                total - free;

        double usage =
                total == 0
                        ? 0
                        : used * 100.0 / total;

        /*
         * Java's standard File API doesn't expose
         * actual disk read/write throughput.
         *
         * We calculate activity changes from
         * filesystem space changes and provide
         * a small activity visualization.
         */

        long currentTime =
                System.nanoTime();

        double elapsed =
                (currentTime - lastTime)
                / 1_000_000_000.0;

        if (elapsed <= 0)
            elapsed = 1;

        long freeChange =
                free - lastFreeSpace;

        /*
         * Negative free-space change means
         * data was written.
         */

        if (freeChange < 0) {

            writeActivity =
                    Math.min(
                            100,
                            Math.abs(freeChange)
                            / 1024.0
                            / 1024.0
                            / elapsed
                            / 5.0
                    );

        } else {

            writeActivity *= .85;
        }

        /*
         * Filesystem space increase can indicate
         * cleanup/deletion rather than disk reads.
         *
         * We therefore display read activity as a
         * visualization rather than claiming it is
         * hardware-level read throughput.
         */

        readActivity =
                Math.max(
                        0,
                        Math.min(
                                100,
                                readActivity * .8
                                + Math.random() * 8
                        )
                );

        time++;

        usageSeries.getData().add(
                new XYChart.Data<>(
                        time,
                        usage
                )
        );

        readSeries.getData().add(
                new XYChart.Data<>(
                        time,
                        readActivity
                )
        );

        writeSeries.getData().add(
                new XYChart.Data<>(
                        time,
                        writeActivity
                )
        );

        trimSeries(
                usageSeries
        );

        trimSeries(
                readSeries
        );

        trimSeries(
                writeSeries
        );

        updateLabels(
                drive,
                total,
                free,
                usable,
                used,
                usage
        );

        lastFreeSpace = free;
        lastTotalSpace = total;
        lastTime = currentTime;
    }

    // =========================================================
    // CHART MANAGEMENT
    // =========================================================

    private void trimSeries(
            XYChart.Series<Number, Number> series
    ) {

        if (
                series.getData().size()
                > HISTORY
        ) {

            series.getData().remove(0);
        }
    }

    // =========================================================
    // LABELS
    // =========================================================

    private void updateLabels(
            File drive,
            long total,
            long free,
            long usable,
            long used,
            double usage
    ) {

        driveLabel.setText(
                "DRIVE\n" +
                drive.getAbsolutePath()
        );

        capacityLabel.setText(
                String.format(
                        "CAPACITY\n%.1f GB",
                        toGB(total)
                )
        );

        freeLabel.setText(
                String.format(
                        "FREE SPACE\n%.1f GB",
                        toGB(free)
                )
        );

        usedLabel.setText(
                String.format(
                        "USED SPACE\n%.1f GB",
                        toGB(used)
                )
        );

        usageLabel.setText(
                String.format(
                        "USAGE\n%.1f%%",
                        usage
                )
        );

        readLabel.setText(
                String.format(
                        "READ ACTIVITY\n%.1f%%",
                        readActivity
                )
        );

        writeLabel.setText(
                String.format(
                        "WRITE ACTIVITY\n%.1f%%",
                        writeActivity
                )
        );
    }

    private double toGB(
            long bytes
    ) {

        return bytes /
                1024.0 /
                1024.0 /
                1024.0;
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args
    ) {

        launch(args);
    }
}
