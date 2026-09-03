package manager;


public class ScriptDepthTracker {
    private static int currentDepth = 0;
    public static final int MAX_DEPTH = 3;


    public static boolean enterScript() {
        if (currentDepth >= MAX_DEPTH) {
            return false;
        }
        currentDepth++;
        System.out.println("[Глубина рекурсии: " + currentDepth + " из " + MAX_DEPTH + "]");
        return true;
    }


    public static void exitScript() {
        if (currentDepth > 0) {
            currentDepth--;
        }
    }


    public static int getCurrentDepth() {
        return currentDepth;
    }


    public static void reset() {
        currentDepth = 0;
    }
}