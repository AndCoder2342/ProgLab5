package manager;

/**
 * трекер глубины вложенности скриптов
 */
public class ScriptDepthTracker {
    private static int currentDepth = 0;
    public static final int MAX_DEPTH = 3;

    /**
     * увеличивает счётчик глубины
     * @return true если глубина не превышена
     */
    public static boolean enterScript() {
        if (currentDepth >= MAX_DEPTH) {
            return false;
        }
        currentDepth++;
        System.out.println("[Глубина рекурсии: " + currentDepth + " из " + MAX_DEPTH + "]");
        return true;
    }

    /**
     * уменьшает счётчик глубины
     */
    public static void exitScript() {
        if (currentDepth > 0) {
            currentDepth--;
        }
    }

    /**
     * возвращает текущую глубину
     */
    public static int getCurrentDepth() {
        return currentDepth;
    }

    /**
     * сбрасывает счётчик
     */
    public static void reset() {
        currentDepth = 0;
    }
}