package danshev.spring.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mitchellbosecke.pebble.error.PebbleException;

import danshev.model.Event;
import danshev.model.Events;
import danshev.model.FileData;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;
import danshev.spring.service.TemplateService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;

public class MainGuiController implements Initializable {
	private Gson gson = new Gson();
	
	@FXML
	private Circle connectedDot;
	@FXML
	private Label connectedLabel;
	@FXML
	private ComboBox<Event> selectionOptions;

	@FXML
	private Button selectFolderButton;

	@FXML
	private WebView webView;

	private String eventUUID;
	private String nifiAddr;
	private Integer nifiPort;
	private String nifiRoute;
	private boolean standaloneMode;
	private boolean nifiReachable;
	private Events events;
	private TemplateService templateService;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadProperties();
		try {
			String eventsJson = Files.toString(new File("events.json"), Charset.defaultCharset());
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
			URL url = new URL("http://" + nifiAddr + ":" + Integer.toString(nifiPort) + nifiRoute);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			int code = connection.getResponseCode();
			System.out.println("RESPONSE: " + code);
			setLabel("Connected to NiFi server at " + nifiAddr + ":" + nifiPort + nifiRoute, code == 405);
			nifiReachable = true;

		} catch (Exception e) {
			setLabel("NiFi server unreachable", false);
			nifiReachable = false;

			// TODO:
			//  Essentially, prevent the User from doing anything, so ...
			//  - hide folder picker
			selectFolderButton.setVisible(false);
			//  - load blank browser area
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

		standaloneMode = new Boolean(props.getProperty("status.standalone", true));
		nifiAddr = props.getProperty("nifi.ip", "127.0.0.1");
		nifiPort = new Integer(props.getProperty("nifi.port", "8080"));
		nifiRoute = props.getProperty("nifi.route", "/contentListener");
	}

	@FXML
	private void selectOption(ActionEvent event) {
		if(nifiReachable){
			selectFolderButton.setVisible(true);

			// TODO (Sprint 3)
			// - generate a UUID (http://www.javapractices.com/topic/TopicAction.do?Id=56), store in the variable `eventUUID`
		};
	}

	@FXML
	private void selectFolder(ActionEvent event) {
		FolderPathData params = new FolderPathData();
		params.processing = selectionOptions.getValue().initialAction != null;
		params.location = selectFolder();
		
		try {
			String postUrl = "http://127.0.0.1:8998/folderPathRaw";
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(postUrl);
			StringEntity postingString = new StringEntity(gson.toJson(params));
			post.setEntity(postingString);
			post.setHeader("Content-type", "application/json");
			HttpResponse response = httpClient.execute(post);
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String selectFolder() {
		DirectoryChooser directoryChooser = new DirectoryChooser(); 
        directoryChooser.setTitle("");
        File file = directoryChooser.showDialog(null);
        if(file!=null){
        	return file.getPath();
        }
        return null;
	}

	public Event getSelectedEvent() {
		Event event = selectionOptions.getValue();
		System.out.println("selected:" + event);
		return event;
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void renderInitialHandler(List<FileData> rawFiles, List<FileData> processedFiles) {
		Writer writer = new StringWriter();
		Map<String, Object> context = new HashMap<>();
		
		context.put("rawFiles", rawFiles);
		context.put("processedFiles", processedFiles);
		context.put("server_port_url", nifiAddr + ":" + nifiPort + nifiRoute);
		context.put("action_path_id", selectionOptions.getValue().actionPathID);

		try {
			templateService.getTemplate(selectionOptions.getValue().initialHandler).evaluate(writer, context);
		} catch (PebbleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String content = writer.toString();
		WebEngine engine = webView.getEngine();
		engine.loadContent(content);
	}

	public void renderUserInput(UserInputData userInputData) {
		Writer writer = new StringWriter();
		Map<String, Object> context = new HashMap<>();

		context.put("responseData", userInputData.responseData);

		// TODO:
		//	- using `userInputData.responseID`, access the SELECTED EVENT'S `followOnHandlers` JSON object ==> get filename of template
		String followOnTemplate = "follow_on_handler.peb";	// ONLY TEMPORARY

		try {
			templateService.getTemplate(followOnTemplate).evaluate(writer, context);
		} catch (PebbleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String content = writer.toString();
		WebEngine engine = webView.getEngine();
		engine.loadContent(content);
		
	}
}
