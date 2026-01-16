package bhaskar.aethershell.hub.controller;

import bhaskar.aethershell.hub.service.PythonBridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/session")
public class DsController {

    @Autowired
    private PythonBridgeService pythonBridge;

    private final ConcurrentHashMap<String, List<String>> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> sessionResults = new ConcurrentHashMap<>();

    // Volatile ensures the ID is visible across port 8080 and 8443 instantly
    private volatile String currentSessionId = null;

    /**
     * 1. START SESSION: Triggered by (Y) on DS
     * Wipes the local PNG storage to keep the AR experience fresh.
     */
    @GetMapping("/new")
    public String startSession() {
        // Run the cleanup before creating a new session
        clearOutputFolder();

        currentSessionId = UUID.randomUUID().toString().substring(0, 8);
        activeSessions.put(currentSessionId, new ArrayList<>());

        System.out.println("\n--- NEW SESSION INITIALIZED & STORAGE CLEANED ---");
        System.out.println("ID: " + currentSessionId);
        System.out.println("------------------------------------------------\n");

        return currentSessionId;
    }

    /**
     * 2. RECEIVE FRAME: Triggered by (A) on DS
     */
    @PostMapping("/frame")
    public String receiveFrame(@RequestBody String coordinateString) {
        if (currentSessionId == null) {
            return "Error: Start session first.";
        }

        String data = coordinateString.trim();
        if (data.isEmpty()) return "Error: Empty frame.";

        activeSessions.get(currentSessionId).add(data);

        System.out.println("[" + currentSessionId + "] Frame " + activeSessions.get(currentSessionId).size() + " received.");
        return "Frame Stored";
    }

    /**
     * 3. FINALIZE & PROCESS: Triggered by (X) on DS
     */
    @GetMapping("/done")
    public Map<String, Object> finalizeSession() {
        if (currentSessionId == null || !activeSessions.containsKey(currentSessionId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No active session found");
            return error;
        }

        List<String> allFrames = activeSessions.get(currentSessionId);
        System.out.println("\n--- TRIGGERING TRIPLE-GATE WORKER (Port 5001) ---");

        Map<String, Object> result = pythonBridge.callPythonWorker(currentSessionId, allFrames);

        if (result != null) {
            sessionResults.put(currentSessionId, result);
            System.out.println(" AI Interpretation: " + result.get("ai_description"));
        } else {
            System.out.println(" Python Bridge failed to return a result.");
        }

        return result;
    }

    /**
     * 4. IPHONE ENDPOINT: Polled by iOS AR App
     */
    @GetMapping("/results/{id}")
    public Map<String, Object> getResults(@PathVariable String id) {
        return sessionResults.getOrDefault(id, Collections.singletonMap("status", "processing"));
    }

    /**
     * HELPER: Recursive cleanup of the static output directory.
     * Keeps the folder but deletes all session images.
     */
    private void clearOutputFolder() {
        try {
            String rootPath = System.getProperty("user.dir");

            // Check if we are already in the hub directory or the parent
            String subPath = rootPath.endsWith("hub")
                    ? "/src/main/resources/static/output/"
                    : "/hub/src/main/resources/static/output/";

            File outputDir = new File(rootPath + subPath);

            if (outputDir.exists() && outputDir.isDirectory()) {
                File[] files = outputDir.listFiles();
                if (files != null) {
                    int count = 0;
                    for (File file : files) {
                        // Only delete the images, keep the directory
                        if (!file.isDirectory() && file.getName().endsWith(".png")) {
                            if (file.delete()) count++;
                        }
                    }
                    System.out.println("Cleanup Success: Deleted " + count + " old frames.");
                }
            } else {
                System.err.println("Cleanup Error: Directory not found at " + outputDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Critical Cleanup Error: " + e.getMessage());
        }
    }
}