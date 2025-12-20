package base;

/*
 * Utility for wrapping strings with ANSI color codes based on hex values.
 */
public class AnsiColor {

    private static final String RESET = "\u001B[0m";
    private static final String ANSI_FOREGROUND_RGB = "\u001B[38;2;%d;%d;%dm";
    private static final String HEX_PREFIX = "#";
    private static final int HEX_LENGTH = 7;
    private static final int DARK_THRESHOLD = 80;
    private static final int BRIGHT_TARGET = 80;

    public static String format(String text, String hexColor) {
        if (hexColor == null || !hexColor.startsWith(HEX_PREFIX) || hexColor.length() != HEX_LENGTH) {
            return text; // fallback: plain text
        }

        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        String colorCode = String.format(ANSI_FOREGROUND_RGB, r, g, b);
        return colorCode + text + RESET;
    }

    public static String brightenIfDark(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            if (r <= DARK_THRESHOLD && g <= DARK_THRESHOLD && b <= DARK_THRESHOLD) {
                r = BRIGHT_TARGET;
                g = BRIGHT_TARGET;
                b = BRIGHT_TARGET;
            }

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }
}
