package robb.william.httplogmonitor.disruptor.buffer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

@Component
public class LogEventBuffer {

    private static final Logger logger = LoggerFactory.getLogger(LogEventBuffer.class);

    private final Disruptor<LogEvent> logEventDisruptor;
    private final EventHandler<LogEvent>[] logEventConsumers;

    private final int bufferSize;

    public LogEventBuffer(EventHandler<LogEvent>[] logEventConsumers, @Value("${eventBuffer.size}") int bufferSize) {
        this.bufferSize = Math.max(bufferSize, 2); //ensure a minimum size of 2 for the buffer to work.
        this.logEventConsumers = logEventConsumers;
        this.logEventDisruptor = createLogEventDisruptor();
    }

    public Disruptor<LogEvent> createLogEventDisruptor() {
        logger.info("Creating log event disruptor");
        Disruptor<LogEvent> disruptor = new Disruptor<>(
                LogEvent::new,
                // Should be a multiple of 2
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                // Since we use a single producer (our log reader), this is set for increased performance
                ProducerType.SINGLE,
                // Blocks when waiting for more data, could be changed to reduce latency at the cost of increased cpu
                new BlockingWaitStrategy());

        //Spring will find all of our logEventConsumers, we add them as consumers to the disruptor
        //By default these consumers will process the events in parallel.
        if (logEventConsumers == null || logEventConsumers.length == 0) {
            logger.info("No initial consumers provided to the log event disruptor, not starting");
            return disruptor;
        } else {
            logger.info("Adding {} consumers to the log event disruptor", logEventConsumers.length);
            disruptor.handleEventsWith(logEventConsumers);
        }

        logger.info("Starting log event disruptor..");
        disruptor.start();
        logger.info("Started log event disruptor");

        return disruptor;
    }

    public void publish(CommonLogFormat log) {
        logEventDisruptor.publishEvent(((logEvent, l) -> logEvent.setCommonLogFormat(log)));
    }

    public RingBuffer<LogEvent> getRingBuffer() {
        return logEventDisruptor.getRingBuffer();
    }

    public Disruptor<LogEvent> getLogEventDisruptor() {
        return logEventDisruptor;
    }

    public EventHandler<LogEvent>[] getLogEventConsumers() {
        return logEventConsumers;
    }
}
