package danshev.model;

import danshev.FollowOnHandlers;

public class Event {
    public String actionPathID;
    public String name;
    public String initialAction;
    public String initialHandler;
    public FollowOnHandlers followOnHandlers;

    @Override
    public String toString() {
        return name;
    }
}
