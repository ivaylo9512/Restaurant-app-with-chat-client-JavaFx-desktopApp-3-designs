package sample;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoggedSecondStyle extends LoginFirstStyle{

    static Stage stage;

    static void displayLoggedScene() throws IOException {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        FXMLLoader loader = new FXMLLoader(LoggedSecondStyle.class.getResource("/FXML/logged-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(LoggedSecondStyle.class.getResource("/css/logged-second.css").toString());

        stage = new Stage();
        ControllerLoggedSecondStyle controller = loader.getController();
        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(isShowing) {
                try {
                    controller.displayUserInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                controller.resetStage();
            }
        });

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());

        AnchorPane contentRoot = (AnchorPane) root.getChildren().get(0);
        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getWidth()) / 2);
    }
}
