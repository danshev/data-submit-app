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
    public @ResponseBody String folderPathRawPost(@RequestBody FolderPathData params) {

    	nifiService.processFolderPath(params);
    	
    	File file = new File(params.location);
    	if(!file.exists()) return "INVALID_PATH";
    	
    	Event selectedEvent = nifiService.getSelectedEvent();
    	// System.out.println("event:"+selectedEvent);
        return "folderPathRaw";
    }

	@RequestMapping(value = "/folderPathProcessed", method = RequestMethod.POST)
    public @ResponseBody String folderPathProcessedPost(@RequestBody FolderPathData params) {

    	File file = new File(folderPath.location);
    	if(!file.exists()) return "INVALID_PATH";
    	
    	nifiService.processFolderPath(params);

        return "folderPathProcessed";
    }

    @RequestMapping(value = "/userInput", method = RequestMethod.POST)
    public @ResponseBody String userInput(@RequestBody UserInputData userInputData) {
    	
        System.out.println(userInputData.responseData);

        nifiService.renderUserInput(userInputData);

    	// TODO: check this syntax
    	return ""; //ResponseEntity.ok();
    }

/*
    @RequestMapping(value = "/formSubmit", method = RequestMethod.POST)
    public @ResponseBody String formSubmit(@RequestBody FormData formData) {
        
        // TODO (Sprint 3)
        //  - write out submitted data in a file named `{{ uuid }}.nifi`, where UUID is the string generated when the User selected an item from the dropdown

        return ResponseEntity.ok();
    }
*/

    @Autowired
    private NiFiService nifiService;

}
