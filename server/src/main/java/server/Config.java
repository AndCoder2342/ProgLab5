package server;

/**
 * простая конфигурация для запуска сервера из командной строки
 * Пример: java -jar server.jar --port 1337 --log-level debug
 */
public class Config {
    private int port = 1337;
    private String logLevel = "info";
    private String logFile = "server.log";

    public static Config parse(String[] args) {
        Config config = new Config();
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                config.port = Integer.parseInt(args[i + 1]);
            } else if ("--log-level".equals(args[i]) && i + 1 < args.length) {
                config.logLevel = args[i + 1];
            } else if ("--log-file".equals(args[i]) && i + 1 < args.length) {
                config.logFile = args[i + 1];
            }
        }
        return config;
    }

    public int getPort() { return port; }
    public String getLogLevel() { return logLevel; }
    public String getLogFile() { return logFile; }
}