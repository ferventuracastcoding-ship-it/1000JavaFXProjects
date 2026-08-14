import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class PlanetExplorationGame extends Application {

    // =========================================================
    // GAME STATE
    // =========================================================

    private int energy = 100;
    private int maxEnergy = 100;

    private int oxygen = 100;
    private int maxOxygen = 100;

    private int fuel = 100;
    private int maxFuel = 100;

    private int science = 0;
    private int credits = 1000;

    private int roverLevel = 1;
    private int discoveries = 0;

    private String currentPlanet = "Mars";

    private Random random = new Random();

    private final List<String> discovered =
            new ArrayList<>();

    private final Map<String, Integer> samples =
            new HashMap<>();

    private ComboBox<String> planetSelector;

    private Label energyLabel;
    private Label oxygenLabel;
    private Label fuelLabel;
    private Label scienceLabel;
    private Label creditLabel;
    private Label roverLabel;
    private Label planetLabel;

    private TextArea logArea;

    private VBox regionBox;

    // =========================================================
    // START
    // =========================================================

    @Override
    public void start(Stage stage) {

        createSampleInventory();

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #020617;"
        );

        // =====================================================
        // HEADER
        // =====================================================

        Label title =
                new Label(
                        "🪐 PLANET EXPLORATION COMMAND"
                );

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #7dd3fc;"
        );

        HBox stats =
                new HBox(25);

        stats.setAlignment(
                Pos.CENTER
        );

        energyLabel =
                createStat("⚡ Energy: 100");

        oxygenLabel =
                createStat("🫁 Oxygen: 100");

        fuelLabel =
                createStat("⛽ Fuel: 100");

        scienceLabel =
                createStat("🔬 Science: 0");

        creditLabel =
                createStat("💰 Credits: $1000");

        roverLabel =
                createStat("🤖 Rover Lv.1");

        planetLabel =
                createStat("🔴 Mars");

        stats.getChildren().addAll(
                energyLabel,
                oxygenLabel,
                fuelLabel,
                scienceLabel,
                creditLabel,
                roverLabel,
                planetLabel
        );

        VBox header =
                new VBox(
                        12,
                        title,
                        stats
                );

        header.setAlignment(
                Pos.CENTER
        );

        header.setPadding(
                new Insets(20)
        );

        header.setStyle(
                "-fx-background-color: #0f172a;" +
                "-fx-border-color: #334155;"
        );

        root.setTop(header);

        // =====================================================
        // LEFT CONTROL PANEL
        // =====================================================

        VBox controls =
                new VBox(12);

        controls.setPadding(
                new Insets(18)
        );

        controls.setPrefWidth(
                250
        );

        controls.setStyle(
                "-fx-background-color: #0b1120;"
        );

        Label missionTitle =
                createHeading(
                        "🚀 MISSION CONTROL"
                );

        planetSelector =
                new ComboBox<>();

        planetSelector.getItems().addAll(
                "Mars",
                "Moon",
                "Europa",
                "Titan",
                "Venus",
                "Mercury"
        );

        planetSelector.setValue(
                "Mars"
        );

        Button landButton =
                createButton(
                        "🛬 Land on Planet",
                        "#0284c7"
                );

        Button rechargeButton =
                createButton(
                        "⚡ Recharge Rover",
                        "#16a34a"
                );

        Button refuelButton =
                createButton(
                        "⛽ Refuel",
                        "#2563eb"
                );

        Button oxygenButton =
                createButton(
                        "🫁 Refill Oxygen",
                        "#0891b2"
                );

        Button upgradeButton =
                createButton(
                        "🔧 Upgrade Rover",
                        "#9333ea"
                );

        Button scanButton =
                createButton(
                        "📡 Planetary Scan",
                        "#ca8a04"
                );

        Button resetButton =
                createButton(
                        "🔄 New Mission",
                        "#dc2626"
                );

        landButton.setOnAction(
                e -> landOnPlanet()
        );

        rechargeButton.setOnAction(
                e -> recharge()
        );

        refuelButton.setOnAction(
                e -> refuel()
        );

        oxygenButton.setOnAction(
                e -> refillOxygen()
        );

        upgradeButton.setOnAction(
                e -> upgradeRover()
        );

        scanButton.setOnAction(
                e -> scanPlanet()
        );

        resetButton.setOnAction(
                e -> resetGame()
        );

        controls.getChildren().addAll(
                missionTitle,
                new Label("Destination:"),
                planetSelector,
                landButton,
                scanButton,
                rechargeButton,
                refuelButton,
                oxygenButton,
                upgradeButton,
                new Separator(),
                resetButton
        );

        root.setLeft(
                controls
        );

        // =====================================================
        // CENTER EXPLORATION AREA
        // =====================================================

        VBox center =
                new VBox(15);

        center.setPadding(
                new Insets(20)
        );

        Label explorationTitle =
                createHeading(
                        "🌌 EXPLORATION ZONE"
                );

        Label instruction =
                new Label(
                        "Select a region to explore."
                );

        instruction.setStyle(
                "-fx-text-fill: #cbd5e1;" +
                "-fx-font-size: 15px;"
        );

        regionBox =
                new VBox(10);

        ScrollPane scroll =
                new ScrollPane(
                        regionBox
                );

        scroll.setFitToWidth(
                true
        );

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        center.getChildren().addAll(
                explorationTitle,
                instruction,
                scroll
        );

        root.setCenter(
                center
        );

        // =====================================================
        // RIGHT PANEL
        // =====================================================

        VBox right =
                new VBox(12);

        right.setPadding(
                new Insets(18)
        );

        right.setPrefWidth(
                300
        );

        right.setStyle(
                "-fx-background-color: #0b1120;"
        );

        Label discoveriesTitle =
                createHeading(
                        "🔬 DISCOVERIES"
                );

        logArea =
                new TextArea();

        logArea.setEditable(
                false
        );

        logArea.setWrapText(
                true
        );

        logArea.setPrefHeight(
                300
        );

        logArea.setStyle(
                "-fx-control-inner-background: #020617;" +
                "-fx-text-fill: #cbd5e1;"
        );

        Label samplesTitle =
                createHeading(
                        "🧪 SAMPLES"
                );

        right.getChildren().addAll(
                discoveriesTitle,
                logArea,
                samplesTitle
        );

        for (
                String sample :
                samples.keySet()
        ) {

            Label label =
                    new Label();

            label.setId(
                    "sample-" +
                    sample.replace(
                            " ",
                            "-"
                    )
            );

            label.setStyle(
                    "-fx-text-fill: #cbd5e1;" +
                    "-fx-padding: 5px;"
            );

            right.getChildren().add(
                    label
            );
        }

        root.setRight(
                right
        );

        // =====================================================
        // SCENE
        // =====================================================

        Scene scene =
                new Scene(
                        root,
                        1400,
                        800
                );

        stage.setTitle(
                "Planet Exploration Game"
        );

        stage.setScene(
                scene
        );

        stage.show();

        updateUI();

        addLog(
                "🚀 Planetary exploration mission initialized."
        );

        addLog(
                "🔴 Current destination: Mars."
        );

        generateRegions();
    }

    // =========================================================
    // SAMPLE INVENTORY
    // =========================================================

    private void createSampleInventory() {

        samples.put(
                "Rock Samples",
                0
        );

        samples.put(
                "Ice Samples",
                0
        );

        samples.put(
                "Minerals",
                0
        );

        samples.put(
                "Organic Material",
                0
        );

        samples.put(
                "Unknown Objects",
                0
        );
    }

    // =========================================================
    // PLANET LANDING
    // =========================================================

    private void landOnPlanet() {

        String destination =
                planetSelector.getValue();

        if (
                destination.equals(
                        currentPlanet
                )
        ) {

            addLog(
                    "🪐 You are already exploring " +
                    destination +
                    "."
            );

            return;
        }

        int cost =
                getLandingFuelCost(
                        destination
                );

        if (
                fuel < cost
        ) {

            addLog(
                    "❌ Not enough fuel to reach " +
                    destination +
                    "."
            );

            return;
        }

        fuel -= cost;

        currentPlanet =
                destination;

        energy =
                maxEnergy;

        oxygen =
                maxOxygen;

        generateRegions();

        updateUI();

        addLog(
                "🛬 Successfully landed on " +
                getPlanetEmoji(
                        destination
                ) +
                " " +
                destination +
                "."
        );
    }

    // =========================================================
    // LANDING COST
    // =========================================================

    private int getLandingFuelCost(
            String planet
    ) {

        switch (planet) {

            case "Mars":
                return 10;

            case "Moon":
                return 8;

            case "Europa":
                return 20;

            case "Titan":
                return 25;

            case "Venus":
                return 30;

            case "Mercury":
                return 35;

            default:
                return 10;
        }
    }

    // =========================================================
    // GENERATE REGIONS
    // =========================================================

    private void generateRegions() {

        regionBox.getChildren()
                .clear();

        List<String> regions =
                getRegions(
                        currentPlanet
                );

        for (
                String region :
                regions
        ) {

            HBox card =
                    new HBox(15);

            card.setAlignment(
                    Pos.CENTER_LEFT
            );

            card.setPadding(
                    new Insets(15)
            );

            card.setStyle(
                    "-fx-background-color: #111827;" +
                    "-fx-border-color: #334155;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;"
            );

            VBox info =
                    new VBox(5);

            Label name =
                    new Label(
                            getRegionEmoji(
                                    region
                            ) +
                            " " +
                            region
                    );

            name.setStyle(
                    "-fx-text-fill: #7dd3fc;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;"
            );

            Label description =
                    new Label(
                            getRegionDescription(
                                    region
                            )
                    );

            description.setWrapText(
                    true
            );

            description.setStyle(
                    "-fx-text-fill: #94a3b8;"
            );

            info.getChildren().addAll(
                    name,
                    description
            );

            HBox.setHgrow(
                    info,
                    Priority.ALWAYS
            );

            Button explore =
                    createButton(
                            "🔎 Explore",
                            "#0f766e"
                    );

            explore.setMaxWidth(
                    130
            );

            explore.setOnAction(
                    e -> exploreRegion(
                            region
                    )
            );

            card.getChildren().addAll(
                    info,
                    explore
            );

            regionBox.getChildren()
                    .add(card);
        }
    }

    // =========================================================
    // REGIONS
    // =========================================================

    private List<String> getRegions(
            String planet
    ) {

        switch (planet) {

            case "Mars":
                return Arrays.asList(
                        "Olympus Mons",
                        "Valles Marineris",
                        "Hellas Basin",
                        "Gale Crater",
                        "Polar Ice Cap"
                );

            case "Moon":
                return Arrays.asList(
                        "Tycho Crater",
                        "Sea of Tranquility",
                        "Copernicus Crater",
                        "South Pole",
                        "Lunar Highlands"
                );

            case "Europa":
                return Arrays.asList(
                        "Ice Plains",
                        "Chaos Terrain",
                        "Deep Ice Fractures",
                        "Subsurface Ocean",
                        "Europa Ridge"
                );

            case "Titan":
                return Arrays.asList(
                        "Methane Lakes",
                        "Dune Fields",
                        "Ice Mountains",
                        "Kraken Mare",
                        "Organic Plains"
                );

            case "Venus":
                return Arrays.asList(
                        "Volcanic Plateau",
                        "Cloud Zone",
                        "Lava Field",
                        "Maxwell Mountains",
                        "Highland Region"
                );

            case "Mercury":
                return Arrays.asList(
                        "Caloris Basin",
                        "Crater Fields",
                        "Iron Plains",
                        "Scarlet Cliffs",
                        "Terminator Zone"
                );

            default:
                return Arrays.asList(
                        "Unknown Region"
                );
        }
    }

    // =========================================================
    // EXPLORE REGION
    // =========================================================

    private void exploreRegion(
            String region
    ) {

        if (
                energy < 15
        ) {

            addLog(
                    "⚡ Not enough rover energy."
            );

            return;
        }

        if (
                oxygen < 10
        ) {

            addLog(
                    "🫁 Oxygen too low. Return to base."
            );

            return;
        }

        energy -= 15;

        oxygen -= 10;

        int discoveryChance =
                random.nextInt(100);

        if (
                discoveryChance < 15
        ) {

            ancientDiscovery(
                    region
            );

        } else if (
                discoveryChance < 35
        ) {

            resourceDiscovery(
                    region
            );

        } else if (
                discoveryChance < 50
        ) {

            hazardEncounter(
                    region
            );

        } else {

            scientificStudy(
                    region
            );
        }

        updateUI();
    }

    // =========================================================
    // RESOURCE DISCOVERY
    // =========================================================

    private void resourceDiscovery(
            String region
    ) {

        String[] resourceTypes = {
                "Rock Samples",
                "Minerals",
                "Ice Samples",
                "Organic Material"
        };

        String sample =
                resourceTypes[
                        random.nextInt(
                                resourceTypes.length
                        )
                ];

        int amount =
                1 +
                random.nextInt(4);

        samples.put(
                sample,
                samples.get(sample)
                        + amount
        );

        science +=
                amount * 5;

        credits +=
                amount * 25;

        discoveries++;

        addLog(
                "🔬 " +
                region +
                ": discovered " +
                amount +
                " " +
                sample +
                "."
        );

        addLog(
                "💰 Scientific value: +" +
                (amount * 25) +
                " credits."
        );
    }

    // =========================================================
    // ANCIENT DISCOVERY
    // =========================================================

    private void ancientDiscovery(
            String region
    ) {

        samples.put(
                "Unknown Objects",
                samples.get(
                        "Unknown Objects"
                ) + 1
        );

        science += 100;

        credits += 500;

        discoveries++;

        addLog(
                "👽 MAJOR DISCOVERY!"
        );

        addLog(
                "📡 An unknown object was discovered at " +
                region +
                "."
        );

        addLog(
                "🔬 Science +100"
        );

        addLog(
                "💰 Credits +500"
        );
    }

    // =========================================================
    // SCIENTIFIC STUDY
    // =========================================================

    private void scientificStudy(
            String region
    ) {

        int points =
                10 +
                random.nextInt(20);

        science += points;

        discoveries++;

        addLog(
                "🔬 Scientific survey completed at " +
                region +
                "."
        );

        addLog(
                "📊 Science +" +
                points
        );
    }

    // =========================================================
    // HAZARD
    // =========================================================

    private void hazardEncounter(
            String region
    ) {

        int damage =
                5 +
                random.nextInt(20);

        energy =
                Math.max(
                        0,
                        energy - damage
                );

        fuel =
                Math.max(
                        0,
                        fuel - 3
                );

        addLog(
                "⚠️ Hazard encountered at " +
                region +
                "!"
        );

        addLog(
                "🤖 Rover energy -" +
                damage
        );
    }

    // =========================================================
    // PLANETARY SCAN
    // =========================================================

    private void scanPlanet() {

        if (
                energy < 20
        ) {

            addLog(
                    "⚡ Not enough energy for a planetary scan."
            );

            return;
        }

        energy -= 20;

        int scanPoints =
                25 +
                random.nextInt(50);

        science += scanPoints;

        addLog(
                "📡 Full planetary scan completed."
        );

        addLog(
                "🔬 Science +" +
                scanPoints
        );

        updateUI();
    }

    // =========================================================
    // RECHARGE
    // =========================================================

    private void recharge() {

        int cost = 100;

        if (
                credits < cost
        ) {

            addLog(
                    "❌ Not enough credits."
            );

            return;
        }

        credits -= cost;

        energy =
                maxEnergy;

        addLog(
                "⚡ Rover fully recharged."
        );

        updateUI();
    }

    // =========================================================
    // REFUEL
    // =========================================================

    private void refuel() {

        int cost = 150;

        if (
                credits < cost
        ) {

            addLog(
                    "❌ Refueling costs 150 credits."
            );

            return;
        }

        credits -= cost;

        fuel =
                maxFuel;

        addLog(
                "⛽ Fuel tank filled."
        );

        updateUI();
    }

    // =========================================================
    // OXYGEN
    // =========================================================

    private void refillOxygen() {

        int cost = 100;

        if (
                credits < cost
        ) {

            addLog(
                    "❌ Oxygen refill costs 100 credits."
            );

            return;
        }

        credits -= cost;

        oxygen =
                maxOxygen;

        addLog(
                "🫁 Oxygen tanks refilled."
        );

        updateUI();
    }

    // =========================================================
    // UPGRADE ROVER
    // =========================================================

    private void upgradeRover() {

        int cost =
                roverLevel * 750;

        if (
                credits < cost
        ) {

            addLog(
                    "❌ Rover upgrade costs " +
                    cost +
                    " credits."
            );

            return;
        }

        credits -= cost;

        roverLevel++;

        maxEnergy += 20;

        maxOxygen += 15;

        maxFuel += 15;

        energy =
                maxEnergy;

        oxygen =
                maxOxygen;

        fuel =
                maxFuel;

        addLog(
                "🔧 Rover upgraded to Level " +
                roverLevel +
                "!"
        );

        addLog(
                "⚡ Energy capacity increased."
        );

        addLog(
                "🫁 Oxygen capacity increased."
        );

        addLog(
                "⛽ Fuel capacity increased."
        );

        updateUI();
    }

    // =========================================================
    // UI UPDATE
    // =========================================================

    private void updateUI() {

        energyLabel.setText(
                "⚡ Energy: " +
                energy +
                "/" +
                maxEnergy
        );

        oxygenLabel.setText(
                "🫁 Oxygen: " +
                oxygen +
                "/" +
                maxOxygen
        );

        fuelLabel.setText(
                "⛽ Fuel: " +
                fuel +
                "/" +
                maxFuel
        );

        scienceLabel.setText(
                "🔬 Science: " +
                science
        );

        creditLabel.setText(
                "💰 Credits: $" +
                credits
        );

        roverLabel.setText(
                "🤖 Rover Lv." +
                roverLevel
        );

        planetLabel.setText(
                getPlanetEmoji(
                        currentPlanet
                ) +
                " " +
                currentPlanet
        );

        updateSampleLabels();
    }

    // =========================================================
    // SAMPLE LABELS
    // =========================================================

    private void updateSampleLabels() {

        for (
                String sample :
                samples.keySet()
        ) {

            String id =
                    "sample-" +
                    sample.replace(
                            " ",
                            "-"
                    );

            Label label =
                    (Label)
                    findNode(
                            id
                    );

            if (
                    label != null
            ) {

                label.setText(
                        sample +
                        ": " +
                        samples.get(sample)
                );
            }
        }
    }

    // =========================================================
    // FIND NODE
    // =========================================================

    private javafx.scene.Node findNode(
            String id
    ) {

        // Search through scene graph.
        // The sample labels are refreshed
        // using the root scene.

        if (
                logArea == null ||
                logArea.getScene() == null
        ) {
            return null;
        }

        return logArea
                .getScene()
                .lookup("#" + id);
    }

    // =========================================================
    // LOG
    // =========================================================

    private void addLog(
            String message
    ) {

        if (
                logArea == null
        )
            return;

        logArea.appendText(
                message +
                "\n"
        );
    }

    // =========================================================
    // PLANET EMOJI
    // =========================================================

    private String getPlanetEmoji(
            String planet
    ) {

        switch (planet) {

            case "Mars":
                return "🔴";

            case "Moon":
                return "🌕";

            case "Europa":
                return "🔵";

            case "Titan":
                return "🟠";

            case "Venus":
                return "🟡";

            case "Mercury":
                return "⚪";

            default:
                return "🪐";
        }
    }

    // =========================================================
    // REGION EMOJI
    // =========================================================

    private String getRegionEmoji(
            String region
    ) {

        if (
                region.contains("Ice") ||
                region.contains("Ocean")
        ) {
            return "🧊";
        }

        if (
                region.contains("Crater") ||
                region.contains("Basin")
        ) {
            return "🌋";
        }

        if (
                region.contains("Mountain") ||
                region.contains("Mons")
        ) {
            return "⛰️";
        }

        if (
                region.contains("Lake") ||
                region.contains("Sea")
        ) {
            return "🌊";
        }

        return "🗺️";
    }

    // =========================================================
    // REGION DESCRIPTION
    // =========================================================

    private String getRegionDescription(
            String region
    ) {

        return
                "Explore " +
                region +
                " for scientific discoveries, " +
                "resources, geological samples, " +
                "and unknown phenomena.";
    }

    // =========================================================
    // RESET
    // =========================================================

    private void resetGame() {

        energy = 100;

        maxEnergy = 100;

        oxygen = 100;

        maxOxygen = 100;

        fuel = 100;

        maxFuel = 100;

        science = 0;

        credits = 1000;

        roverLevel = 1;

        discoveries = 0;

        currentPlanet = "Mars";

        planetSelector.setValue(
                "Mars"
        );

        for (
                String sample :
                samples.keySet()
        ) {

            samples.put(
                    sample,
                    0
            );
        }

        logArea.clear();

        generateRegions();

        updateUI();

        addLog(
                "🔄 New planetary exploration mission started."
        );
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private Label createStat(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-text-fill: #bae6fd;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;"
        );

        return label;
    }

    private Label createHeading(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-text-fill: #7dd3fc;" +
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    private Button createButton(
            String text,
            String color
    ) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setStyle(
                "-fx-background-color: " +
                color +
                ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10px;"
        );

        return button;
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
