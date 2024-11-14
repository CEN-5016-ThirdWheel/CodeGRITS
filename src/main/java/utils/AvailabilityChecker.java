package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class is used to check the availability of the python environment and the eye-tracking device, and to get the eye tracker name and the available frequencies.
 */
public class AvailabilityChecker {

    private static final String NOT_FOUND = "Not Found";
    private static final String FOUND = "Found";
    private static final String OK = "OK";

    /**
     * Check the availability of the python environment, i.e., whether the required python packages are installed.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return {@code true} if the python environment is available, {@code false} otherwise.
     */
    public static boolean checkPythonEnvironment(String pythonInterpreter) throws IOException, InterruptedException {
        if (pythonInterpreter == null || pythonInterpreter.trim().isEmpty()) {
            throw new IllegalArgumentException("Python interpreter path cannot be null or empty");
        }
        
        String pythonScript = """
                from screeninfo import get_monitors
                import pyautogui
                import time
                import sys
                import math
                                
                print('OK')
                """;

        String line = runPythonScript(pythonInterpreter, pythonScript);
        return OK.equals(line);
    }

    /**
     * Check the availability of the eye-tracking device.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return {@code true} if the eye-tracking device is available, {@code false} otherwise.
     */
    public static boolean checkEyeTracker(String pythonInterpreter) throws IOException, InterruptedException {
        if (pythonInterpreter == null || pythonInterpreter.trim().isEmpty()) {
            throw new IllegalArgumentException("Python interpreter path cannot be null or empty");
        }

        String pythonScript = """
                import tobii_research as tr
                                
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('Not Found')
                else:
                    print('Found')
                """;

        String line = runPythonScript(pythonInterpreter, pythonScript);
        return FOUND.equals(line);
    }

    /**
     * Get the name of the eye-tracking device.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return The name of the eye tracker.
     */
    public static String getEyeTrackerName(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                import tobii_research as tr
                                
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('Not Found')
                else:
                    print(found_eyetrackers[0].device_name)
                """;

        return runPythonScript(pythonInterpreter, pythonScript);
    }

    /**
     * Get the available frequencies of the eye-tracking device.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return The available frequencies of the eye tracker.
     */
    public static List<String> getFrequencies(String pythonInterpreter) throws IOException, InterruptedException {
        if (pythonInterpreter == null || pythonInterpreter.trim().isEmpty()) {
            throw new IllegalArgumentException("Python interpreter path cannot be null or empty");
        }

        String pythonScript = """
                import tobii_research as tr
                                
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('Not Found')
                else:
                    print(found_eyetrackers[0].get_all_gaze_output_frequencies())
                """;
        String resultTuple = runPythonScript(pythonInterpreter, pythonScript);
        if (resultTuple == null || NOT_FOUND.equals(resultTuple)) {
            return List.of();
        }
        
        String cleaned = resultTuple.replaceAll("[()]", "").trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return List.of(cleaned.split(",\\s*"));
    }

    /**
     * Run a python script with {@code ProcessBuilder} and use {@code BufferedReader} to get the first line of the output.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @param pythonScript      The python script to run.
     * @return The first line of the output.
     */
    private static String runPythonScript(String pythonInterpreter, String pythonScript) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true);
        
        try (Process p = pb.start();
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            
            String line = reader.readLine();
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("Python script failed with exit code: " + exitCode);
            }
            
            return line;
        }
    }
}
