package danshev.spring.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;
import danshev.spring.gui.MainGui;

@Service("nifiService")
public class NiFiServiceImpl implements NiFiService {
	@Autowired private MainGui mainGui;
	
    private static final Log LOG = LogFactory.getLog(NiFiServiceImpl.class);

	@Override
	public Event getSelectedEvent() {
		return mainGui.getController().getSelectedEvent();
	}

	@Override
	public void renderUserInput(UserInputData userInputData) {
		mainGui.renderUserInput(userInputData);
	}

	@Override
	public void processFolderPath(FolderPathData params) {

		if (params.processing != null) {
			mainGui.processFolderPathRaw(params);
		} else {
        	mainGui.processFolderPathProcessed(params);
    	}
	}

}
