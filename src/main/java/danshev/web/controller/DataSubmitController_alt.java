package danshev.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
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

	@RequestMapping(value = "/formSubmit", method = RequestMethod.POST)
	public @ResponseBody String formSubmit(@RequestBody FormSubmitData formData) {

		String eventUUIDstring = appService.getEventUUID().toString();

		if (formData.nifiEndpoint.endsWith("/contentListener")) {

			if (!formData.files.isEmpty()) {

				FileWriter outFile = new FileWriter(eventUUIDstring + ".nifi");
				String newLine = System.getProperty("line.separator");

				for (FormSubmitFileData fileData : formData.files) {
					JsonObject json = new JsonObject();
					json.addProperty("eventUUID", eventUUIDstring);
					json.addProperty("actionPathID", formData.actionPathId);
					json.addProperty("filePath", fileData.filepath);
					json.addProperty("fileName", fileData.filename);
					json.addProperty("isRaw", fileData.isRaw);
					json.addProperty("standalone", appService.isStandalone());
					json.addProperty("savePath", new File(OsUtilities.getFilename(eventUUIDstring)).toPath().toAbsolutePath().toString());
					// "savePath": "//folder/to/the/app/output/{{ eventUUID }}" <==== pretty sure the above code does not generate this

					try {
						sendPost("http://" + formData.nifiEndpoint, json);

						if (appService.standalone()) {
							outFile.write(json + newLine);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (appService.standalone()) {
					outFile.close();
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
		System.out.println("Response Code : " +
                                    response.getStatusLine().getStatusCode());

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