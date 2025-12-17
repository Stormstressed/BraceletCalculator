package base;

/*
 * Utility for wrapping strings with ANSI color codes based on hex values.
 * Keeps presentation logic separate from core logic.
 */
public class AnsiColor {

    private static final String RESET = "\u001B[0m";

    /**
     * Format text with a given hex color.
     *
     * @param text the knot/label to display
     * @param hexColor the hex color string (e.g. "#ff00ff")
     * @return colored string with ANSI codes
     */
    public static String format(String text, String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return text; // fallback: plain text
        }

        // Parse hex into RGB
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        // ANSI 24-bit foreground color
        String colorCode = String.format("\u001B[38;2;%d;%d;%dm", r, g, b);

        return colorCode + text + RESET;
    }
    
    public static String brightenIfDark(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            // Only brighten if it's really close to black (all channels low)
            int threshold = 80; // adjust as needed
            if (r <= threshold && g <= threshold && b <= threshold) {
                // Lift it to a medium gray so it's visible
                int target = 80;
                r = target;
                g = target;
                b = target;
            }

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }

}
