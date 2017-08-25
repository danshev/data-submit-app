package danshev.util;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class OsUtilities {
	private static String getOSName() {
		String name = System.getProperty("os.name");

		if (name == null) {
			name = "Unknown";
		}

		return name;
	}
	
	public static boolean isOSLinux() {
		String os = getOSName();
		return (os != null && os.toLowerCase().startsWith("linux"));
	}

	public static boolean isOSMac() {
		String os = getOSName();
		return (os != null && os.toLowerCase().startsWith("mac"));
	}

	public static boolean isOSWindows() {
		String os = getOSName();
		return (os != null && os.toLowerCase().startsWith("windows"));
	}
	
	public static String getFilename(String filename) {
		if(isOSMac()) {
			return "../../../" + filename;
		} else if(isOSWindows()) {
			return "./" +filename;
		} else {
			return filename;
		}
	}
	
	public static String simplifyPath(String path) {
	    Deque<String> pathDeterminer = new ArrayDeque<String>();
	    String[] pathSplitter = path.split("/");
	    StringBuilder absolutePath = new StringBuilder();
	    for(String term : pathSplitter){
	        if(term == null || term.length() == 0 || term.equals(".")){
	            /*ignore these guys*/
	        }else if(term.equals("..")){
	            if(pathDeterminer.size() > 0){
	                pathDeterminer.removeLast();
	            }
	        }else{
	            pathDeterminer.addLast(term);
	        }
	    }
	    if(pathDeterminer.isEmpty()){
	        return "/";
	    }
	    while(! pathDeterminer.isEmpty()){
	        absolutePath.insert(0, pathDeterminer.removeLast());
	        absolutePath.insert(0, "/");
	    }
	    return absolutePath.toString();
	 }
	
	public static File getFile(String filename) {
		return new File(getFilename(filename));
	}
}
