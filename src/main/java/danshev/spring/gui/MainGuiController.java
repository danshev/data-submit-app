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
import java.util.UUID;

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
import danshev.model.StatusUpdate;
import danshev.model.UserInputData;
import danshev.spring.service.TemplateService;
import danshev.util.OsUtilities;
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

	private UUID eventUUID;
	private Integer myPort;
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
			String eventsJson = Files.toString(OsUtilities.getFile("events.json"), Charset.defaultCharset());
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

	public UUID getEventUUID() {
		return this.eventUUID;
  	}

	protected void checkNiFi() {
		try {
			URL url = new URL("http://" + nifiAddr + ":" + Integer.toString(nifiPort) + nifiRoute);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			int code = connection.getResponseCode();
			setLabel("Connected to NiFi server at " + nifiAddr + ":" + nifiPort + nifiRoute, code == 405);
			nifiReachable = true;

		} catch (Exception e) {
			setLabel("NiFi server unreachable", false);
			nifiReachable = false;

			//  Essentially, prevent the User from doing anything, so ...
			
			// ... hide the folder picker
			selectFolderButton.setVisible(false);
			
			// ... blank the browser area
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					webView.getEngine().loadContent("");
				}
			});
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
			File f = OsUtilities.getFile("config.properties");
			is = new FileInputStream(f);
			props.load(is);
			is.close();
		} catch (Exception e) {
			is = null;
		}

//		myPort = new Integer(props.getProperty("status.port", "8998"));
		myPort = 8998;
		standaloneMode = new Boolean(props.getProperty("status.standalone"));
		nifiAddr = props.getProperty("nifi.ip", "127.0.0.1");
		nifiPort = new Integer(props.getProperty("nifi.port", "8080"));
		nifiRoute = props.getProperty("nifi.route", "/contentListener");
	}

	@FXML
	private void selectOption(ActionEvent event) {
		if(nifiReachable){
			selectFolderButton.setVisible(true);
			eventUUID = UUID.randomUUID();
			System.out.println(eventUUID);
		};
	}

	@FXML
	private void selectFolder(ActionEvent event) {
		FolderPathData params = new FolderPathData();

		params.location = selectFolder();
		params.processing = getSelectedEvent().initialAction != null;
		
		try {
			String postUrl = "http://127.0.0.1:" + myPort + "/folderPathRaw";
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
		context.put("action_path_id", getSelectedEvent().actionPathID);

		try {
			templateService.getTemplate(getSelectedEvent().initialHandler).evaluate(writer, context);
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

		String followOnTemplate = getSelectedEvent().followOnHandlers.get(userInputData.responseID).toString();
		context.put("responseData", userInputData.responseData);
		
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

	public void renderStatusUpdate(List<FileData> rawFiles, List<FileData> processedFiles,
			Map<String, List<StatusUpdate>> statusUpdates) {
		Writer writer = new StringWriter();
		Map<String, Object> context = new HashMap<>();
		context.put("rawFiles", rawFiles);
		context.put("processedFiles", processedFiles);
		context.put("updates", statusUpdates);

		try {
			templateService.getTemplate(OsUtilities.getFilename("status.peb")).evaluate(writer, context);
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
