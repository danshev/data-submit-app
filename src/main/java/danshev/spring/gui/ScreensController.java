package danshev.spring.gui;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

@Service
public class ScreensController implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Stage stage;
    private String currentScreenId;

    public void init(Stage stage) {
        this.stage = stage;
        Group root = new Group();
        this.stage.setScene(new Scene(root));

    }

    public FXMLLoader getLoader(String fxml) {
        FXMLLoader loader = new FXMLLoader(MainGui.class.getResource(fxml));

        return loader;
    }

    private boolean swapScreen(final Parent root) {
        final Group rootGroup = getScreenRoot();
        final DoubleProperty opacity = rootGroup.opacityProperty();
        if (!isScreenEmpty()) {

            Timeline fade = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
                    new KeyFrame(new Duration(250),
                            new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent t) {
                                    rootGroup.getChildren().remove(0);

                                    rootGroup.getChildren().add(0, root);
                                    Timeline fadeIn = new Timeline(
                                            new KeyFrame(Duration.ZERO,
                                                    new KeyValue(opacity, 0.0)),
                                            new KeyFrame(new Duration(350),
                                                    new KeyValue(opacity,
                                                            1.0)));
                                    fadeIn.play();
                                }
                            }, new KeyValue(opacity, 0.0)));
            fade.play();
            return true;
        } else {
            opacity.set(0.0);
            rootGroup.getChildren().add(0, root);
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                    new KeyFrame(new Duration(350),
                            new KeyValue(opacity, 1.0)));
            fadeIn.play();
        }

        if (!this.stage.isShowing()) {
            this.stage.show();
        }
        return true;
    }

    private Group getScreenRoot() {
        return (Group) this.stage.getScene().getRoot();
    }

    private boolean isScreenEmpty() {
        return getScreenRoot().getChildren().isEmpty();
    }

    public String getCurrentScreenId() {
        return currentScreenId;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
}
