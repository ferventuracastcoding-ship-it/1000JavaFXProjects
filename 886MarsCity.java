import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MarsColonyCommand extends Application {

    Circle rocket;
    Label altitude;
    Label fuel;
    Label robots;
    Label oxygen;
    Label status;

    double height = 450;
    double fuelValue = 100;


    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();


        // Mars sky

        Pane mars = new Pane();

        mars.setStyle(
            "-fx-background-color:#b85c38;"
        );


        // Mars ground

        Rectangle ground =
                new Rectangle(
                900,
                120,
                Color.DARKRED
        );

        ground.setY(480);


        // Rocket

        rocket =
            new Circle(
            25,
            Color.LIGHTGRAY
        );

        rocket.setCenterX(450);
        rocket.setCenterY(height);



        // Mars colony dome

        Circle dome =
            new Circle(
            100,
            Color.rgb(180,220,255,0.5)
        );

        dome.setCenterX(200);
        dome.setCenterY(450);



        // Robot

        Rectangle robot =
            new Rectangle(
            40,
            40,
            Color.GRAY
        );

        robot.setX(700);
        robot.setY(440);



        mars.getChildren()
            .addAll(
            ground,
            dome,
            robot,
            rocket
        );



        // Dashboard


        altitude =
            new Label();

        fuel =
            new Label();

        robots =
            new Label(
            "Robots Online: 250"
            );

        oxygen =
            new Label(
            "Oxygen: 98%"
            );

        status =
            new Label(
            "Mission: Ready"
            );


        VBox panel =
            new VBox(
            15,
            new Label(
            "🔴 MARS COLONY CONTROL"
            ),
            status,
            altitude,
            fuel,
            oxygen,
            robots
        );


        panel.setStyle(
            "-fx-padding:20;"
        );


        Button land =
            new Button(
            "🚀 Land Rocket"
            );


        land.setOnAction(e ->
            startLanding()
        );


        panel.getChildren()
             .add(land);



        root.setCenter(mars);
        root.setRight(panel);



        Scene scene =
            new Scene(
            root,
            1100,
            600
        );


        stage.setScene(scene);

        stage.setTitle(
            "Mars Colony Command"
        );

        stage.show();


        update();

    }



    void startLanding(){

        status.setText(
            "Mission: Rocket Descending"
        );


        Timeline animation =
            new Timeline(

            new KeyFrame(
            Duration.millis(100),

            e -> {

                height -= 5;
                fuelValue -= .3;


                rocket.setCenterY(
                    height
                );


                update();


                if(height <= 450){

                    status.setText(
                    "Mission: Landing Complete ✓"
                    );

                }

            })

        );


        animation.setCycleCount(90);

        animation.play();

    }



    void update(){

        altitude.setText(
        "Altitude: "
        +(int)height
        +" meters"
        );


        fuel.setText(
        "Rocket Fuel: "
        +(int)fuelValue
        +"%"
        );

    }



    public static void main(String[] args){

        launch();

    }
}
