package sample.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.lang.reflect.Method;

public class AuthWindowController {

    @FXML
    private CustomTextField loginField;

    @FXML
    private CustomPasswordField passwordField;

    @FXML
    private CustomTextField surnameField;

    @FXML
    private CustomTextField nameField;

    @FXML
    private CustomTextField groupField;

    @FXML
    private HBox boxGroup;

    @FXML
    private ImageView loginImg;

    @FXML
    private ImageView passwordImg;

    @FXML
    private ToggleSwitch toggleStudent;

    @FXML
    private ToggleSwitch toggleTeacher;

    @FXML
    private Button loginButton;

    @FXML
    private Label surnameLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label errorFirstLabel;

    @FXML
    private Label errorSecondLabel;

    private Stage mainStage;
    private Parent fxmlMain;
    private FXMLLoader fxmlLoader = new FXMLLoader();
    private MainWindowController mainWindowController;
    private Stage mainWindowStage;
    private boolean flagToggleTeacher = false;
    private boolean flagToggleStudent = false;

    @FXML
    private void initialize() {
        setupClearButtonField(loginField, surnameField, nameField, groupField, passwordField);
        initMainWindowFXMLLoader();
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    private void setupClearButtonField (CustomTextField customTextField1, CustomTextField customTextField2, CustomTextField customTextField3, CustomTextField customTextField4, CustomPasswordField customPasswordField) {
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, customTextField1, customTextField1.rightProperty());
            m.invoke(null, customTextField2, customTextField2.rightProperty());
            m.invoke(null, customTextField3, customTextField3.rightProperty());
            m.invoke(null, customTextField4, customTextField4.rightProperty());
            m.invoke(null, customPasswordField, customPasswordField.rightProperty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initMainWindowFXMLLoader() {
        try {
            fxmlLoader.setLocation(getClass().getResource("../fxml/mainWindow.fxml"));
            fxmlMain = fxmlLoader.load();
            mainWindowController = fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionButtonPressed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (!(source instanceof Button))
            return;
        errorFirstLabel.setVisible(false);
        if (flagToggleTeacher) {
            closeAuthWindow(actionEvent);
            showMainWindow();
        }
        if (flagToggleStudent) {
            if (checkSurnameNameGroup()) {
                closeAuthWindow(actionEvent);
                showMainWindow();
            }
        }
        if (!flagToggleStudent && !flagToggleTeacher) {
            errorFirstLabel.setText("Укажите Ваш Статус");
            errorFirstLabel.setVisible(true);
        }
    }

    private void closeAuthWindow(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }

    private void hideLoginPassword() {
        loginImg.setVisible(false);
        loginField.setVisible(false);
        passwordField.setVisible(false);
        passwordImg.setVisible(false);
    }

    private void showStudentIdentify() {
        surnameLabel.setVisible(true);
        surnameField.setVisible(true);
        nameLabel.setVisible(true);
        nameField.setVisible(true);
        boxGroup.setVisible(true);
    }

    private void showMainWindow() {
        if (mainWindowStage == null) {
            mainWindowStage = new Stage();
            mainWindowStage.setTitle("Каф. 22 - ММФПвЯЭУ");
            mainWindowStage.getIcons().add(new Image(AuthWindowController.class.getResourceAsStream("../img/kaf22.ico")));
            mainWindowStage.setMinHeight(400);
            mainWindowStage.setMinWidth(700);
            mainWindowStage.setResizable(true);
            mainWindowStage.setScene(new Scene(fxmlMain));
        }
        mainWindowStage.show();
    }

    private boolean checkLoginPassword() {
        if (loginField.getText().equals("") || passwordField.getText().equals("")) {
            errorFirstLabel.setText("Неверный Логин или Пароль");
            errorFirstLabel.setVisible(true);
        }
        else {
            errorFirstLabel.setVisible(false);
            return true;
        }
        return false;
    }

    private boolean checkSurnameNameGroup() {
        if (surnameField.getText().equals("")) {
            errorSecondLabel.setText("Укажите Вашу Фамилию");
            errorSecondLabel.setVisible(true);
            surnameField.setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px; -fx-border-radius: 3px");
        }
        else if (nameField.getText().equals("")) {
            surnameField.setStyle("-fx-border: none");
            errorSecondLabel.setText("Укажите Ваше Имя");
            errorSecondLabel.setVisible(true);
            nameField.setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px; -fx-border-radius: 3px");
        }
        else if (groupField.getText().equals("")) {
            surnameField.setStyle("-fx-border: none");
            nameField.setStyle("-fx-border: none");
            errorSecondLabel.setText("Укажите Вашу Группу");
            errorSecondLabel.setVisible(true);
            groupField.setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px; -fx-border-radius: 3px");
        }
        else {
            errorSecondLabel.setVisible(false);
            surnameField.setStyle("-fx-border: none");
            nameField.setStyle("-fx-border: none");
            groupField.setStyle("-fx-border: none");
            return true;
        }
        return false;
    }

    public void toggleStudentClick(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();
        if (!(source instanceof ToggleSwitch))
            return;
        if (toggleTeacher.isSelected())
            toggleTeacher.setSelected(false);
        if (checkLoginPassword()) {
            hideLoginPassword();
            showStudentIdentify();
            toggleTeacher.setDisable(true);
            flagToggleStudent = true;
        }
        else if (!checkLoginPassword())
            toggleStudent.setSelected(false);
    }

    public void toggleTeacherClick(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();
        if (!(source instanceof ToggleSwitch))
            return;
        if (toggleStudent.isSelected())
            toggleStudent.setSelected(false);
        if (checkLoginPassword())
            flagToggleTeacher = true;
        else if (!checkLoginPassword())
            toggleTeacher.setSelected(false);
    }
}
