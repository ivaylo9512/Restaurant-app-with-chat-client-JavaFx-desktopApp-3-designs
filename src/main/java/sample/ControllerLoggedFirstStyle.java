package sample;

import Animations.ExpandOrderPane;
import Animations.ResizeMainChat;
import Models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import static sample.Reversed.reversed;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;


public class ControllerLoggedFirstStyle {
    @FXML Label firstName, lastName, country, age, role;
    @FXML FlowPane ordersFlow;
    @FXML Pane contentPane;
    @FXML VBox mainChatBlock, chatUsers;
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll, ordersScroll, mainChatScroll;
    @FXML AnchorPane contentRoot, mainChat;
    @FXML ImageView roleImage;
    @FXML TextArea mainChatTextArea;
    private User loggedUser;
    private ObjectMapper mapper = new ObjectMapper();
    private HashMap<Integer, ChatSpec> chatsMap = new HashMap<>();
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    private Preferences userPreference = Preferences.userRoot();
    private Image userProfileImage;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    private ChatSpec mainChatSpec;
    private int pageSize = 3;
    @FXML
    public void initialize() throws IOException {
        mapper.registerModule(new JavaTimeModule());
        String userJson = userPreference.get("user",null);
        loggedUser = mapper.readValue(userJson, User.class);
        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserInfo();

        List<Order> orders = getOrders();
        appendOrders(orders);
        getChats();

        manageSceneScrolls();

        ExpandOrderPane.scrollPane = ordersScroll;
        ExpandOrderPane.contentPane = contentPane;
        ExpandOrderPane.buttonExpandedProperty().addListener((observable, oldValue, newValue) -> {
            Button currentButton = ExpandOrderPane.button;
            if(newValue){
                currentButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);
                currentButton.addEventFilter(MouseEvent.MOUSE_CLICKED, reverseOrderHandler);
            }else{
                currentButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, reverseOrderHandler);
                currentButton.addEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);
            }
        });

        ResizeMainChat.resize(mainChat);
    }

    private void fixBlurryContent(ScrollPane scrollPane){
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollPane.getChildrenUnmodifiable().get(0).setCache(false);
            }
        });
    }

    private void manageSceneScrolls() {
        fixBlurryContent(menuScroll);
        fixBlurryContent(userInfoScroll);
        fixBlurryContent(chatUsersScroll);
        fixBlurryContent(mainChatScroll);
        fixBlurryContent(ordersScroll);
        mainChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurryContent(textAreaScroll);

            }
        });
        ordersScroll.setOnScroll(event -> {
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                FlowPane pane = (FlowPane) ordersScroll.getContent();
                ordersScroll.setHvalue(ordersScroll.getHvalue() - event.getDeltaY() / pane.getWidth());
            }
        });

        AnchorPane anchorPane = (AnchorPane) menuScroll.getContent();
        menuScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {

                if (menuScroll.getHeight() <= 211) {
                    ScrollPane scrollPane;
                    if (menuScroll.getVvalue() == 0) {
                        scrollPane = userInfoScroll;
                    }else {
                        scrollPane = chatUsersScroll;
                    }

                    Pane content = (Pane) scrollPane.getContent();
                    scrollPane.setVvalue(scrollPane.getVvalue() - event.getDeltaY() / content.getHeight());
                    event.consume();
                }else{
                    chatUsersScroll.setDisable(true);
                    userInfoScroll.setDisable(false);

                    if(anchorPane.getHeight() <= menuScroll.getHeight()){
                        chatUsersScroll.setDisable(false);
                    }else if(menuScroll.getVvalue() == 1){
                        chatUsersScroll.setDisable(false);
                        userInfoScroll.setDisable(true);
                    }
                }

            }

        });
    }

    private void displayUserInfo(){
        firstName.setText(loggedUser.getFirstName());
        lastName.setText(loggedUser.getLastName());
        country.setText(loggedUser.getCountry());
        age.setText(String.valueOf(loggedUser.getAge()));
        role.setText(loggedUser.getRole());

        if (loggedUser.getRole().equals("chef")) {
            roleImage.setImage(new Image(getClass().getResourceAsStream("/chef-second.png")));
        }else{
            roleImage.setImage(new Image(getClass().getResourceAsStream("/waiter-second.png")));
        }
    }
    private void getChats(){
        HttpGet get = new HttpGet("http://localhost:8080/api/auth/chat/getChats");
        get.setHeader("Authorization", userPreference.get("token", null));

        try(CloseableHttpResponse response = httpClient.execute(get)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            if(responseStatus != 200){
                EntityUtils.consume(entity);
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            List<Chat> chats = mapper.readValue(content, new TypeReference<List<Chat>>(){});

            chats.forEach(chat -> {

                try {
                    InputStream in;
                    ChatSpec chatSpec;
                    Image profilePicture;
                    if(chat.getFirstUser().getId() == loggedUser.getId()){
                        in = new BufferedInputStream(
                        new URL(chat.getSecondUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);
                        chatSpec = new ChatSpec(chat.getId(), chat.getSecondUser().getId(), profilePicture);
                    }else{
                        in = new BufferedInputStream(
                                new URL(chat.getFirstUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);

                        chatSpec = new ChatSpec(chat.getId(), chat.getFirstUser().getId(), profilePicture);
                    }
                    in.close();
                    ImageView imageView = new ImageView(profilePicture);
                    imageView.setId(String.valueOf(chat.getId()));
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.setOnMouseClicked(this::setMainChat);
                    chatUsers.getChildren().add(imageView);
                    chatsMap.put(chat.getId(), chatSpec);

                }catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
    }
    private void setMainChat(MouseEvent event){
        ImageView imageView = (ImageView) event.getSource();
        int chatId = Integer.parseInt(imageView.getId());
        ChatSpec chat = chatsMap.get(chatId);
//        int page = chat.getSessions().size() / perPage;
        if(mainChatSpec != null && chatId == mainChatSpec.getChatId()){
            if(mainChat.isDisabled()){
                mainChat.setDisable(false);
                mainChat.setOpacity(1);
            }else{
                mainChat.setOpacity(0);
                mainChat.setDisable(true);
            }
        }else{
            mainChatBlock.getChildren().clear();
            mainChat.setDisable(false);
            mainChat.setOpacity(1);
            mainChatSpec = chat;

            List<Session> chatSessions = mainChatSpec.getSessions();
            if(chatSessions == null) {
                List<Session> sessions = getNextSessions(chatId, 0, pageSize);
                mainChatSpec.setSessions(sessions);

                if(sessions.size() < pageSize){
                    mainChatSpec.setMoreSessions(false);
                }else{
                    Text info = new Text("Scroll for more history");
                    TextFlow sessionInfo = new TextFlow(info);
                    HBox hbox = new HBox(sessionInfo);
                    hbox.getStyleClass().add("session-info");
                    hbox.setAlignment(Pos.CENTER);
                    sessionInfo.setTextAlignment(TextAlignment.CENTER);

                    mainChatBlock.getChildren().add(hbox);
                }

                reversed(sessions).forEach(session -> {

                    Text date = new Text(dateFormatter.format(session.getDate()));
                    TextFlow dateFlow = new TextFlow(date);
                    dateFlow.setTextAlignment(TextAlignment.CENTER);
                    HBox sessionDate = new HBox(dateFlow);
                    HBox.setHgrow(dateFlow, Priority.ALWAYS);
                    sessionDate.getStyleClass().add("session-date");

                    mainChatBlock.getChildren().add(sessionDate);
                    session.getMessages()
                            .forEach(message -> appendMessage(message, chat, mainChatBlock));
                });
            }else{
                List<Session> lastSessions = chatSessions.subList(
                        Math.max(chatSessions.size() - pageSize, 0), chatSessions.size());

                if(mainChatSpec.isMoreSessions()){
                    Text info = new Text("Scroll for more history");
                    TextFlow sessionInfo = new TextFlow(info);
                    HBox hbox = new HBox(sessionInfo);
                    hbox.getStyleClass().add("session-info");
                    hbox.setAlignment(Pos.CENTER);
                    sessionInfo.setTextAlignment(TextAlignment.CENTER);

                    mainChatBlock.getChildren().add(hbox);
                }

                reversed(lastSessions).forEach(session -> {
                    Text date = new Text(dateFormatter.format(session.getDate()));
                    TextFlow dateFlow = new TextFlow(date);
                    dateFlow.setTextAlignment(TextAlignment.CENTER);
                    HBox sessionDate = new HBox(dateFlow);
                    HBox.setHgrow(dateFlow, Priority.ALWAYS);
                    sessionDate.getStyleClass().add("session-date");

                    mainChatBlock.getChildren().add(sessionDate);
                    session.getMessages()
                            .forEach(message -> appendMessage(message, chat, mainChatBlock));
                });
            }
        }
        mainChatBlock.heightProperty().addListener((observable, oldValue, newValue) -> {
            mainChatScroll.setVvalue(1);
        });
    }
//    private void openChat(MouseEvent event){
//        ImageView imageView = (ImageView) event.getSource();
//        int chatId = Integer.parseInt(imageView.getId());
//        int pageSize = 3;
//        ChatSpec chat = chatsMap.get(chatId);
////        int page = chat.getSessions().size() / perPage;
//        System.out.println(imageView.getId());
//
//        List<Session> sessions = getNextSessions(chatId,0,pageSize);
//        sessions.forEach(session -> {
//            session.getMessages().forEach(this::appendMessage);
//        });
//    }

    private List<Session> getNextSessions(int id, int page, int pageSize){
        HttpGet get;
        List<Session> sessions = new LinkedList<>();
        try {

            URIBuilder builder = new URIBuilder("http://localhost:8080/api/auth/chat/nextSessions");
            builder
                    .setParameter("chatId", String.valueOf(id))
                    .setParameter("page", String.valueOf(page))
                    .setParameter("pageSize", String.valueOf(pageSize));
            get = new HttpGet(builder.build());
            get.setHeader("Authorization", userPreference.get("token", null));

            try(CloseableHttpResponse response = httpClient.execute(get)){

                int responseStatus = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);

                if(responseStatus != 200){
                    EntityUtils.consume(entity);
                    throw new HttpException("Invalid response code: " + responseStatus  + ". With an error message: " + content);
                }
                sessions = mapper.readValue(content, new TypeReference<List<Session>>(){});

            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return sessions;
    }
    private void appendMessage(Message message, ChatSpec chat, VBox chatBlock){
        HBox hBox = new HBox();
        VBox newBlock = new VBox();
        Text text = new Text();
        Text time = new Text();
        ImageView imageView = new ImageView();
        TextFlow textFlow = new TextFlow();

        time.getStyleClass().add("time");
        text.getStyleClass().add("message");
        imageView.getStyleClass().add("shadow");
        newBlock.getStyleClass().add("chat-block");
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        HBox.setMargin(imageView,new Insets(-20,0,0,0));

        if(message.getReceiver() == loggedUser.getId()){
            imageView.setImage(chat.getSecondUserPicture());
            text.setText(message.getMessage());
            time.setText("  " + timeFormatter.format(message.getDate()));
            textFlow.getChildren().addAll(text, time);
            hBox.setAlignment(Pos.TOP_LEFT);

        }else{
            imageView.setImage(userProfileImage);
            text.setText(message.getMessage());
            time.setText(timeFormatter.format(message.getDate()) + "  ");
            textFlow.getChildren().addAll(time, text);
            hBox.setAlignment(Pos.TOP_RIGHT);

        }

        boolean timeElapsed;
        int timeToElapse = 10;

        List<Node> messageBlocks = chatBlock.getChildren();
        if(messageBlocks.size() > 0 && messageBlocks.get(messageBlocks.size() - 1).getTypeSelector().equals("VBox")) {
            VBox lastBlock = (VBox) messageBlocks.get(messageBlocks.size() - 1);
            HBox lastMessage = (HBox) lastBlock.getChildren().get(lastBlock.getChildren().size() - 1);
            LocalTime lastBlockStartedDate;
            TextFlow firstTextFlow = (TextFlow)lastMessage.lookup("TextFlow");
            Text lastBlockStartedText = (Text)firstTextFlow.lookup(".time");
            lastBlockStartedDate = LocalTime.parse(lastBlockStartedText.getText().replaceAll("\\s+",""));

            timeElapsed = java.time.Duration.between(lastBlockStartedDate, message.getDate()).toMinutes() > timeToElapse;

            if(message.getReceiver() == loggedUser.getId()){
                if(!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("second-user-message")){

                    hBox.getStyleClass().add("second-user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                }else{

                    hBox.getStyleClass().add("second-user-message-first");
                    hBox.getChildren().addAll(imageView, textFlow);
                    newBlock.getChildren().add(hBox);
                    chatBlock.getChildren().add(newBlock);

                }
            }else{
                if(!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("user-message")){

                    hBox.getStyleClass().add("user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                }else{

                    hBox.getStyleClass().add("user-message-first");
                    hBox.getChildren().addAll(textFlow, imageView);
                    newBlock.getChildren().add(hBox);
                    chatBlock.getChildren().add(newBlock);

                }
            }
        }else{

            if(message.getReceiver() == loggedUser.getId()){
                hBox.getStyleClass().add("second-user-message-first");
                hBox.getChildren().addAll(imageView, textFlow);
                newBlock.getChildren().add(hBox);
            }else{
                hBox.getStyleClass().add("user-message-first");
                hBox.getChildren().addAll(textFlow, imageView);
                newBlock.getChildren().add(hBox);
            }
            chatBlock.getChildren().add(newBlock);
        }
    }
    @FXML
    private void scrollToChats(){
        Animation animation = new Timeline(
            new KeyFrame(Duration.millis(1000), new KeyValue(
                    menuScroll.vvalueProperty(), 1)));
        animation.play();
    }
    @FXML
    private void scrollToProfile(){
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 0)));
        animation.play();
    }

    private List<Order> getOrders(){
        List<Order> orders = new ArrayList<>();
        HttpGet httpGet = new HttpGet("http://localhost:8080/api/auth/order/findAll");
        httpGet.setHeader("Authorization", userPreference.get("token", null));
        try(CloseableHttpResponse response = httpClient.execute(httpGet)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity receivedEntity = response.getEntity();
            String content = EntityUtils.toString(receivedEntity);

            if(responseStatus != 200){
                EntityUtils.consume(receivedEntity);
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            orders = mapper.readValue(content, new TypeReference<List<Order>>(){});

            EntityUtils.consume(receivedEntity);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private void appendOrders(List<Order> orders) {
        orders.forEach(order -> {

            Pane orderPane = new Pane();
            orderPane.setLayoutX(20.6);
            orderPane.setLayoutY(51.0);
            orderPane.getStyleClass().add("order");

            Image clout = new Image(getClass().getResourceAsStream("/cloud-down.png"));
            ImageView imageView = new ImageView(clout);
            imageView.setFitWidth(15);
            imageView.setFitHeight(15);
            imageView.fitWidthProperty().setValue(15);
            imageView.fitHeightProperty().setValue(15);

            Button button = new Button("", imageView);
            button.setLayoutX(29);
            button.setLayoutY(48);
            button.setTranslateX(0);
            button.setTranslateY(0);
            button.setPrefWidth(28);
            button.setPrefHeight(28);
            button.setMinWidth(28);
            button.setMinHeight(28);
            button.addEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);

            Label label = new Label(String.valueOf(order.getId()));
            label.setLayoutX(28);
            label.setLayoutY(11);
            label.setDisable(true);

            Pane orderContainer = new Pane();
            orderContainer.getStyleClass().add("order-container");
            orderContainer.getChildren().add(orderPane);
            orderPane.getChildren().add(button);
            orderPane.getChildren().add(label);

            ordersFlow.getChildren().add(orderContainer);
        });
    }

    private EventHandler expandOrderHandler = (EventHandler<MouseEvent>) this::expandOrder;
    private EventHandler reverseOrderHandler = (EventHandler<MouseEvent>)e-> ExpandOrderPane.reverseOrder();

    @FXML
    public void expandOrder(MouseEvent event){
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        if(!ExpandOrderPane.action && (intersectedNode.getTypeSelector().equals("Button")
                ||intersectedNode.getStyleClass().get(0).equals("order"))){

            ExpandOrderPane.setCurrentOrder(event);

        }
    }
}
