import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarsColonyPowerGrid extends Application {

    private Canvas canvas;
    private GraphicsContext gc;

    private boolean running = false;
    private boolean paused = false;

    // =========================================================
    // GRID STATE
    // =========================================================

    private double solarOutput = 0;
    private double nuclearOutput = 500;
    private double batteryPower = 0;
    private double batteryEnergy = 6000;

    private double totalGeneration = 0;
    private double totalDemand = 0;
    private double gridLoad = 0;

    private double gridFrequency = 60;
    private double colonyVoltage = 480;

    private double batteryCapacity = 10000;

    private double solarEfficiency = 85;

    private boolean solarOnline = true;
    private boolean nuclearOnline = true;
    private boolean batteryOnline = true;

    private boolean overload = false;
    private boolean blackout = false;

    private double simulationTime = 0;

    // =========================================================
    // LOADS
    // =========================================================

    private double habitatDemand = 350;
    private double lifeSupportDemand = 250;
    private double miningDemand = 600;
    private double factoryDemand = 450;
    private double waterDemand = 300;
    private double greenhouseDemand = 180;

    // =========================================================
    // UI
    // =========================================================

    private Label generationLabel;
    private Label demandLabel;
    private Label batteryLabel;
    private Label loadLabel;
    private Label frequencyLabel;
    private Label voltageLabel;
    private Label statusLabel;

    private ProgressBar batteryBar;
    private ProgressBar loadBar;

    private Slider solarSlider;
    private Slider miningSlider;
    private Slider factorySlider;

    // =========================================================
    // NODES
    // =========================================================

    private final List<GridNode> nodes =
            new ArrayList<>();

    private final List<GridLine> lines =
            new ArrayList<>();

    private final Random random =
            new Random(7);

    // =========================================================
    // START
    // =========================================================

    @Override
    public void start(Stage stage) {

        canvas = new Canvas(
                950,
                700
        );

        gc = canvas.getGraphicsContext2D();

        createGrid();

        BorderPane root =
                new BorderPane();

        root.setTop(
                createHeader()
        );

        root.setCenter(
                canvas
        );

        root.setRight(
                createControlPanel()
        );

        root.setBottom(
                createStatusBar()
        );

        Scene scene =
                new Scene(
                        root,
                        1350,
                        820
                );

        stage.setTitle(
                "Mars Colony Power Grid Simulator"
        );

        stage.setScene(scene);

        stage.show();

        startLoop();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader() {

        Label title =
                new Label(
                        "🔴 MARS COLONY POWER GRID"
                );

        title.setTextFill(
                Color.WHITE
        );

        title.setFont(
                Font.font(
                        "Arial",
                        25
                )
        );

        Label subtitle =
                new Label(
                        "Power Generation • Storage • Distribution • Colony Loads"
                );

        subtitle.setTextFill(
                Color.LIGHTGRAY
        );

        VBox box =
                new VBox(
                        3,
                        title,
                        subtitle
                );

        box.setPadding(
                new Insets(12)
        );

        HBox header =
                new HBox(box);

        header.setStyle(
                "-fx-background-color:#11151b;"
        );

        return header;
    }

    // =========================================================
    // CONTROL PANEL
    // =========================================================

    private VBox createControlPanel() {

        VBox panel =
                new VBox(10);

        panel.setPadding(
                new Insets(14)
        );

        panel.setPrefWidth(
                370
        );

        panel.setStyle(
                "-fx-background-color:#1a1e25;"
        );

        Label controlTitle =
                sectionTitle(
                        "GRID CONTROL"
                );

        Button start =
                new Button(
                        "▶ Start Grid"
                );

        Button pause =
                new Button(
                        "⏸ Pause"
                );

        Button reset =
                new Button(
                        "↻ Reset"
                );

        Button solar =
                new Button(
                        "☀ Toggle Solar"
                );

        Button nuclear =
                new Button(
                        "⚛ Toggle Nuclear"
                );

        Button battery =
                new Button(
                        "🔋 Toggle Battery"
                );

        for(Button button :
                new Button[]{
                        start,
                        pause,
                        reset,
                        solar,
                        nuclear,
                        battery
                }) {

            button.setMaxWidth(
                    Double.MAX_VALUE
            );
        }

        start.setOnAction(
                e -> {

                    running = true;
                    paused = false;

                    setStatus(
                            "GRID ONLINE"
                    );
                }
        );

        pause.setOnAction(
                e -> {

                    paused =
                            !paused;

                    setStatus(
                            paused
                            ? "GRID PAUSED"
                            : "GRID RESUMED"
                    );
                }
        );

        reset.setOnAction(
                e -> resetGrid()
        );

        solar.setOnAction(
                e -> {

                    solarOnline =
                            !solarOnline;

                    setStatus(
                            solarOnline
                            ? "SOLAR FARM ONLINE"
                            : "SOLAR FARM OFFLINE"
                    );
                }
        );

        nuclear.setOnAction(
                e -> {

                    nuclearOnline =
                            !nuclearOnline;

                    setStatus(
                            nuclearOnline
                            ? "NUCLEAR REACTOR ONLINE"
                            : "NUCLEAR REACTOR OFFLINE"
                    );
                }
        );

        battery.setOnAction(
                e -> {

                    batteryOnline =
                            !batteryOnline;

                    setStatus(
                            batteryOnline
                            ? "BATTERY STORAGE ONLINE"
                            : "BATTERY STORAGE OFFLINE"
                    );
                }
        );

        Label solarTitle =
                sectionTitle(
                        "SOLAR FARM OUTPUT"
                );

        solarSlider =
                new Slider(
                        0,
                        1500,
                        1000
                );

        solarSlider.setShowTickLabels(
                true
        );

        solarSlider.valueProperty()
                .addListener(
                        (obs, oldVal, newVal) ->
                                solarEfficiency =
                                        newVal.doubleValue()
                                        / 1500
                                        * 100
                );

        Label miningTitle =
                sectionTitle(
                        "MINING COMPLEX"
                );

        miningSlider =
                new Slider(
                        0,
                        1000,
                        600
                );

        miningSlider.setShowTickLabels(
                true
        );

        miningSlider.valueProperty()
                .addListener(
                        (obs, oldVal, newVal) ->
                                miningDemand =
                                        newVal.doubleValue()
                );

        Label factoryTitle =
                sectionTitle(
                        "FACTORY COMPLEX"
                );

        factorySlider =
                new Slider(
                        0,
                        1000,
                        450
                );

        factorySlider.setShowTickLabels(
                true
        );

        factorySlider.valueProperty()
                .addListener(
                        (obs, oldVal, newVal) ->
                                factoryDemand =
                                        newVal.doubleValue()
                );

        Label telemetry =
                sectionTitle(
                        "GRID TELEMETRY"
                );

        generationLabel =
                telemetryLabel();

        demandLabel =
                telemetryLabel();

        batteryLabel =
                telemetryLabel();

        loadLabel =
                telemetryLabel();

        frequencyLabel =
                telemetryLabel();

        voltageLabel =
                telemetryLabel();

        batteryBar =
                new ProgressBar(
                        0.6
                );

        batteryBar.setMaxWidth(
                Double.MAX_VALUE
        );

        loadBar =
                new ProgressBar(
                        0
                );

        loadBar.setMaxWidth(
                Double.MAX_VALUE
        );

        panel.getChildren().addAll(

                controlTitle,

                start,
                pause,
                reset,

                solar,
                nuclear,
                battery,

                solarTitle,
                solarSlider,

                miningTitle,
                miningSlider,

                factoryTitle,
                factorySlider,

                telemetry,

                generationLabel,
                demandLabel,
                batteryLabel,
                loadLabel,
                frequencyLabel,
                voltageLabel,

                new Label("Battery Level"),
                batteryBar,

                new Label("Grid Load"),
                loadBar
        );

        return panel;
    }

    // =========================================================
    // STATUS
    // =========================================================

    private HBox createStatusBar() {

        statusLabel =
                new Label(
                        "MARS POWER GRID READY"
                );

        statusLabel.setTextFill(
                Color.LIGHTGREEN
        );

        HBox bar =
                new HBox(
                        statusLabel
                );

        bar.setPadding(
                new Insets(8)
        );

        bar.setStyle(
                "-fx-background-color:#0c1015;"
        );

        return bar;
    }

    // =========================================================
    // GRID CREATION
    // =========================================================

    private void createGrid() {

        nodes.clear();

        lines.clear();

        nodes.add(
                new GridNode(
                        "Solar Farm",
                        100,
                        140,
                        "GENERATOR"
                )
        );

        nodes.add(
                new GridNode(
                        "Nuclear Reactor",
                        100,
                        310,
                        "GENERATOR"
                )
        );

        nodes.add(
                new GridNode(
                        "Battery Station",
                        300,
                        220,
                        "STORAGE"
                )
        );

        nodes.add(
                new GridNode(
                        "Main Substation",
                        480,
                        310,
                        "GRID"
                )
        );

        nodes.add(
                new GridNode(
                        "Habitat",
                        690,
                        120,
                        "LOAD"
                )
        );

        nodes.add(
                new GridNode(
                        "Life Support",
                        690,
                        240,
                        "LOAD"
                )
        );

        nodes.add(
                new GridNode(
                        "Mining",
                        690,
                        370,
                        "LOAD"
                )
        );

        nodes.add(
                new GridNode(
                        "Factory",
                        690,
                        500,
                        "LOAD"
                )
        );

        nodes.add(
                new GridNode(
                        "Water Plant",
                        480,
                        520,
                        "LOAD"
                )
        );

        nodes.add(
                new GridNode(
                        "Greenhouse",
                        280,
                        520,
                        "LOAD"
                )
        );

        connect(
                0,
                2
        );

        connect(
                1,
                2
        );

        connect(
                2,
                3
        );

        connect(
                3,
                4
        );

        connect(
                3,
                5
        );

        connect(
                3,
                6
        );

        connect(
                3,
                7
        );

        connect(
                3,
                8
        );

        connect(
                2,
                9
        );
    }

    private void connect(
            int a,
            int b
    ) {

        lines.add(
                new GridLine(
                        nodes.get(a),
                        nodes.get(b)
                )
        );
    }

    // =========================================================
    // SIMULATION LOOP
    // =========================================================

    private void startLoop() {

        AnimationTimer timer =
                new AnimationTimer() {

            private long previous;

            @Override
            public void handle(long now) {

                if(previous == 0) {

                    previous = now;

                    return;
                }

                double dt =
                        (now - previous)
                        / 1_000_000_000.0;

                previous = now;

                dt =
                        Math.min(
                                dt,
                                .05
                        );

                if(
                        running &&
                        !paused
                ) {

                    updateGrid(dt);
                }

                updateTelemetry();

                draw();
            }
        };

        timer.start();
    }

    // =========================================================
    // GRID SIMULATION
    // =========================================================

    private void updateGrid(
            double dt
    ) {

        simulationTime += dt;

        calculateGeneration();

        calculateDemand();

        balanceGrid(dt);

        calculateGridStability();

        if(
                batteryEnergy <= 0 &&
                totalGeneration < totalDemand
        ) {

            blackout = true;

            setStatus(
                    "⚠ GRID BLACKOUT"
            );
        }

        if(
                blackout &&
                totalGeneration > totalDemand
        ) {

            blackout = false;

            setStatus(
                    "GRID POWER RESTORED"
            );
        }
    }

    // =========================================================
    // GENERATION
    // =========================================================

    private void calculateGeneration() {

        double solarCycle =
                0.55 +
                0.45 *
                Math.sin(
                        simulationTime *
                        0.08
                );

        solarOutput =
                solarOnline
                ? 1500 *
                  solarEfficiency /
                  100 *
                  Math.max(
                          0.1,
                          solarCycle
                  )
                : 0;

        nuclearOutput =
                nuclearOnline
                ? 500
                : 0;

        totalGeneration =
                solarOutput +
                nuclearOutput;
    }

    // =========================================================
    // DEMAND
    // =========================================================

    private void calculateDemand() {

        habitatDemand =
                350;

        lifeSupportDemand =
                250;

        waterDemand =
                300;

        greenhouseDemand =
                180;

        totalDemand =
                habitatDemand +
                lifeSupportDemand +
                miningDemand +
                factoryDemand +
                waterDemand +
                greenhouseDemand;
    }

    // =========================================================
    // BATTERY BALANCING
    // =========================================================

    private void balanceGrid(
            double dt
    ) {

        double surplus =
                totalGeneration -
                totalDemand;

        batteryPower = 0;

        if(
                batteryOnline
        ) {

            if(
                    surplus > 0
            ) {

                double chargingPower =
                        Math.min(
                                surplus,
                                800
                        );

                double availableCapacity =
                        batteryCapacity -
                        batteryEnergy;

                double charge =
                        Math.min(
                                chargingPower *
                                dt,
                                availableCapacity
                        );

                batteryEnergy +=
                        charge;

                batteryPower =
                        -chargingPower;

            } else {

                double required =
                        -surplus;

                double available =
                        Math.min(
                                800,
                                batteryEnergy /
                                Math.max(
                                        dt,
                                        .001
                                )
                        );

                double discharge =
                        Math.min(
                                required,
                                available
                        );

                batteryEnergy -=
                        discharge *
                        dt;

                batteryPower =
                        discharge;
            }
        }

        batteryEnergy =
                Math.max(
                        0,
                        Math.min(
                                batteryCapacity,
                                batteryEnergy
                        )
                );
    }

    // =========================================================
    // GRID STABILITY
    // =========================================================

    private void calculateGridStability() {

        double effectiveGeneration =
                totalGeneration +
                Math.max(
                        0,
                        batteryPower
                );

        gridLoad =
                totalDemand /
                Math.max(
                        effectiveGeneration,
                        1
                ) *
                100;

        overload =
                gridLoad > 100;

        if(overload) {

            gridFrequency =
                    Math.max(
                            45,
                            60 -
                            (gridLoad - 100)
                            * .25
                    );

            colonyVoltage =
                    Math.max(
                            300,
                            480 -
                            (gridLoad - 100)
                            * 1.2
                    );

        } else {

            gridFrequency =
                    60 -
                    Math.max(
                            0,
                            gridLoad - 80
                    ) *
                    .03;

            colonyVoltage =
                    480 -
                    Math.max(
                            0,
                            gridLoad - 90
                    ) *
                    .5;
        }

        if(
                blackout
        ) {

            gridFrequency = 0;

            colonyVoltage = 0;
        }
    }

    // =========================================================
    // TELEMETRY
    // =========================================================

    private void updateTelemetry() {

        generationLabel.setText(
                String.format(
                        "Generation: %.0f kW",
                        totalGeneration
                )
        );

        demandLabel.setText(
                String.format(
                        "Demand: %.0f kW",
                        totalDemand
                )
        );

        batteryLabel.setText(
                String.format(
                        "Battery: %.0f / %.0f kWh",
                        batteryEnergy,
                        batteryCapacity
                )
        );

        loadLabel.setText(
                String.format(
                        "Grid Load: %.1f%%",
                        gridLoad
                )
        );

        frequencyLabel.setText(
                String.format(
                        "Frequency: %.2f Hz",
                        gridFrequency
                )
        );

        voltageLabel.setText(
                String.format(
                        "Voltage: %.0f V",
                        colonyVoltage
                )
        );

        batteryBar.setProgress(
                batteryEnergy /
                batteryCapacity
        );

        loadBar.setProgress(
                Math.min(
                        gridLoad /
                        100,
                        1
                )
        );
    }

    // =========================================================
    // DRAW
    // =========================================================

    private void draw() {

        double width =
                canvas.getWidth();

        double height =
                canvas.getHeight();

        gc.setFill(
                Color.rgb(
                        55,
                        20,
                        15
                )
        );

        gc.fillRect(
                0,
                0,
                width,
                height
        );

        drawMarsSurface();

        drawPowerLines();

        drawNodes();

        drawHUD();
    }

    // =========================================================
    // MARS SURFACE
    // =========================================================

    private void drawMarsSurface() {

        gc.setFill(
                Color.rgb(
                        115,
                        47,
                        34
                )
        );

        gc.fillRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        Random r =
                new Random(42);

        gc.setFill(
                Color.rgb(
                        140,
                        62,
                        42,
                        .5
                )
        );

        for(int i = 0; i < 180; i++) {

            double x =
                    r.nextDouble() *
                    canvas.getWidth();

            double y =
                    r.nextDouble() *
                    canvas.getHeight();

            double size =
                    r.nextDouble() * 20;

            gc.fillOval(
                    x,
                    y,
                    size,
                    size * .5
            );
        }

        // Horizon

        gc.setFill(
                Color.rgb(
                        70,
                        28,
                        22,
                        .5
                )
        );

        gc.fillRect(
                0,
                0,
                canvas.getWidth(),
                65
        );

        gc.setFill(
                Color.rgb(
                        210,
                        100,
                        65
                )
        );

        gc.setFont(
                Font.font(
                        "Arial",
                        16
                )
        );

        gc.fillText(
                "MARS COLONY • POWER DISTRIBUTION NETWORK",
                25,
                35
        );
    }

    // =========================================================
    // POWER LINES
    // =========================================================

    private void drawPowerLines() {

        for(GridLine line :
                lines) {

            boolean active =
                    !blackout;

            gc.setStroke(
                    active
                    ? Color.LIMEGREEN
                    : Color.DARKRED
            );

            gc.setLineWidth(
                    active ? 4 : 2
            );

            gc.strokeLine(
                    line.a.x,
                    line.a.y,
                    line.b.x,
                    line.b.y
            );

            if(active) {

                drawPowerFlow(
                        line.a,
                        line.b
                );
            }
        }
    }

    private void drawPowerFlow(
            GridNode a,
            GridNode b
    ) {

        double progress =
                (simulationTime * .5)
                % 1;

        double x =
                a.x +
                (b.x - a.x) *
                progress;

        double y =
                a.y +
                (b.y - a.y) *
                progress;

        gc.setFill(
                Color.YELLOW
        );

        gc.fillOval(
                x - 4,
                y - 4,
                8,
                8
        );
    }

    // =========================================================
    // NODES
    // =========================================================

    private void drawNodes() {

        for(GridNode node :
                nodes) {

            drawNode(
                    node
            );
        }
    }

    private void drawNode(
            GridNode node
    ) {

        Color color;

        switch(node.type) {

            case "GENERATOR":
                color =
                        node.name.contains(
                                "Solar"
                        )
                        ? Color.GOLD
                        : Color.CYAN;
                break;

            case "STORAGE":
                color =
                        Color.LIMEGREEN;
                break;

            case "GRID":
                color =
                        Color.ORANGE;
                break;

            default:
                color =
                        Color.LIGHTGRAY;
        }

        if(
                blackout
        ) {

            color =
                    Color.DARKRED;
        }

        gc.setFill(
                Color.rgb(
                        0,
                        0,
                        0,
                        .5
                )
        );

        gc.fillOval(
                node.x - 31,
                node.y - 31,
                62,
                62
        );

        gc.setFill(
                color
        );

        gc.fillOval(
                node.x - 24,
                node.y - 24,
                48,
                48
        );

        gc.setStroke(
                Color.WHITE
        );

        gc.setLineWidth(
                2
        );

        gc.strokeOval(
                node.x - 24,
                node.y - 24,
                48,
                48
        );

        gc.setFill(
                Color.WHITE
        );

        gc.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        gc.fillText(
                node.name,
                node.x - 45,
                node.y + 47
        );

        // Power symbols

        gc.setFont(
                Font.font(
                        "Arial",
                        20
                )
        );

        if(
                node.type.equals(
                        "GENERATOR"
                )
        ) {

            gc.fillText(
                    node.name.contains(
                            "Solar"
                    )
                    ? "☀"
                    : "⚛",
                    node.x - 10,
                    node.y + 7
            );

        } else if(
                node.type.equals(
                        "STORAGE"
                )
        ) {

            gc.fillText(
                    "🔋",
                    node.x - 10,
                    node.y + 7
            );

        } else if(
                node.type.equals(
                        "LOAD"
                )
        ) {

            gc.fillText(
                    "⚡",
                    node.x - 10,
                    node.y + 7
            );
        }
    }

    // =========================================================
    // HUD
    // =========================================================

    private void drawHUD() {

        gc.setFill(
                Color.rgb(
                        0,
                        0,
                        0,
                        .72
                )
        );

        gc.fillRoundRect(
                20,
                520,
                350,
                140,
                12,
                12
        );

        gc.setFill(
                Color.WHITE
        );

        gc.setFont(
                Font.font(
                        "monospace",
                        13
                )
        );

        gc.fillText(
                "POWER GENERATION",
                40,
                548
        );

        gc.fillText(
                String.format(
                        "SOLAR       %6.0f kW",
                        solarOutput
                ),
                40,
                568
        );

        gc.fillText(
                String.format(
                        "NUCLEAR     %6.0f kW",
                        nuclearOutput
                ),
                40,
                588
        );

        gc.fillText(
                String.format(
                        "DEMAND      %6.0f kW",
                        totalDemand
                ),
                40,
                608
        );

        gc.fillText(
                String.format(
                        "BATTERY     %6.0f kWh",
                        batteryEnergy
                ),
                40,
                628
        );

        gc.fillText(
                String.format(
                        "FREQUENCY   %6.2f Hz",
                        gridFrequency
                ),
                40,
                648
        );

        if(
                overload
        ) {

            gc.setFill(
                    Color.RED
            );

            gc.setFont(
                    Font.font(
                            "Arial",
                            18
                    )
            );

            gc.fillText(
                    "⚠ GRID OVERLOAD",
                    600,
                    650
            );
        }
    }

    // =========================================================
    // RESET
    // =========================================================

    private void resetGrid() {

        running = false;
        paused = false;

        solarOnline = true;
        nuclearOnline = true;
        batteryOnline = true;

        solarEfficiency = 85;

        miningDemand = 600;
        factoryDemand = 450;

        batteryEnergy = 6000;

        gridFrequency = 60;
        colonyVoltage = 480;

        overload = false;
        blackout = false;

        simulationTime = 0;

        setStatus(
                "MARS POWER GRID READY"
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Label sectionTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setTextFill(
                Color.ORANGE
        );

        label.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        return label;
    }

    private Label telemetryLabel() {

        Label label =
                new Label();

        label.setTextFill(
                Color.LIGHTGREEN
        );

        label.setFont(
                Font.font(
                        "monospace",
                        13
                )
        );

        return label;
    }

    private void setStatus(
            String text
    ) {

        statusLabel.setText(
                text
        );
    }

    // =========================================================
    // DATA CLASSES
    // =========================================================

    private static class GridNode {

        String name;
        double x;
        double y;
        String type;

        GridNode(
                String name,
                double x,
                double y,
                String type
        ) {

            this.name = name;
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    private static class GridLine {

        GridNode a;
        GridNode b;

        GridLine(
                GridNode a,
                GridNode b
        ) {

            this.a = a;
            this.b = b;
        }
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
