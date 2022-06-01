package robb.william.httplogmonitor.disruptor.buffer;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;

@Component
public class LogEventBuffer {

    private final Logger logger = LoggerFactory.getLogger(LogEventBuffer.class);

    private final Disruptor<LogEvent> logEventDisruptor;
    private final EventHandler<LogEvent>[] logEventConsumers;


    public LogEventBuffer(EventHandler<LogEvent>[] logEventConsumers) {
        this.logEventConsumers = logEventConsumers;
        this.logEventDisruptor = createLogEventDisruptor();
    }

    public RingBuffer<LogEvent> getRingBuffer(){
        return logEventDisruptor.getRingBuffer();
    }

    public Disruptor<LogEvent> createLogEventDisruptor(){
        logger.info("Creating log event disruptor");
        int bufferSize = 64;
        Disruptor<LogEvent> disruptor =
                new Disruptor<>(LogEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);

        //Spring will find all of our logEventConsumers, we add them as consumers to the disruptor
        //By default these consumers will process the events in parallel.
        logger.info("Adding {} consumers to the log event disruptor", logEventConsumers.length);
        disruptor.handleEventsWith(logEventConsumers);

        logger.info("Starting log event disruptor..");
        disruptor.start();

        return disruptor;
    }
}
