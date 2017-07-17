package danshev.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import danshev.spring.service.NiFiService;

@Controller
@RequestMapping("/")
public class DataSubmitController {

    @RequestMapping(value = "/folderPathRaw", method = RequestMethod.POST)
    public String folderPathRawPost(@RequestBody String json) {
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
        return "folderPath";
    }
    
    @RequestMapping(value = "/folderPathProcessed", method = RequestMethod.POST)
    public String folderPathProcessedPost(@RequestBody String json) {
		/*
		 * /folderPathProcessed … w/JSON object:  { “location”: “//path/to/folder” } … such that:
		Read filepath specified by POSTed location parameter
		Recurse through specified filepath, build a processedFiles array (with path and name attributes)
		render selected event’s initialHandler template (found in events.json), passing the previously-stored rawFiles array and processedFiles array

		 */
        return "folderPath";
    }

    @RequestMapping(value = "/userinput", method = RequestMethod.POST)
    public String pumpOn(@RequestBody String json) {
		/*
		 * /userInput … w/JSON object:  { “responseID”: “a1b2c3”, “responseData”: { <json object> } } ... such that:
		Read responseID json value
		Read responseData json
		Render the appropriate template (using the selected event’s followOnHandlers json object + the responseID), passing the responseData json payload
		Loop play a sound / flash window (... ideally, this would continue until the User has focused the app) … ideas?

		 */
        return "";
    }

    /*
     * 
     * The one to /folderPath will just be a json object in the form: {
     * "location": "/path/to/folder" } The GET request is just a simple get
     * request to the ip:port The POST to /userInput will be a json object in
     * the form: { "responseID": "xyz", "responseData": <more json> }
     */

    @Autowired
    private NiFiService nifiService;


}
