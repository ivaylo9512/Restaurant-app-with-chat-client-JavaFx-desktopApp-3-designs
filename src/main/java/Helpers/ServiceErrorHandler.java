package Helpers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;

public class ServiceErrorHandler implements EventHandler<WorkerStateEvent> {
    @Override
    public void handle(WorkerStateEvent event) {
        Service service = (Service) event.getSource();
        Throwable exception = service.getException();
        if(exception != null) {
            if (exception.getMessage().equals("Jwt token has expired.")) {
                loginManager.logout();
                stageManager.showAlert("Session has expired.");
            } else if(exception.getMessage().equals("Socket closed")) {
                service.restart();
            }else{
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), eventT -> service.restart()));
                timeline.play();
            }
        }
    }
}
