package danshev.spring.service;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;
import danshev.spring.gui.MainGui;

@Service("appService")
public class AppServiceImpl implements AppService {
	@Autowired private MainGui mainGui;
	
    private static final Log LOG = LogFactory.getLog(AppServiceImpl.class);

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
			System.out.println("Processing");
			mainGui.processFolderPathRaw(params);
		} else {
        	mainGui.processFolderPathProcessed(params);
    	}
	}

	@Override
	public UUID getEventUUID() {
		return mainGui.getEventUUID();
	}

}
