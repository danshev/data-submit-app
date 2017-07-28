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
import danshev.model.StatusUpdateData;

import danshev.spring.service.NiFiService;

@Controller
public class DataSubmitController {
	Gson gson = new Gson();
	
    @RequestMapping(value = "/folderPathRaw", method = RequestMethod.POST)
    public @ResponseBody String folderPathRawPost(@RequestBody FolderPathData params) {
    	
    	File file = new File(params.location);
    	if(!file.exists()) return "INVALID_PATH";

        nifiService.processFolderPath(params);

        return "folderPathRaw";
    }

	@RequestMapping(value = "/folderPathProcessed", method = RequestMethod.POST)
    public @ResponseBody String folderPathProcessedPost(@RequestBody FolderPathData params) {

    	File file = new File(params.location);
        if(!file.exists()) return "INVALID_PATH";

        nifiService.processFolderPath(params);

        return "folderPathProcessed";
    }

    @RequestMapping(value = "/userInput", method = RequestMethod.POST)
    public @ResponseBody String userInput(@RequestBody UserInputData userInputData) {
        
        nifiService.renderUserInput(userInputData);

        System.out.println(userInputData.responseID);
        System.out.println(userInputData.responseData);

    	return "userInput";
    }

    @RequestMapping(value = "/statusUpdate", method = RequestMethod.POST)
    public @ResponseBody String statusUpdate(@RequestBody StatusUpdateData statusUpdateData) {
        
        nifiService.renderStatusUpdate(statusUpdateData);

        return "statusUpdate";
    }

/*
    @RequestMapping(value = "/formSubmit", method = RequestMethod.POST)
    public @ResponseBody String formSubmit(@RequestBody FormData formData) {
        
        // TODO (Sprint 3)
        if(standalone) {
            save POSTed formData to ./uuid.nifi
        }

        return ResponseEntity.ok();
    }
*/

    @Autowired
    private NiFiService nifiService;
}
