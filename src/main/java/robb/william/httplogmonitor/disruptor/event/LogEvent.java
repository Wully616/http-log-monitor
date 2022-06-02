package robb.william.httplogmonitor.disruptor.event;

import robb.william.httplogmonitor.reader.model.CommonLogFormat;

/*
    This is a holder class for CommonLogFormat objects for within the Disruptor.
    The Disruptor will be preallocated with these objects.
 */
public final class LogEvent {

    private CommonLogFormat commonLogFormat;

    public CommonLogFormat getLogLine() {
        return commonLogFormat;
    }

    public void setLogLine(CommonLogFormat commonLogFormat) {
        this.commonLogFormat = commonLogFormat;
    }

    public LogEvent() {
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "commonLogFormat=" + commonLogFormat +
                '}';
    }
}
