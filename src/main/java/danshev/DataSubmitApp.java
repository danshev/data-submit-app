package danshev;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import danshev.spring.server.WebServer;
import javafx.application.Application;
import javafx.stage.Stage;

public class DataSubmitApp extends Application {
    private static final Log LOG = LogFactory.getLog(DataSubmitApp.class);

    public static void main(String[] args) throws Exception {
    	launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        DataSubmitApp app = new DataSubmitApp();

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:/applicationContext.xml");

        ctx.getBeanFactory().autowireBeanProperties(app,
                AutowireCapableBeanFactory.AUTOWIRE_NO, false);

        LOG.info("Starting...");

        ctx.registerShutdownHook();

        if (app.webServer.enabled()) {
            app.webServer.start();
        }

    }

    @Autowired
    private WebServer webServer;

    public void setWebServer(WebServer server) {
        this.webServer = server;

    }

}
