import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AircraftMaintenanceTracker extends Application {

    private final ObservableList<Aircraft> aircraftList =
            FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {

        // =========================
        // TITLE
        // =========================

        Label title = new Label("✈ Aircraft Maintenance Tracker");
        title.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;"
        );

        // =========================
        // INPUT FIELDS
        // =========================

        TextField tailField = new TextField();
        tailField.setPromptText("N123AB");

        TextField modelField = new TextField();
        modelField.setPromptText("Boeing 737");

        TextField hoursField = new TextField();
        hoursField.setPromptText("Flight hours");

        DatePicker maintenanceDate =
                new DatePicker(LocalDate.now());

        DatePicker inspectionDate =
                new DatePicker(LocalDate.now().plusMonths(3));

        ComboBox<String> statusBox = new ComboBox<>();

        statusBox.getItems().addAll(
                "Operational",
                "Maintenance Due",
                "Grounded",
                "Inspection Required"
        );

        statusBox.setValue("Operational");

        // =========================
        // BUTTONS
        // =========================

        Button addButton = new Button("Add Aircraft");
        Button deleteButton = new Button("Delete Selected");
        Button clearButton = new Button("Clear");

        // =========================
        // TABLE
        // =========================

        TableView<Aircraft> table = new TableView<>();

        TableColumn<Aircraft, String> tailColumn =
                new TableColumn<>("Tail Number");

        tailColumn.setCellValueFactory(
                data -> data.getValue().tailNumberProperty()
        );

        TableColumn<Aircraft, String> modelColumn =
                new TableColumn<>("Aircraft Model");

        modelColumn.setCellValueFactory(
                data -> data.getValue().modelProperty()
        );

        TableColumn<Aircraft, Number> hoursColumn =
                new TableColumn<>("Flight Hours");

        hoursColumn.setCellValueFactory(
                data -> data.getValue().flightHoursProperty()
        );

        TableColumn<Aircraft, String> statusColumn =
                new TableColumn<>("Status");

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        TableColumn<Aircraft, String> lastMaintenanceColumn =
                new TableColumn<>("Last Maintenance");

        lastMaintenanceColumn.setCellValueFactory(
                data -> data.getValue().lastMaintenanceProperty()
        );

        TableColumn<Aircraft, String> inspectionColumn =
                new TableColumn<>("Next Inspection");

        inspectionColumn.setCellValueFactory(
                data -> data.getValue().nextInspectionProperty()
        );

        table.getColumns().addAll(
                tailColumn,
                modelColumn,
                hoursColumn,
                statusColumn,
                lastMaintenanceColumn,
                inspectionColumn
        );

        table.setItems(aircraftList);

        // =========================
        // ADD AIRCRAFT
        // =========================

        addButton.setOnAction(event -> {

            try {

                String tail = tailField.getText();
                String model = modelField.getText();

                if (tail.isEmpty() || model.isEmpty()) {
                    showAlert(
                            "Missing Information",
                            "Enter the tail number and aircraft model."
                    );
                    return;
                }

                double hours =
                        Double.parseDouble(hoursField.getText());

                Aircraft aircraft = new Aircraft(
                        tail,
                        model,
                        hours,
                        statusBox.getValue(),
                        maintenanceDate.getValue(),
                        inspectionDate.getValue()
                );

                aircraftList.add(aircraft);

                clearFields(
                        tailField,
                        modelField,
                        hoursField,
                        statusBox
                );

            } catch (NumberFormatException e) {

                showAlert(
                        "Invalid Flight Hours",
                        "Flight hours must be a number."
                );
            }
        });

        // =========================
        // DELETE AIRCRAFT
        // =========================

        deleteButton.setOnAction(event -> {

            Aircraft selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected != null) {
                aircraftList.remove(selected);
            } else {
                showAlert(
                        "No Aircraft Selected",
                        "Select an aircraft to delete."
                );
            }
        });

        // =========================
        // CLEAR
        // =========================

        clearButton.setOnAction(event -> {

            clearFields(
                    tailField,
                    modelField,
                    hoursField,
                    statusBox
            );

            maintenanceDate.setValue(LocalDate.now());
            inspectionDate.setValue(
                    LocalDate.now().plusMonths(3)
            );
        });

        // =========================
        // FORM
        // =========================

        GridPane form = new GridPane();

        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        form.add(new Label("Tail Number:"), 0, 0);
        form.add(tailField, 1, 0);

        form.add(new Label("Model:"), 0, 1);
        form.add(modelField, 1, 1);

        form.add(new Label("Flight Hours:"), 0, 2);
        form.add(hoursField, 1, 2);

        form.add(new Label("Status:"), 0, 3);
        form.add(statusBox, 1, 3);

        form.add(new Label("Last Maintenance:"), 2, 0);
        form.add(maintenanceDate, 3, 0);

        form.add(new Label("Next Inspection:"), 2, 1);
        form.add(inspectionDate, 3, 1);

        HBox buttons = new HBox(
                10,
                addButton,
                deleteButton,
                clearButton
        );

        VBox root = new VBox(
                15,
                title,
                form,
                buttons,
                table
        );

        root.setPadding(new Insets(20));

        VBox.setVgrow(table, Priority.ALWAYS);

        // =========================
        // SCENE
        // =========================

        Scene scene = new Scene(
                root,
                1100,
                650
        );

        stage.setTitle("Aircraft Maintenance Tracker");
        stage.setScene(scene);
        stage.show();
    }

    private void clearFields(
            TextField tail,
            TextField model,
            TextField hours,
            ComboBox<String> status
    ) {

        tail.clear();
        model.clear();
        hours.clear();

        status.setValue("Operational");
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // ==========================================
    // AIRCRAFT CLASS
    // ==========================================

    public static class Aircraft {

        private final StringProperty tailNumber;
        private final StringProperty model;
        private final DoubleProperty flightHours;
        private final StringProperty status;
        private final StringProperty lastMaintenance;
        private final StringProperty nextInspection;

        public Aircraft(
                String tailNumber,
                String model,
                double flightHours,
                String status,
                LocalDate lastMaintenance,
                LocalDate nextInspection
        ) {

            this.tailNumber =
                    new SimpleStringProperty(tailNumber);

            this.model =
                    new SimpleStringProperty(model);

            this.flightHours =
                    new SimpleDoubleProperty(flightHours);

            this.status =
                    new SimpleStringProperty(status);

            this.lastMaintenance =
                    new SimpleStringProperty(
                            lastMaintenance.toString()
                    );

            this.nextInspection =
                    new SimpleStringProperty(
                            nextInspection.toString()
                    );
        }

        public StringProperty tailNumberProperty() {
            return tailNumber;
        }

        public StringProperty modelProperty() {
            return model;
        }

        public DoubleProperty flightHoursProperty() {
            return flightHours;
        }

        public StringProperty statusProperty() {
            return status;
        }

        public StringProperty lastMaintenanceProperty() {
            return lastMaintenance;
        }

        public StringProperty nextInspectionProperty() {
            return nextInspection;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
