package danshev.web.controller;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.FormSubmitData;
import danshev.model.FormSubmitFileData;
import danshev.model.StatusUpdate;
import danshev.model.UserInputData;
import danshev.spring.service.AppService;
import danshev.util.OsUtilities;

@Controller
public class DataSubmitController {
	Gson gson = new Gson();

	@RequestMapping(value = "/folderPathRaw", method = RequestMethod.POST)
	public @ResponseBody String folderPathRawPost(@RequestBody FolderPathData params) {
		System.out.println("Folder path raw called");
		appService.processFolderPath(params);
		System.out.println("Folder path processed");

		File file = new File(params.location);
		if (!file.exists())
			return "INVALID_PATH";

		Event selectedEvent = appService.getSelectedEvent();
		System.out.println("event:" + selectedEvent);
		return "folderPathRaw";
	}

	@RequestMapping(value = "/folderPathProcessed", method = RequestMethod.POST)
	public @ResponseBody String folderPathProcessedPost(@RequestBody FolderPathData params) {

		File file = new File(params.location);
		if (!file.exists())
			return "INVALID_PATH";

		appService.processFolderPath(params);

		return "folderPathProcessed";
	}

	@RequestMapping(value = "/userInput", method = RequestMethod.POST)
	public @ResponseBody String userInput(@RequestBody UserInputData userInputData) {

		System.out.println(userInputData.responseData);
		appService.renderUserInput(userInputData);

		return "";
	}
	@RequestMapping(value = "/formSubmit2", method = RequestMethod.POST)
	public @ResponseBody String formSubmit2(@RequestBody String formData) {
		System.out.println("RECEIVED: '" + formData+ "'");
		return formData;
	}
	
	private Properties getMetadata() {
		Properties props = new Properties();
		InputStream is = null;

		// First try loading from the current directory
		try {
			File f = OsUtilities.getFile("metadata.properties");
			is = new FileInputStream(f);
			props.load(is);
			is.close();
		} catch (Exception e) {
			is = null;
		}
		
		return props;
	}


	@RequestMapping(value = "/formSubmit", method = RequestMethod.POST)
	public @ResponseBody String formSubmit(@RequestBody FormSubmitData formData) {

		String eventUUIDstring = appService.getEventUUID().toString();

		if (formData.nifiEndpoint.endsWith("/contentListener")) {

			List<JsonObject> postedJsons = new ArrayList<>();

			String rawBaseFilePath = null;
			String processedBaseFilePath = null;

			if (!formData.files.isEmpty()) {

				// TODO: Add additional metadata
				//  1. Read key/value pairs from the metadata.properties file
				//  2. Add the key/value pairs to the formData.metadata object

				for (FormSubmitFileData fileData : formData.files) {

					
					String relativeFilePath = "";
					if (fileData.isRaw) {
						if (rawBaseFilePath == null) {
							rawBaseFilePath = fileData.filepath;
						}
						
						relativeFilePath = fileData.filepath.replace(rawBaseFilePath, "");
					} else {
						if (processedBaseFilePath == null) {
							processedBaseFilePath = fileData.filepath;
						}
						
						relativeFilePath = fileData.filepath.replace(processedBaseFilePath, "");
					}
										
					Properties metadata = getMetadata();
					for(Object key : metadata.keySet()) {
						String metadatakey = (String) key;
						formData.metadata.put(metadatakey, metadata.getProperty(metadatakey));
						
					}
					JsonObject json = new JsonObject();
					json.addProperty("metadata", gson.toJson(formData.metadata));	// 3. Send the augmented metadata
					json.addProperty("eventUUID", eventUUIDstring);
					json.addProperty("actionPathID", formData.actionPathId);
					json.addProperty("filePath", fileData.filepath);
					json.addProperty("relativeFilePath", relativeFilePath);
					json.addProperty("fileName", fileData.filename);
					json.addProperty("isRaw", fileData.isRaw);
					json.addProperty("standalone", appService.isStandalone());
					json.addProperty("savePath", OsUtilities.simplifyPath(new File(OsUtilities.getFilename("output/" + eventUUIDstring)).toPath()
							.toAbsolutePath().toString()));
					
					postedJsons.add(json);

					try {
						sendPost("http://" + formData.nifiEndpoint, json);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (appService.isStandalone()) {
					UUID eventUUID = appService.getEventUUID();

					File outFile = OsUtilities.getFile(eventUUID.toString() + ".nifi");
					FileOutputStream fos = null;
					DataOutputStream dos = null;
					try {
						fos = new FileOutputStream(outFile, true);
						dos = new DataOutputStream(fos);

						for (JsonObject json : postedJsons) {
							dos.writeChars(json.toString());
							dos.writeChars(System.lineSeparator());
						}

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (dos != null) {
								dos.close();
							}
							if (fos != null) {
								fos.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		} else {
			JsonObject json = new JsonObject();
			json.addProperty("eventUUID", eventUUIDstring);
			json.addProperty("responseParams", formData.responseParams);

			try {
				sendPost("http://" + formData.nifiEndpoint, json);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return "ok";
	}

	private void sendPost(String url, JsonObject data) throws Exception {

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		// add header
		post.setHeader("User-Agent", "data-submit-app");

		post.setEntity(new StringEntity(gson.toJson(data)));
		post.setHeader("Content-type", "application/json");
		HttpResponse response = client.execute(post);

		System.out.println("Sending POST to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

	}

	@RequestMapping(value = "/getStatic/{filename:.+}", method = RequestMethod.GET)
	public @ResponseBody FileSystemResource getStatic(@PathVariable String filename) {
		FileSystemResource resource = new FileSystemResource(OsUtilities.getFile(filename));
		System.out.println("resource:" + resource);
		return resource;
	}

	@RequestMapping(value = "/statusUpdate", method = RequestMethod.POST)
	public @ResponseBody String statusUpdate(@RequestBody StatusUpdate update) {
		System.out.println("Status update called");
		appService.statusUpdate(update);

		return "statusUpdate";
	}

	@Autowired
	private AppService appService;

}