package utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for calculating the relative path of a file compared to a project path.
 */
public class RelativePathGetter {

    /**
     * Calculates the relative path of a file compared to the project path. If the project path is not a prefix of the file path,
     * the absolute path of the file is returned.
     *
     * @param absolutePath The absolute path of the file. Must not be null.
     * @param projectPath  The absolute path of the project. Must not be null.
     * @return The relative path of the file compared to the project path, or the absolute path if the project path is not a prefix.
     * @throws IllegalArgumentException if either argument is null or invalid.
     */
    public static String getRelativePath(String absolutePath, String projectPath) {
        if (absolutePath == null || projectPath == null) {
            throw new IllegalArgumentException("Paths must not be null.");
        }

        try {
            Path absolute = Paths.get(absolutePath).normalize();
            Path project = Paths.get(projectPath).normalize();

            if (absolute.startsWith(project)) {
                return project.relativize(absolute).toString();
            }

            return absolute.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path input: " + e.getMessage(), e);
        }
    }
}
