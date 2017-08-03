package danshev.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.StatusUpdate;
import danshev.model.UserInputData;
import danshev.model.StatusUpdateData;
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
	public @ResponseBody String formSubmit(@RequestBody String formData) {
		

	// TODO: take submitted data (formData -- will be in JSON), convert to a series of HTTP POSTS of JSON data

		/* 

		*** INCOMING ***

		formData structure:

		  {
			"nifi_endpoint": "server:port/URL to which to perform the HTTP POST(s)",		// ALWAYS present

			"action_path_id": "a specific ID",												// may (or may not) be present
			
			"metadata": { 																	// may (or may not) be present
				"key": "value" 
			},

			"files": [																		// may (or may not) be present
				{ 
					"filepath": "//folder/to/the/", 
					"filename": "file.ext",
					"is_raw": true (or false)
				},
				...
			],

			"response_params": {															// may not (or may) be present
				<< anything >>
			}
		  }
		
		*** OUTGOING ***

		  -- If the URL of `nifi_endpoint` == "/contentListener", then ...

			1.  For each file in the formData payload object, build an outgoing HTTP POST like this:
				  
				  curl -X POST -d {{ JSON object *below* }} {{ nifi_endpoint }}
					{
						"eventUUID": getEventUUID(),				// supplied here by `getEventUUID()`
						"actionPathID": {{ action_path_id }},		// supplied in formData payload
						"filePath": "//folder/to/the/", 			// supplied in formData payload
						"fileName": "file.ext", 					// supplied in formData payload
						"isRaw": true (or false)					// supplied in formData payload
						"standalone": true (or false)				// supplied here by `isStandalone()`
						"savePath": "//folder/to/this/app/output/{{ eventUUID }}"		// supplied here by `new File(OsUtilities.getFilename("")).toPath().toAbsolutePath()`
					}

			2.  If (appService.isStandalone()) ... append the JSON object (above) to the next line of a `eventUUID.nifi` file, saved in a folder:  //folder/to/this/app/output/{{ eventUUID }}/

				For example, after processing 3 files from the formData payload, `013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5.nifi` might look like this:
					
					{ "eventUUID": "013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5", "actionPathID": "abc", "filePath": "//folder/to/the/", "fileName": "f1.txt", "isRaw": true, "standalone": false, "savePath": "//Users/dss0111/Downloads/app/output/013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5" }
					{ "eventUUID": "013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5", "actionPathID": "abc", "filePath": "//folder/to/the/", "fileName": "f2.txt", "isRaw": true, "standalone": false, "savePath": "//Users/dss0111/Downloads/app/output/013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5" }
					{ "eventUUID": "013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5", "actionPathID": "abc", "filePath": "//folder/to/the/", "fileName": "f3.txt", "isRaw": true, "standalone": false, "savePath": "//Users/dss0111/Downloads/app/output/013a1e6d-0d82-4ea0-81a2-e23b8c1c58f5" }

				... yes, the filename is the only thing changing, but this is useful for one of my use-cases.  Again, this UUID.nifi file should be stored in //folder/to/this/app/output/{{ eventUUID }}/




		  -- If the URL of `nifi_endpoint` != "/contentListener", then ...

			1.  Perform one HTTP POST, with structure:
				  
				  curl -X POST -d {{ JSON object *below* }} {{ nifi_endpoint }}
					{
						"eventUUID": getEventUUID(),				// supplied here by `getEventUUID()`
						"responseParams": {{ response_params }}		// supplied in formData payload
					}
		*/
		
		if (appService.isStandalone()) {
			UUID eventUUID = appService.getEventUUID();

			File outFile = OsUtilities.getFile(eventUUID.toString() + ".nifi");
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(outFile);
				fos.write(formData.toString().getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        //  - write out submitted data in a file named `{{ uuid }}.nifi`, where UUID is the string generated when the User selected an item from the dropdown
	        catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return "ok";
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
