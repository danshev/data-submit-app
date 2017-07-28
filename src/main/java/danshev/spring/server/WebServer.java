package danshev.spring.server;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
//@PropertySource("file:config.properties")
public class WebServer {
    private static final Log LOG = LogFactory.getLog(WebServer.class);

    private static final String LOG_PATH = "./var/logs/access/yyyy_mm_dd.request.log";

    private static final String WEB_XML = "WEB-INF/web.xml";
    private static final String CLASS_ONLY_AVAILABLE_IN_IDE = "com.sjl.IDE";
    private static final String PROJECT_RELATIVE_PATH_TO_WEBAPP = "src/main/java/META-INF/webapp";

    public static interface WebContext {
        public File getWarPath();

        public String getContextPath();
    }

    private Server server;
    private String bindInterface = null;

    public void start() throws Exception {
        server = new Server();

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers());
        server.setStopAtShutdown(true);

        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private ThreadPool createThreadPool() {
        QueuedThreadPool _threadPool = new QueuedThreadPool();
        _threadPool.setMinThreads(10);
        _threadPool.setMaxThreads(100);
        return _threadPool;
    }

    public boolean enabled() {
//        String enabled = environment.getProperty("status.enabled");
        String enabled = "1";
    	if (enabled != null && enabled.equals("1")) {
            return true;
        }
        return false;
    }

    private SelectChannelConnector createConnector() {
        SelectChannelConnector _connector = new SelectChannelConnector();
//        _connector.setPort(
//                Integer.parseInt(environment.getProperty("status.port")));
        _connector.setPort(8998);
        _connector.setHost(bindInterface);
        return _connector;
    }

    private HandlerCollection createHandlers() {
        WebAppContext _ctx = new WebAppContext();
        _ctx.setContextPath("/");

        if (isRunningInShadedJar()) {
            _ctx.setWar(getShadedWarUrl());
        } else {
            _ctx.setWar(PROJECT_RELATIVE_PATH_TO_WEBAPP);
        }

        List<Handler> _handlers = new ArrayList<>();

        _handlers.add(_ctx);

        HandlerList _contexts = new HandlerList();
        _contexts.setHandlers(_handlers.toArray(new Handler[0]));

        RequestLogHandler _log = new RequestLogHandler();
        _log.setRequestLog(createRequestLog());

        HandlerCollection _result = new HandlerCollection();
        _result.setHandlers(new Handler[] { _contexts, _log });

        return _result;
    }

    private RequestLog createRequestLog() {
        NCSARequestLog _log = new NCSARequestLog();

        File _logPath = new File(LOG_PATH);
        _logPath.getParentFile().mkdirs();

        _log.setFilename(_logPath.getPath());
        _log.setRetainDays(90);
        _log.setExtended(false);
        _log.setAppend(true);
        _log.setLogTimeZone("GMT");
        _log.setLogLatency(true);
        return _log;
    }

    private boolean isRunningInShadedJar() {
        try {
            Class.forName(CLASS_ONLY_AVAILABLE_IN_IDE);
            return false;
        } catch (ClassNotFoundException anExc) {
            return true;
        }
    }

    private URL getResource(String aResource) {
        return Thread.currentThread().getContextClassLoader()
                .getResource(aResource);
    }

    private String getShadedWarUrl() {
        String _urlStr = getResource(WEB_XML).toString();
        // Strip off "WEB-INF/web.xml"
        return _urlStr.substring(0, _urlStr.length() - 15);
    }

    @Autowired
    private Environment environment;

    public void setEnvironment(Environment env) {
        this.environment = env;
    }
}