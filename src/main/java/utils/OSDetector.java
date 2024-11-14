package utils;

/**
 * This class is used to detect the operating system.
 */
public class OSDetector {
    /**
     * The operating system name.
     */
    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private OSDetector() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Get the operating system name.
     *
     * @return The operating system name in lowercase.
     */
    public static String getOSName() {
        return OS;
    }

    /**
     * Check if the operating system is Windows.
     *
     * @return {@code true} if the operating system is Windows, {@code false} otherwise.
     */
    public static boolean isWindows() {
        return OS.contains("win");
    }

    /**
     * Check if the operating system is Mac.
     *
     * @return {@code true} if the operating system is Mac, {@code false} otherwise.
     */
    public static boolean isMac() {
        return OS.contains("mac");
    }

    /**
     * Check if the operating system is Unix.
     *
     * @return {@code true} if the operating system is Unix, {@code false} otherwise.
     */
    public static boolean isUnix() {
        return OS.contains("nix") || 
               OS.contains("nux") || 
               OS.contains("aix") || 
               OS.contains("sunos");
    }

    /**
     * Get the operating system type.
     *
     * @return The {@link OSType} of the current system.
     */
    public static OSType getOSType() {
        if (isWindows()) return OSType.WINDOWS;
        if (isMac()) return OSType.MAC;
        if (isUnix()) return OSType.UNIX;
        return OSType.UNKNOWN;
    }
}

/**
 * Enum representing different operating system types.
 */
enum OSType {
    WINDOWS,
    MAC,
    UNIX,
    UNKNOWN
}
