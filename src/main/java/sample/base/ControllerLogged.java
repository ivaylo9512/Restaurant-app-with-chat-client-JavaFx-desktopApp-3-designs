package sample.base;

import Application.RestaurantApplication;
import Helpers.ListViews.MenuListViewCell;
import Models.Dish;
import Models.Menu;
import Models.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.MediaPlayer;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;

import static Application.RestaurantApplication.*;
import static Helpers.ServerRequests.httpClientLongPolling;
import static Helpers.ServerRequests.sendUserInfo;

public class ControllerLogged {
    @FXML
    protected TextField usernameField, firstNameField, lastNameField, countryField, ageField, menuSearch, roleField;
    @FXML
    protected Button saveButton, editButton;
    @FXML
    protected Pane userInfo;

    @FXML
    ListView<Menu> menuList, newOrderList;
    @FXML
    ListView<Dish> dishesList;
    @FXML
    ImageView profileImage;

    protected DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    protected DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    protected MediaPlayer notificationSound = RestaurantApplication.notificationSound;
    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    private BooleanProperty isEditable = new SimpleBooleanProperty(false);
    private User oldInfo;
    private Pane buttonParent;

    @FXML
    public void initialize() {
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());

        menuList.setItems(userMenu);
        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            userMenu.setAll(searchMenu(newValue.toLowerCase()).values());
        });

        buttonParent = (Pane) saveButton.getParent();
        buttonParent.getChildren().remove(saveButton);

        if(usernameField == null) usernameField = new TextField();

        usernameField.setEditable(false);
        usernameField.setDisable(true);
        roleField.setEditable(false);
        roleField.setDisable(true);

        bindUserFields();
    }

    private void bindUserFields() {
        loginManager.bindUserFields(usernameField.textProperty(), firstNameField.textProperty(), lastNameField.textProperty(), countryField.textProperty(),
                roleField.textProperty(), ageField.textProperty(), profileImage.imageProperty());

        firstNameField.editableProperty().bind(isEditable);
        lastNameField.editableProperty().bind(isEditable);
        ageField.editableProperty().bind(isEditable);
        countryField.editableProperty().bind(isEditable);

        firstNameField.disableProperty().bind(isEditable.not());
        lastNameField.disableProperty().bind(isEditable.not());
        ageField.disableProperty().bind(isEditable.not());
        countryField.disableProperty().bind(isEditable.not());
    }

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return orderManager.userMenu.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    @FXML
    public void editUserInfo() {
        isEditable.setValue(true);

        buttonParent.getChildren().add(saveButton);
        buttonParent.getChildren().remove(editButton);

        oldInfo = new User(usernameField.getText(), firstNameField.getText(), lastNameField.getText(), Integer.valueOf(ageField.getText()), countryField.getText());
    }

    @FXML
    public void saveUserInfo() {
        isEditable.setValue(false);

        buttonParent.getChildren().add(editButton);
        buttonParent.getChildren().remove(saveButton);

        if (loginManager.isUserEdited(oldInfo)) {
            User user = sendUserInfo(firstNameField.getText(), lastNameField.getText(),
                    ageField.getText(), countryField.getText());

            if (user == null) {
                firstNameField.setText(oldInfo.getFirstName().get());
                lastNameField.setText(oldInfo.getLastName().get());
                ageField.setText(oldInfo.getAge().get());
                countryField.setText(oldInfo.getCountry().get());
            }
        }
    }

    @FXML
    public void addMenuItem(){
        Menu menuItem = menuList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().add(0, menuItem);
    }

    @FXML
    public void removeMenuItem(){
        Menu menuItem = newOrderList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().remove(menuItem);
    }

    @FXML
    public void showLoggedFirstStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        stageManager.changeStage(stageManager.firstLoggedStage);


    }

    @FXML
    public void showLoggedSecondStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        stageManager.changeStage(stageManager.secondLoggedStage);

    }

    @FXML
    public void showLoggedThirdStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        stageManager.changeStage(stageManager.thirdLoggedStage);
    }

    @FXML
    public void minimize(){
        stageManager.currentStage.setIconified(true);
    }

    @FXML
    public void close(){
        stageManager.currentStage.close();
    }

    @FXML
    protected void logout(){
        loginManager.logout();
    }
}
