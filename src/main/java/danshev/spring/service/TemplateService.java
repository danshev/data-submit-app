package danshev.spring.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import danshev.util.OsUtilities;

@Service
public class TemplateService implements InitializingBean {
	FileLoader loader = new FileLoader();
	PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();

	@Override
 	public void afterPropertiesSet() throws Exception {

 	}

	public PebbleTemplate getTemplate(String template) throws PebbleException {
		return engine.getTemplate(OsUtilities.getFilename(template));
	}
	
}
