package danshev.util;

import java.io.File;

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
			return filename;
		} else {
			return filename;
		}
	}
	
	public static File getFile(String filename) {
		return new File(getFilename(filename));
	}
}
