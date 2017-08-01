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

    @RequestMapping(value = "/statusUpdate", method = RequestMethod.POST)
    public @ResponseBody String statusUpdate(@RequestBody StatusUpdateData statusUpdateData) {
        
        appService.renderStatusUpdate(statusUpdateData);

        return "statusUpdate";
    }


	@RequestMapping(value = "/formSubmit", method = RequestMethod.POST)
	public @ResponseBody String formSubmit(@RequestBody String formData) {
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
