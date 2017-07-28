package danshev.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import danshev.model.Event;
import danshev.model.FolderPathData;
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
    	if(!file.exists()) return "INVALID_PATH";
    	
    	Event selectedEvent = appService.getSelectedEvent();
    	System.out.println("event:"+selectedEvent);
        return "folderPathRaw";
    }

	@RequestMapping(value = "/folderPathProcessed", method = RequestMethod.POST)
    public @ResponseBody String folderPathProcessedPost(@RequestBody FolderPathData params) {

    	File file = new File(params.location);
    	if(!file.exists()) return "INVALID_PATH";
    	
    	appService.processFolderPath(params);

        return "folderPathProcessed";
    }

    @RequestMapping(value = "/userInput", method = RequestMethod.POST)
    public @ResponseBody String userInput(@RequestBody UserInputData userInputData) {
        
        appService.renderUserInput(userInputData);

    	return "userInput";
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


    @Autowired

    private AppService appService;

}
