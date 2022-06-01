package robb.william.httplogmonitor.disruptor.event;

import robb.william.httplogmonitor.reader.model.LogLine;

//This is a holder class for logLine objects for within the Disruptor.
//The Disruptor will be preallocated with these objects.
public final class LogEvent {

    private LogLine logLine;

    public LogLine getLogLine() {
        return logLine;
    }

    public void setLogLine(LogLine logLine) {
        this.logLine = logLine;
    }

    public LogEvent() {
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "logLine=" + logLine +
                '}';
    }
}
