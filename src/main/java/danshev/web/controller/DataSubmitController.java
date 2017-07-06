package danshev.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import danshev.spring.service.NiFiService;

@Controller
@RequestMapping("/")
public class DataSubmitController {

    @RequestMapping(value = "/folderPath", method = RequestMethod.POST)
    public String folderPathPost(ModelMap model) {

        return "folderPath";
    }

    @RequestMapping(value = "/userinput", method = RequestMethod.POST)
    public String pumpOn(ModelMap model) {

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
