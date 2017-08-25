package danshev.spring.service;

import java.util.UUID;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.StatusUpdate;
import danshev.model.UserInputData;

public interface AppService {

	Event getSelectedEvent();

	void renderUserInput(UserInputData userInputData);

	void processFolderPath(FolderPathData params);

	UUID getEventUUID();

	void statusUpdate(StatusUpdate update);

	boolean isStandalone();

	String getNifiBaseAddr();

}
