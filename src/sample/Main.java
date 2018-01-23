package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.controllers.AuthWindowController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("fxml/authWindow.fxml"));
        Parent fxmlMain = fxmlLoader.load();
        AuthWindowController authWindowController = fxmlLoader.getController();
        authWindowController.setMainStage(primaryStage);

        primaryStage.setTitle("Каф. 22 - Авторизация");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("img/kaf22.ico")));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(fxmlMain, 400, 400));

        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
