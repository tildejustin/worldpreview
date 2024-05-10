package me.voidxwalker.worldpreview;

import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A helper class for outputting the state to help macros handle Minecraft without
 */
public final class StateOutputHelper {
    private static final Path OUT_PATH = Paths.get("wpstateout.txt");
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    // Storage variable, not necessarily involved or required for using outputState()
    public static int loadingProgress = 0;
    public static boolean titleHasEverLoaded = false;
    private static String lastOutput = "";

    private static RandomAccessFile stateFile;


    private StateOutputHelper() {
    }

    public static void outputState(String string) {
        // Prevent "generating,0" from appearing on game start
        if (!titleHasEverLoaded) {
            if (string.equals("title")) titleHasEverLoaded = true;
            else return;
        }

        // Check for changes
        if (lastOutput.equals(string)) {
            return;
        }
        lastOutput = string;

        // Queue up the file writes as to not interrupt mc itself
        EXECUTOR.execute(() -> outputStateInternal(string));
    }

    private static void outputStateInternal(String string) {
        try {
            if(stateFile == null){ // opening file only once is better for performance
                stateFile = new RandomAccessFile(OUT_PATH.toString(), "rw");
            }
            stateFile.setLength(0); // clear existing file contents
            stateFile.seek(0); // move pointer back to start of file
            stateFile.write(string.getBytes(StandardCharsets.UTF_8));
            WorldPreview.log(Level.INFO, "WorldPreview State: " + string);
        } catch (IOException ignored) {
            WorldPreview.log(Level.ERROR, "Failed to write state output!");
        }
    }
}
