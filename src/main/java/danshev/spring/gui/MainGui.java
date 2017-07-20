package danshev.spring.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import danshev.model.FileData;
import danshev.model.FolderPathData;
import danshev.model.UserInputData;
import danshev.spring.service.TemplateService;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
public class MainGui implements InitializingBean {

    @Autowired
    private ScreensController bean;

    @Autowired
    private TemplateService templateService;
    
    private MainGuiController controller;
    
    private List<FileData> rawFiles = new ArrayList<>();
    
    private Stage window;
    
    public void setScreensController(ScreensController controller) {
        this.bean = controller;
    }
    
    public MainGuiController getController() {
    	return controller;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {

        FXMLLoader loader = bean.getLoader("/MainGui.fxml");

        AnchorPane mainPane = null;
        try {
            mainPane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = loader.getController();
        controller.setTemplateService(templateService);
        
        Scene scene = new Scene(mainPane);

        window = new Stage();

        window.setTitle("Data Submit App");
        window.setScene(scene);
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });
        window.show();
    }

    private void addFiles(List<FileData> files, File location) {
		Collection<File> fileList = FileUtils.listFilesAndDirs(location, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		
		for(File file:fileList) {
			if(!file.isDirectory()) {
				files.add(new FileData(file.getParent(), file.getName()));
			}
		}
	}

	public void processFolderPathRaw(FolderPathData params) {
		rawFiles = new ArrayList<>();
		
		addFiles(rawFiles, new File(params.location));

		if(params.processing) {
			// TODO:
			//	- execute file, located at path specified by: `initialAction` value
			System.out.println("==> run initialAction executable");

		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					controller.renderInitialHandler(rawFiles, null);
				}
			}); 
		}
	}

	public void processFolderPathProcessed(FolderPathData params) {
		List<FileData> processedFiles = new ArrayList<>();
		
		addFiles(processedFiles, new File(params.location));

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controller.renderInitialHandler(rawFiles, processedFiles);
			}
		}); 
	}

	public void renderUserInput(UserInputData userInputData) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controller.renderUserInput(userInputData);
				if(!window.isFocused()) {
					window.hide();
					window.show();
				}
			}
		}); 
	}

}
