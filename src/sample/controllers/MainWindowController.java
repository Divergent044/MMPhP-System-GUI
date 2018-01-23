package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class MainWindowController {

    @FXML
    private Button exitButton;

    public void actionButtonPressed(ActionEvent actionEvent) throws Exception {
        Object source = actionEvent.getSource();
        if (!(source instanceof Button))
            return;
        closeMainWindow(actionEvent);
    }

    private void closeMainWindow(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }
}
