package base;

public final class Debug {

    public static boolean enabled = false;

    public static void log(String msg) {
        if (enabled) {
            System.out.println(msg);
        }
    }
}
