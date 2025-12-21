package base;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class to save and load Pattern objects locally as JSON.
 * Stores all patterns in a single file (patterns.json) keyed by ID.
 */
public class PatternStorage {
    private static final String STORAGE_FILE = "patterns.json";
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // In-memory cache of all patterns
    private static Map<String, Pattern> cache = new HashMap<>();

    static {
        // Load existing file if present
        try {
            File f = new File(STORAGE_FILE);
            if (f.exists()) {
                cache = mapper.readValue(f,
                        mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Pattern.class));
            }
        } catch (IOException e) {
            System.err.println("Warning: could not load patterns.json: " + e.getMessage());
        }
    }

    public static void savePattern(Pattern pattern, String id) throws IOException {
        cache.put(id, pattern);
        mapper.writeValue(new File(STORAGE_FILE), cache);
    }

    public static Pattern loadPattern(String id) throws IOException {
        return cache.get(id);
    }
    
    public static void deletePattern(String id) throws IOException {
        if (cache.remove(id) != null) {
            // Rewrite the entire JSON file with the updated cache
            mapper.writeValue(new File(STORAGE_FILE), cache);
        }
    }

    public static Set<String> getSavedIds() {
        return cache.keySet(); // keySet is a Set<String>
    }
}
