package danshev.spring.gui;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
public class MainGui implements InitializingBean {

    @Autowired
    private ScreensController bean;

    public void setScreensController(ScreensController controller) {
        this.bean = controller;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        FXMLLoader loader = bean.getLoader("/MainGui.fxml");

        AnchorPane mainPane = null;
        try {
            mainPane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MainGuiController controller = loader.getController();

        Scene scene = new Scene(mainPane);

        Stage window = new Stage();

        window.setTitle("Data Submit App");
        window.setScene(scene);
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });
        window.show();
    }

}
