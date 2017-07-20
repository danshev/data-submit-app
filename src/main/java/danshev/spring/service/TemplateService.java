package danshev.spring.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

@Service
public class TemplateService implements InitializingBean {
	private PebbleTemplate followOnHandlerTemplate;
	
	PebbleEngine engine = new PebbleEngine.Builder().build();

	@Override
	public void afterPropertiesSet() throws Exception {
		followOnHandlerTemplate = 
				engine.getTemplate("follow_on_handler.peb");
	}
	
	public PebbleTemplate getTemplate(String template) throws PebbleException {
		return engine.getTemplate(template);
	}
	
	public PebbleTemplate getFollowOnHandlerTemplate() {
		return followOnHandlerTemplate;
	}
}
