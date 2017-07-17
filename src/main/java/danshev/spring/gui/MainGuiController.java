package danshev.spring.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.io.Files;
import com.google.gson.Gson;

import danshev.model.Event;
import danshev.model.Events;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MainGuiController implements Initializable {
    @FXML
    private Circle connectedDot;
    @FXML
    private Label connectedLabel;
    @FXML
    private ComboBox<Event> selectionOptions;

    @FXML
    private Button selectFolderButton;

    private String nifiAddr;
    private Integer nifiPort;
    private String nifiRoute;
    private Events events;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProperties();
        try {
            String eventsJson = Files.toString(new File("events.json"),
                    Charset.defaultCharset());
            Gson gson = new Gson();
            events = gson.fromJson(eventsJson, Events.class);
            selectionOptions.getItems().setAll(events.events);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    checkNiFi();
                }
            }, 0, 30000);
        } catch (IOException e) {
            setLabel(e.getMessage(), false);
        }
    }

    protected void checkNiFi() {
        try {
            URL url = new URL("http://" + nifiAddr + ":"
                    + Integer.toString(nifiPort) + nifiRoute);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            System.out.println("RESPONSE: " + code);
            setLabel("Connected to NiFi server at " + nifiAddr + ":" + nifiPort
                    + nifiRoute, code == 405);
        } catch (Exception e) {
            setLabel("NiFi server unreachable; operating in standalone mode",
                    false);
        }

    }

    private void setLabel(String string, boolean valid) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                connectedDot.setFill(valid ? Color.GREEN : Color.RED);
                connectedLabel.setText(string);
            }
        });
    }

    public void loadProperties() {
        Properties props = new Properties();
        InputStream is = null;

        // First try loading from the current directory
        try {
            File f = new File("config.properties");
            is = new FileInputStream(f);
            props.load(is);
            is.close();
        } catch (Exception e) {
            is = null;
        }

        nifiAddr = props.getProperty("nifi.ip", "127.0.0.1");
        nifiPort = new Integer(props.getProperty("nifi.port", "8080"));
        nifiRoute = props.getProperty("nifi.route", "/contentListener");
    }

    @FXML
    private void selectOption(ActionEvent event) {
        selectFolderButton.setVisible(true);
    }

    @FXML
    private void selectFolder(ActionEvent event) {
        // TODO select folder
    }
}
