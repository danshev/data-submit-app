package danshev.spring.service;

import danshev.model.Event;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;

public interface NiFiService {

	Event getSelectedEvent();

	void renderUserInput(UserInputData userInputData);

	void processFolderPathRaw(FolderPathData folderPath);

	void processFolderPathProcessed(FolderPathData folderPath);

}
