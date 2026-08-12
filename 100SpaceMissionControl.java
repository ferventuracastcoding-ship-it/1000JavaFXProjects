import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class SpaceMissionControl extends Application {

    private double altitude = 0;
    private double speed = 0;
    private double fuel = 100;
    private double oxygen = 100;

    private boolean launched = false;
    private boolean missionComplete = false;

    private final Random random = new Random();

    private Label altitudeLabel;
    private Label speedLabel;
    private Label fuelLabel;
    private Label oxygenLabel;
    private Label statusLabel;
    private Label timerLabel;

    private ProgressBar fuelBar;
    private ProgressBar oxygenBar;

    private Timeline missionTimer;

    private int seconds = 0;

    @Override
    public void start(Stage stage) {

        Label title = new Label("🚀 SPACE MISSION CONTROL");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );

        Label mission = new Label("MISSION: ORBITAL-01");
        mission.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-text-fill: #8fd3ff;"
        );

        VBox header = new VBox(5, title, mission);
        header.setAlignment(Pos.CENTER);

        // ---------------- TELEMETRY ----------------

        altitudeLabel = createTelemetryLabel("Altitude: 0 km");
        speedLabel = createTelemetryLabel("Speed: 0 km/h");
        fuelLabel = createTelemetryLabel("Fuel: 100%");
        oxygenLabel = createTelemetryLabel("Oxygen: 100%");
        timerLabel = createTelemetryLabel("Mission Time: 00:00");

        VBox telemetry = new VBox(
                15,
                createSectionTitle("LIVE TELEMETRY"),
                altitudeLabel,
                speedLabel,
                fuelLabel,
                oxygenLabel,
                timerLabel
        );

        telemetry.setPadding(new Insets(20));
        telemetry.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-background-radius: 10;"
        );

        // ---------------- FUEL ----------------

        fuelBar = new ProgressBar(1);
        fuelBar.setPrefWidth(300);

        oxygenBar = new ProgressBar(1);
        oxygenBar.setPrefWidth(300);

        VBox resources = new VBox(
                15,
                createSectionTitle("RESOURCES"),
                new Label("Fuel"),
                fuelBar,
                new Label("Oxygen"),
                oxygenBar
        );

        resources.setPadding(new Insets(20));

        for (var node : resources.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: white;");
            }
        }

        resources.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-background-radius: 10;"
        );

        // ---------------- ROCKET DISPLAY ----------------

        StackPane rocketArea = createRocketArea();

        // ---------------- STATUS ----------------

        statusLabel = new Label("READY FOR LAUNCH");
        statusLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #00ff88;"
        );

        // ---------------- BUTTONS ----------------

        Button launchButton = new Button("🚀 LAUNCH");
        Button abortButton = new Button("⛔ ABORT");
        Button resetButton = new Button("↻ RESET");

        styleButton(launchButton);
        styleButton(abortButton);
        styleButton(resetButton);

        launchButton.setOnAction(e -> launchMission());
        abortButton.setOnAction(e -> abortMission());
        resetButton.setOnAction(e -> resetMission());

        HBox controls = new HBox(
                15,
                launchButton,
                abortButton,
                resetButton
        );

        controls.setAlignment(Pos.CENTER);

        VBox center = new VBox(
                20,
                rocketArea,
                statusLabel,
                controls
        );

        center.setAlignment(Pos.CENTER);

        // ---------------- MAIN LAYOUT ----------------

        BorderPane root = new BorderPane();

        root.setTop(header);
        root.setLeft(telemetry);
        root.setCenter(center);
        root.setRight(resources);

        root.setPadding(new Insets(20));

        root.setStyle(
                "-fx-background-color: #030712;"
        );

        BorderPane.setAlignment(header, Pos.CENTER);
        BorderPane.setMargin(header, new Insets(0, 0, 20, 0));

        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("Space Mission Control");
        stage.setScene(scene);
        stage.show();
    }

    // --------------------------------------------------
    // ROCKET DISPLAY
    // --------------------------------------------------

    private StackPane createRocketArea() {

        StackPane area = new StackPane();

        area.setPrefSize(500, 350);

        Rectangle background = new Rectangle(500, 350);
        background.setFill(Color.web("#020617"));

        Circle earth = new Circle(70);
        earth.setFill(Color.web("#2563eb"));

        Label rocket = new Label("🚀");
        rocket.setStyle("-fx-font-size: 70px;");

        VBox display = new VBox(
                10,
                earth,
                rocket
        );

        display.setAlignment(Pos.CENTER);

        area.getChildren().addAll(background, display);

        return area;
    }

    // --------------------------------------------------
    // LAUNCH
    // --------------------------------------------------

    private void launchMission() {

        if (launched || missionComplete) {
            return;
        }

        launched = true;

        statusLabel.setText("🚀 LAUNCH IN PROGRESS");
        statusLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #00ff88;"
        );

        seconds = 0;

        missionTimer = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        e -> updateMission()
                )
        );

        missionTimer.setCycleCount(Timeline.INDEFINITE);
        missionTimer.play();
    }

    // --------------------------------------------------
    // MISSION UPDATE
    // --------------------------------------------------

    private void updateMission() {

        seconds++;

        if (fuel <= 0) {
            missionFailure("OUT OF FUEL");
            return;
        }

        if (oxygen <= 0) {
            missionFailure("OXYGEN DEPLETED");
            return;
        }

        // Increase altitude
        altitude += 8 + random.nextDouble() * 5;

        // Increase speed
        speed += 250 + random.nextDouble() * 100;

        // Consume fuel
        fuel -= 1.2 + random.nextDouble();

        // Consume oxygen slowly
        oxygen -= 0.15;

        if (fuel < 0) fuel = 0;
        if (oxygen < 0) oxygen = 0;

        updateTelemetry();

        // Reach orbit
        if (altitude >= 200) {

            altitude = 200;

            missionComplete = true;
            launched = false;

            missionTimer.stop();

            statusLabel.setText("✅ ORBIT ACHIEVED");

            statusLabel.setStyle(
                    "-fx-font-size: 20px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #00ff88;"
            );
        }
    }

    // --------------------------------------------------
    // ABORT
    // --------------------------------------------------

    private void abortMission() {

        if (!launched) {
            statusLabel.setText("NO ACTIVE MISSION");
            return;
        }

        launched = false;

        if (missionTimer != null) {
            missionTimer.stop();
        }

        statusLabel.setText("⛔ MISSION ABORTED");

        statusLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #ff4444;"
        );
    }

    // --------------------------------------------------
    // RESET
    // --------------------------------------------------

    private void resetMission() {

        if (missionTimer != null) {
            missionTimer.stop();
        }

        altitude = 0;
        speed = 0;
        fuel = 100;
        oxygen = 100;

        seconds = 0;

        launched = false;
        missionComplete = false;

        statusLabel.setText("READY FOR LAUNCH");

        statusLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #00ff88;"
        );

        updateTelemetry();
    }

    // --------------------------------------------------
    // FAILURE
    // --------------------------------------------------

    private void missionFailure(String reason) {

        launched = false;

        if (missionTimer != null) {
            missionTimer.stop();
        }

        statusLabel.setText("❌ MISSION FAILURE: " + reason);

        statusLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #ff3333;"
        );
    }

    // --------------------------------------------------
    // TELEMETRY
    // --------------------------------------------------

    private void updateTelemetry() {

        altitudeLabel.setText(
                String.format("Altitude: %.1f km", altitude)
        );

        speedLabel.setText(
                String.format("Speed: %.0f km/h", speed)
        );

        fuelLabel.setText(
                String.format("Fuel: %.1f%%", fuel)
        );

        oxygenLabel.setText(
                String.format("Oxygen: %.1f%%", oxygen)
        );

        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        timerLabel.setText(
                String.format(
                        "Mission Time: %02d:%02d",
                        minutes,
                        remainingSeconds
                )
        );

        fuelBar.setProgress(fuel / 100);
        oxygenBar.setProgress(oxygen / 100);
    }

    // --------------------------------------------------
    // UI HELPERS
    // --------------------------------------------------

    private Label createTelemetryLabel(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-text-fill: #8fd3ff;"
        );

        return label;
    }

    private Label createSectionTitle(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );

        return label;
    }

    private void styleButton(Button button) {

        button.setPrefWidth(130);
        button.setPrefHeight(45);

        button.setStyle(
                "-fx-background-color: #1e3a8a;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
