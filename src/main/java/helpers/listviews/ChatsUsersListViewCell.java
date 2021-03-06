package helpers.listviews;

import controllers.firststyle.LoggedFirst;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import models.ChatValue;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.io.IOException;

import static application.RestaurantApplication.stageManager;

public class ChatsUsersListViewCell extends ListCell<ChatValue> {
    @FXML
    private ImageView imageView;
    @FXML
    private Pane imageContainer;
    @FXML
    private HBox container;

    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(ChatValue chat, boolean empty) {
        super.updateItem(chat, empty);
        if(empty || chat == null){
            setGraphic(null);
            setText(null);
        }else{

            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/chat-user-cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            imageView.setImage(chat.getSecondUserPicture());

            setText(null);
            setGraphic(container);
        }
    }

    @FXML
    public void openChat(){
        if(stageManager.currentController instanceof LoggedFirst && stageManager.firstLoggedStage.isShowing()){
            ((LoggedFirst)stageManager.firstLoggedController).setChatValue(getItem(), this);
        }
    }
}
