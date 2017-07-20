package danshev.web.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;
import danshev.spring.service.NiFiService;

@Controller
public class DataSubmitController {
	Gson gson = new Gson();
	
    @RequestMapping(value = "/folderPathRaw", method = RequestMethod.POST)
    public @ResponseBody String folderPathRawPost(@RequestBody FolderPathData folderPath) {
		/*
		 * /folderPathRaw … w/JSON object:  { “location”: “//path/to/folder”, “processing”: true (false) } … such that:
		Read filepath specified by POSTed location parameter
		Recurse through specified filepath, build a rawFiles array (with path and name attributes)
		If processing parameter = false
		render selected event’s initialHandler template (found in events.json), passing the rawFiles array (you can use test_file_listing.peb for testing)
		If processing parameter = true
		Temporarily store rawFiles array
		Execute initialAction executable (found in events.json)

		 */
    	nifiService.processFolderPathRaw(folderPath);
    	
    	File file = new File(folderPath.location);
    	if(!file.exists()) return "INVALID_PATH";
    	
    	Event selectedEvent = nifiService.getSelectedEvent();
    	System.out.println("event:"+selectedEvent);
        return "folderPathRaw";
    }

	@RequestMapping(value = "/folderPathProcessed", method = RequestMethod.POST)
    public @ResponseBody String folderPathProcessedPost(@RequestBody FolderPathData folderPath) {
		/*
		 * /folderPathProcessed … w/JSON object:  { “location”: “//path/to/folder” } … such that:
		Read filepath specified by POSTed location parameter
		Recurse through specified filepath, build a processedFiles array (with path and name attributes)
		render selected event’s initialHandler template (found in events.json), passing the previously-stored rawFiles array and processedFiles array

		 */
    	File file = new File(folderPath.location);
    	if(!file.exists()) return "INVALID_PATH";
    	
    	nifiService.processFolderPathProcessed(folderPath);

        return "folderPathProcessed";
    }

    @RequestMapping(value = "/userInput", method = RequestMethod.POST)
    public @ResponseBody String userInput(@RequestBody UserInputData userInputData) {
		/*
		 * /userInput … w/JSON object:  { “responseID”: “a1b2c3”, “responseData”: { <json object> } } ... such that:
		Read responseID json value
		Read responseData json
		Render the appropriate template (using the selected event’s followOnHandlers json object + the responseID), passing the responseData json payload
		Loop play a sound / flash window (... ideally, this would continue until the User has focused the app) … ideas?

		 */
    	nifiService.renderUserInput(userInputData);
        return "";
    }

    @Autowired
    private NiFiService nifiService;

}
