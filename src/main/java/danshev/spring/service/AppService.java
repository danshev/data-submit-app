package danshev.spring.service;

import java.util.UUID;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;
import danshev.model.StatusUpdateData;

public interface AppService {

	Event getSelectedEvent();

	void renderUserInput(UserInputData userInputData);

	void processFolderPath(FolderPathData params);

	void renderStatusUpdate(StatusUpdateData statusUpdateData);

	UUID getEventUUID();

}
