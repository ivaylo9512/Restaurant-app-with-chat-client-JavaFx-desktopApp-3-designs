package sample;

import Animations.MoveRoot;
import Animations.ResizeRoot;
import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
//import Helpers.ListViews.ChatsListViewCell;
import Helpers.Scrolls;
import Models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import sample.base.ControllerLogged;

import java.util.*;
//import static Helpers.ServerRequests.*;

public class ControllerLoggedSecondStyle extends ControllerLogged {
    @FXML Label dishesCountLabel;

    @FXML AnchorPane menuRoot,menu, menuButtons, menuButtonsContainer, profileView, menuContent, orderInfo, orderView,
            chatView, userChatsClip, createView, dishesContainer, chatContainer;

    @FXML Button menuButton, updateButton;
    @FXML HBox notificationsInfo;
    @FXML Region notificationIcon;
    @FXML Pane profileImageContainer, profileImageClip, contentBar;
    @FXML ListView<Chat> userChats;
    @FXML TextArea chatTextArea;
    @FXML ScrollPane chatScroll;
    @FXML VBox chatBlock;
    @FXML Region notificationRegion;

    private AnchorPane currentView, currentMenuView;

    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private ChatValue chatValue;

    @Override
    public void initialize() {
        super.initialize();

        ordersList.setCellFactory(param -> new ListCell<Order>(){
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Order" + item.getId().get());
                }
            }
        });

//        userChats.setCellFactory(chatCell -> new ChatsListViewCell());

        chatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && chatValue != null) {
                loadOlderHistory(chatValue, chatBlock);
            }
        });

        chatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage();
                event.consume();
            }
        });

        Scrolls scrolls = new Scrolls(chatScroll, chatTextArea);
        scrolls.manageScrollsSecondStyle();

        MoveRoot.move(menuButton, menuRoot);
        MoveRoot.move(contentBar, contentRoot);
        ResizeRoot.addListeners(contentRoot);

        menuContent.getChildren().remove(profileView);

        Circle clip = new Circle(30.8, 30.8, 30.8);
        profileImageClip.setClip(clip);

        Rectangle chatsClip = new Rectangle(211, 421);
        userChatsClip.setClip(chatsClip);

        Rectangle notificationClip = new Rectangle();
        notificationClip.setArcHeight(33);
        notificationClip.setArcWidth(33);
        notificationClip.heightProperty().bind(notificationsList.heightProperty());
        notificationClip.widthProperty().bind(notificationsList.widthProperty());

        notificationsList.setClip(notificationClip);

        chatBlock.prefWidthProperty().bind(chatScroll.widthProperty().subtract(25));

        editIndicator.maxHeightProperty().bind(editButton.heightProperty().subtract(15));

        setNotificationIcon();
    }

    private void setNotificationIcon() {
        SVGPath usb3 = new SVGPath();
        usb3.setContent("m434.753906 360.8125c-32.257812-27.265625-50.753906-67.117188-50.753906-109.335938v-59.476562c0-75.070312-55.765625-137.214844-128-147.625v-23.042969c0-11.796875-9.558594-21.332031-21.332031-21.332031-11.777344 0-21.335938 9.535156-21.335938 21.332031v23.042969c-72.253906 10.410156-128 72.554688-128 147.625v59.476562c0 42.21875-18.496093 82.070313-50.941406 109.503907-8.300781 7.105469-13.058594 17.429687-13.058594 28.351562 0 20.589844 16.746094 37.335938 37.335938 37.335938h352c20.585937 0 37.332031-16.746094 37.332031-37.335938 0-10.921875-4.757812-21.246093-13.246094-28.519531zm0 0");

        SVGPath usb4 = new SVGPath();
        usb4.setContent("m234.667969 512c38.632812 0 70.953125-27.542969 78.378906-64h-156.757813c7.421876 36.457031 39.742188 64 78.378907 64zm0 0");
        Shape s = Shape.union(usb3,usb4);
        s.setFill(Paint.valueOf("FC3903"));

        HBox notificationIcon = (HBox)this.notificationIcon;

        notificationRegion.setShape(s);
        notificationRegion.minWidthProperty().bind(updateButton.widthProperty().subtract(68));
        notificationRegion.prefWidthProperty().bind(updateButton.widthProperty().subtract(68));
        notificationRegion.setMaxSize(14, 14);
        notificationRegion.minHeightProperty().bind(notificationRegion.widthProperty());
        notificationRegion.prefHeightProperty().bind(notificationRegion.widthProperty());
        notificationRegion.setStyle("-fx-background-color: #FC3903");
        notificationRegion.getStyleClass().add("shadow");

        notificationIcon.setAlignment(Pos.CENTER);
        notificationIcon.prefWidthProperty().bind(updateButton.widthProperty().subtract(57));
        notificationIcon.prefHeightProperty().bind(updateButton.widthProperty().subtract(57));
        notificationIcon.setMaxSize(25, 25);
        notificationIcon.setTranslateX(40);
        notificationIcon.setTranslateY(-8);
        notificationIcon.setStyle("-fx-background-radius: 5em;" + "-fx-background-color: #1a1a1a;" + "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,1) , 6.5, 0.4 , 0 , 0 )");

        RotateTransition tt = new RotateTransition(Duration.millis(200), notificationIcon);
        tt.setByAngle(16);
        RotateTransition tl = new RotateTransition(Duration.millis(200), notificationIcon);
        tl.setByAngle(-32);
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(500));

        SequentialTransition sequentialTransition = new SequentialTransition(pauseTransition, tt, tl);
        sequentialTransition.setCycleCount(Timeline.INDEFINITE);
        sequentialTransition.setAutoReverse(true);
        sequentialTransition.play();

    }

    @FXML
    public void showChatView(){
        displayView(chatView);
    }
    @FXML
    public void showOrderView(){
        displayView(orderView);
    }
    @FXML
    public void showCreateView(){
        displayView(createView);
    }

    private void displayView(AnchorPane requestedView){
        if(requestedView.equals(currentView)){
            contentRoot.setOpacity(0);
            contentRoot.setDisable(true);
            requestedView.setOpacity(0);
            requestedView.setDisable(true);

            currentView = null;
        }else if(currentView == null) {
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            contentRoot.setOpacity(1);
            contentRoot.setDisable(false);
            currentView = requestedView;
        }else{
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            currentView.setDisable(true);
            currentView.setOpacity(0);
            currentView = requestedView;
        }
    }

    private void waitForNewMessages(){
//        messageService = new MessageService();
//        messageService.setOnSucceeded(event -> {
//            MessageService.lastMessageCheck = LocalDateTime.now();
//            List<Message> newMessages = (List<Message>) messageService.getValue();
//            newMessages.forEach(message -> {
//                int index = chatBlock.getChildren().size();
//                chatBlock.setId("new-message");
//
//                ChatValue chat = chatsMap.get(message.getChatId());
//                ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();
//
//                Session session = sessions.get(LocalDate.now());
//                if (session == null) {
//                    LocalDate sessionDate = LocalDate.now();
//
//                    session = new Session();
//                    session.setDate(sessionDate);
//                    sessions.put(0, sessionDate, session);
//                    session.getMessages().add(message);
//
//                    if (chatValue != null && chatValue.getChatId() == message.getChatId()) {
//                        chatValue.setDisplayedSessions(chatValue.getDisplayedSessions() + 1);
//                        appendSession(session, chatBlock, chatValue, index);
//                    }
//                } else {
//                    session.getMessages().add(message);
//                    if (chatValue != null && chatValue.getChatId() == message.getChatId()) {
//                        appendMessage(message, chatValue, (VBox) chatBlock.getChildren().get(index - 1));
//                    }
//                }
//                updateLastMessage(message.getChatId(), message.getMessage());
//            });
//            messageService.restart();
//        });
//
//        messageService.setOnFailed(event -> serviceFailed(messageService));
    }

    @FXML
    public void onNotificationClick(){
        notificationsList.getItems().remove(notificationsList.getFocusModel().getFocusedItem());
    }

    @FXML public void expandMenu(){
        if(menuButtonsContainer.getChildren().size() == 1){
            menuButtonsContainer.getChildren().add(0, menuButtons);
        }
        TransitionResizeWidth expand = new TransitionResizeWidth(Duration.millis(700), menu, 518);
        expand.play();
    }
    @FXML
    public void reverseMenu(){
        if(currentMenuView == null) {
            TransitionResizeWidth reverse = new TransitionResizeWidth(Duration.millis(700), menu, 38.5);
            reverse.play();
            menuButtonsContainer.getChildren().remove(menuButtons);
        }
    }
    private void expandMenuContent(){
        TransitionResizeHeight expand = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
        expand.play();
    }
    private void reverseMenuContent(){
        TransitionResizeHeight reverse = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
        reverse.play();
    }
    @Override
    public void setStage() throws Exception{
        super.setStage();

//        ObservableList<Chat> chats = FXCollections.observableArrayList(getChats());
//        setChatValues(chats);
//        userChats.setItems(chats);

        menuRoot.setLayoutX((primaryScreenBounds.getWidth() - menuRoot.getWidth()) / 2);
        menuRoot.setLayoutY(contentRoot.getLayoutY() - 60);
    }

    @Override
    public void resetStage(){
        super.resetStage();

        chatValue = null;
        chatTextArea.setText(null);
        menuSearch.setText("");

        chatBlock.getChildren().remove(1,chatBlock.getChildren().size());

        chatContainer.setDisable(true);
        chatContainer.setOpacity(0);

        userChats.getItems().clear();

        menuContent.setDisable(true);
        menuContent.getChildren().remove(profileView);
        notificationsView.setOpacity(0);
        notificationsView.setDisable(true);

        contentRoot.setOpacity(0);
        contentRoot.setDisable(true);

        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }

        currentMenuView = null;
        currentView = null;

        expandMenu();
        reverseMenuContent();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.play();
    }

    @FXML
    public void showProfile(){
        if(menuContent.isDisabled()) {
            expandMenuContent();
            currentMenuView = profileView;

            menuContent.getChildren().add(profileView);
            menuContent.setDisable(false);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();

        }else if(!currentMenuView.equals(profileView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            currentMenuView.setOpacity(0);
            currentMenuView.setDisable(true);
            currentMenuView = profileView;

            profileImageContainer.setOpacity(1);
            menuContent.getChildren().add(profileView);

        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            reverseMenuContent();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                menuContent.getChildren().remove(profileView);
                currentMenuView = null;
                reverseMenu();
            }));
            removeView.play();
        }
    }
    @FXML
    public void showNotifications(){
        if(menuContent.isDisabled()) {
            expandMenuContent();

            currentMenuView = notificationsView;
            currentMenuView.setOpacity(1);
            currentMenuView.setDisable(false);
            menuContent.setDisable(false);

            isNewNotificationChecked.set(true);
        }else if(!currentMenuView.equals(notificationsView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            menuContent.getChildren().remove(currentMenuView);
            profileImageContainer.setOpacity(0);

            currentMenuView = notificationsView;
            currentMenuView.setDisable(false);
            currentMenuView.setOpacity(1);

            isNewNotificationChecked.set(true);
        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            reverseMenuContent();

            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                currentMenuView.setDisable(true);
                currentMenuView.setOpacity(0);
                currentMenuView = null;
                reverseMenu();
            }));
            removeView.play();
        }
    }
    @FXML
    public void profileButtonHoverOver(MouseEvent event){
        AnchorPane shadowContainer = (AnchorPane) ((Button)event.getSource()).getParent();
        shadowContainer.getStyleClass().add("profile-button-hovered");
    }
    @FXML
    public void profileButtonHoverOut(MouseEvent event){
        AnchorPane shadowContainer = (AnchorPane) ((Button)event.getSource()).getParent();
        shadowContainer.getStyleClass().remove("profile-button-hovered");
    }
    @FXML
    public void focus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -5.5);
        AnchorPane.setBottomAnchor(button, -4.0);
    }
    @FXML
    public void unFocus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -1.0);
        AnchorPane.setBottomAnchor(button, 0.0);
    }

    @FXML
    private void showOrder(){
        Order order = ordersList.getSelectionModel().getSelectedItem();
        bindOrderProperties(order);

        if(!currentView.equals(orderView)){
            currentView.setOpacity(0);
            currentView.setDisable(true);

            orderView.setOpacity(1);
            orderView.setDisable(false);
            currentView = orderView;
        }


        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), orderInfo);
        fadeIn.setFromValue(0.36);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @Override
    public void bindOrderProperties(Order currentOrder) {
        super.bindOrderProperties(currentOrder);

        dishesCountLabel.textProperty().unbind();
        dishesCountLabel.textProperty().bind(Bindings.concat("Dishes ")
                .concat(Bindings.size(currentDishList.getItems()).asString()));
    }

    private void setChatValues(List<Chat> chats) {
        chats.forEach(chat -> {
            InputStream in;
            Image profilePicture;
            User user;

            if (chat.getFirstUser().getId() == loggedUser.getId()) {
                user = chat.getSecondUser();
                try {
                    in = new BufferedInputStream(
                            new URL(chat.getSecondUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);
                    in.close();
                }catch(Exception e){
                    profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
                }
            } else {
                user= chat.getFirstUser();
                try {
                    in = new BufferedInputStream(
                            new URL(chat.getFirstUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);
                    in.close();
                }catch(Exception e){
                    profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
                }
            }
            user.setImage(profilePicture);

            ChatValue chatValue = new ChatValue(chat.getId(), user.getId(), profilePicture);
            chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));
            chatsMap.put(chat.getId(), chatValue);
        });
    }

    @FXML
    private void setChat() {
        Chat selectedChat = userChats.getSelectionModel().getSelectedItem();

        if(selectedChat != null) {
            chatContainer.setOpacity(1);
            chatContainer.setDisable(false);

            int chatId = selectedChat.getId();
            ChatValue chat = chatsMap.get(chatId);

            HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
            Text info = (Text) sessionInfo.lookup("Text");

            if (chatValue == null || chatId != chatValue.getChatId()) {
                chatBlock.setId("beginning");
                chatBlock.getChildren().remove(1, chatBlock.getChildren().size());

                chatValue = chat;

                ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
                List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
                List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

                if (lastSessions.size() == pageSize) {
                    info.setText("Scroll for more history");
                    chatValue.setDisplayedSessions(pageSize);
                } else {
                    info.setText("Beginning of the chat");
                    chatValue.setMoreSessions(false);
                    chatValue.setDisplayedSessions(lastSessions.size());
                }

                lastSessions.forEach(session -> appendSession(session, chatBlock, chatValue, 1));

            }
        }
    }
    private void loadOlderHistory(ChatValue chatValue, VBox chatBlock) {
        int displayedSessions = chatValue.getDisplayedSessions();
        int loadedSessions = chatValue.getSessions().size();
        int nextPage = loadedSessions / pageSize;

        HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");

        ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> nextSessions;
        if (loadedSessions > displayedSessions) {

            nextSessions = chatSessions.subList(displayedSessions,
                    Math.min(displayedSessions + pageSize, loadedSessions));

            if (displayedSessions + nextSessions.size() == loadedSessions && !chatValue.isMoreSessions()) {
                info.setText("Beginning of the chat");
            }
            chatValue.setDisplayedSessions(displayedSessions + nextSessions.size());

            nextSessions.forEach(session -> appendSession(session, chatBlock, chatValue, 1));

        } else if (chatValue.isMoreSessions()) {
            nextSessions = getNextSessions(chatValue.getChatId(), nextPage, pageSize);
            if (nextSessions.size() < pageSize) {
                chatValue.setMoreSessions(false);
                info.setText("Beginning of the chat");
            }
            chatValue.setDisplayedSessions(displayedSessions + nextSessions.size());
            nextSessions.forEach(session -> {

                if (!sessionsMap.containsKey(session.getDate())) {
                    sessionsMap.put(session.getDate(), session);

                    appendSession(session, chatBlock, chatValue, 1);
                }

            });
        } else {
            info.setText("Beginning of the chat");
        }
    }
    private void appendSession(Session session, VBox chatBlock, ChatValue chatValue, int index) {

        Text date = new Text(dateFormatter.format(session.getDate()));
        TextFlow dateFlow = new TextFlow(date);
        dateFlow.setTextAlignment(TextAlignment.CENTER);

        HBox sessionDate = new HBox(dateFlow);
        HBox.setHgrow(dateFlow, Priority.ALWAYS);
        sessionDate.getStyleClass().add("session-date");

        VBox sessionBlock = new VBox(sessionDate);
        sessionBlock.setId(session.getDate().toString());
        session.getMessages()
                .forEach(message -> appendMessage(message, chatValue, sessionBlock));
        chatBlock.getChildren().add(index, sessionBlock);
    }

    private void appendMessage(Message message, ChatValue chat, VBox chatBlock) {
        HBox hBox = new HBox();
        VBox newBlock = new VBox();
        Text text = new Text();
        Text time = new Text();
        ImageView imageView = new ImageView();
        TextFlow textFlow = new TextFlow();

        time.getStyleClass().add("time");
        text.getStyleClass().add("message");
        newBlock.getStyleClass().add("chat-block");

        imageView.setFitHeight(34);
        imageView.setFitWidth(34);
        imageView.setLayoutX(3);
        imageView.setLayoutY(7);

        Circle clip = new Circle(20.5, 20.5, 20.5);

        Pane imageContainer = new Pane(imageView);
        imageContainer.setClip(clip);
        imageContainer.setMaxHeight(40);
        imageContainer.setMaxWidth(40);
        imageContainer.setMinWidth(40);


        Pane imageShadow = new Pane(imageContainer);
        imageShadow.setMaxHeight(40);
        imageShadow.setMaxWidth(40);
        imageShadow.setMinWidth(40);
        imageShadow.getStyleClass().add("imageShadow");

        imageShadow.setViewOrder(1);
        textFlow.setViewOrder(5);

        HBox.setMargin(imageShadow, new Insets(-20, 0, 0, 0));

        if (message.getReceiverId() == loggedUser.getId()) {
            imageView.setImage(chat.getSecondUserPicture());
            text.setText(message.getMessage());
            time.setText("  " + timeFormatter.format(message.getTime()));
            textFlow.getChildren().addAll(text, time);
            hBox.setAlignment(Pos.TOP_LEFT);

        } else {
            imageView.setImage(userProfileImage);
            text.setText(message.getMessage());
            time.setText(timeFormatter.format(message.getTime()) + "  ");
            textFlow.getChildren().addAll(time, text);
            hBox.setAlignment(Pos.TOP_RIGHT);

        }

        boolean timeElapsed;
        int timeToElapse = 10;

        List<Node> messageBlocks = chatBlock.getChildren();
        if (messageBlocks.size() > 0 && messageBlocks.get(messageBlocks.size() - 1) instanceof VBox) {
            VBox lastBlock = (VBox) messageBlocks.get(messageBlocks.size() - 1);
            HBox lastMessage = (HBox) lastBlock.getChildren().get(lastBlock.getChildren().size() - 1);
            LocalTime lastBlockStartedDate;
            TextFlow firstTextFlow = (TextFlow) lastMessage.lookup("TextFlow");
            Text lastBlockStartedText = (Text) firstTextFlow.lookup(".time");
            lastBlockStartedDate = LocalTime.parse(lastBlockStartedText.getText().replaceAll("\\s+", ""));

            timeElapsed = java.time.Duration.between(lastBlockStartedDate, message.getTime()).toMinutes() > timeToElapse;

            if (message.getReceiverId() == loggedUser.getId()) {
                if (!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("second-user-message")) {

                    hBox.getStyleClass().add("second-user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                } else {

                    hBox.getStyleClass().add("second-user-message-first");
                    hBox.getChildren().addAll(imageShadow, textFlow);
                    newBlock.getChildren().add(hBox);
                    chatBlock.getChildren().add(newBlock);

                }
            } else {
                if (!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("user-message")) {

                    hBox.getStyleClass().add("user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                } else {

                    hBox.getStyleClass().add("user-message-first");
                    hBox.getChildren().addAll(textFlow, imageShadow);
                    newBlock.getChildren().add(hBox);
                    chatBlock.getChildren().add(newBlock);

                }
            }
        } else {

            if (message.getReceiverId() == loggedUser.getId()) {
                hBox.getStyleClass().add("second-user-message-first");
                hBox.getChildren().addAll(imageShadow, textFlow);
                newBlock.getChildren().add(hBox);
            } else {
                hBox.getStyleClass().add("user-message-first");
                hBox.getChildren().addAll(textFlow, imageShadow);
                newBlock.getChildren().add(hBox);
            }
            chatBlock.getChildren().add(newBlock);
        }
    }

    @FXML
    public void addNewMessage(){
        String messageText = chatTextArea.getText();
        int chatId = chatValue.getChatId();
        int receiverId = chatValue.getUserId();
        int index = chatBlock.getChildren().size();
        chatTextArea.clear();

        if (messageText.length() > 0){
            Message message = sendMessage(messageText, chatId, receiverId);
            updateLastMessage(chatId, messageText);

            ChatValue chat = chatsMap.get(chatId);
            ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();

            chatBlock.setId("new-message");
            Session session = sessions.get(LocalDate.now());
            if (session == null) {
                LocalDate sessionDate = LocalDate.now();

                session = new Session();
                session.setDate(sessionDate);
                sessions.put(0, sessionDate, session);
                session.getMessages().add(message);

                if (chatValue.getChatId() == message.getChatId()) {
                    chatValue.setDisplayedSessions(chatValue.getDisplayedSessions() + 1);
                    appendSession(session, chatBlock, chatValue, index);
                }
            } else {
                session.getMessages().add(message);
                if (chatValue.getChatId() == message.getChatId()) {
                    appendMessage(message, chatValue, (VBox) chatBlock.getChildren().get(index - 1));
                }
            }
        }
    }
    private void updateLastMessage(int chatId, String message) {
        Label lastMessage = (Label) contentRoot.lookup("#lastMessage" + chatId);
        lastMessage.setText(message);
    }
}
