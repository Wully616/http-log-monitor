package robb.william.httplogmonitor.disruptor.event;

import lombok.Data;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

/*
    This is a holder class for CommonLogFormat objects for within the Disruptor.
    The Disruptor will be preallocated with these objects.
 */
@Data
public final class LogEvent {
    private CommonLogFormat commonLogFormat;
}
