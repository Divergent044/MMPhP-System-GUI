import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
public class ScalingDemo extends Application{
    public void start(Stage primaryStage) throws Exception {
        WebView webView = new WebView();
        ImageView img = new ImageView();
        Image image = new Image(ScalingDemo.class.getResourceAsStream("sample/img/mephi.png"));
        img.setImage(image);
        Slider slider = new Slider(0.5,2,1);
        ZoomingPane zoomingPane = new ZoomingPane(/*webView*/img);
        zoomingPane.zoomFactorProperty().bind(slider.valueProperty());
        primaryStage.setScene(new Scene(new BorderPane(zoomingPane, null, null, slider, null/*new Button("Button")*/)));
        webView.getEngine().load("http://www.google.com");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}