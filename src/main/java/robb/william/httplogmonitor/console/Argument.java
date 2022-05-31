package robb.william.httplogmonitor.console;

public enum Argument {
    ALERT_THRESHOLD("alertThreshold"),
    AT("at"),
    ALERT_SAMPLE_RATE("alertSampleRate"),
    ASR("asr"),
    STATS_INTERVAL("statsInterval"),
    SI("si"),
    FILE ("file"),
    F("f");

    public final String arg;

    private Argument(String arg) {
        this.arg = arg;
    }
}
