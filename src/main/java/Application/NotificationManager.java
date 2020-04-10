package Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationManager {
    public ObservableList<String> notifications = FXCollections.observableArrayList();

    private NotificationManager(){
    }

    static NotificationManager initialize(){
        return new NotificationManager();
    }

    public void addNotification(String notification){
        notifications.add(notification);
    }

    public void removeNotification(String notification){
        notifications.remove(notification);
    }
}
