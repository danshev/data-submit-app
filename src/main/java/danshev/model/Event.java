package danshev.model;

import java.io.Serializable;
import java.util.Map;

import danshev.FollowOnHandlers;

public class Event implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String actionPathID;
    public String name;
    public String initialAction;
    public String initialHandler;
    public Map<String, String> followOnHandlers;

    @Override
    public String toString() {
        return name;
    }
}
