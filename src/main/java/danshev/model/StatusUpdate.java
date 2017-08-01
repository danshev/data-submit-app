package danshev.model;

import java.io.Serializable;

public class StatusUpdate implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String filename;
    public Boolean success;
    public String text;

}
