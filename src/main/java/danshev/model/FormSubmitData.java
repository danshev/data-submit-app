package danshev.model;

import java.util.List;
import java.util.Map;

public class FormSubmitData {
	public String nifiEndpoint;
	public String actionPathId;
	public Map<String, String> metadata;
	public List<FormSubmitFileData> files;
	public String responseParams;
}
