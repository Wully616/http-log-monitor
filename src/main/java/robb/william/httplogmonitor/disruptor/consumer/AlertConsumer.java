package robb.william.httplogmonitor.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;

@Component
public class AlertConsumer implements EventHandler<LogEvent> {

    private final Logger logger = LoggerFactory.getLogger(AlertConsumer.class);

    @Override
    public void onEvent(LogEvent logEvent, long l, boolean b) throws Exception {
        //logger.info("#2 sequence: {}, endOfbatch: {}, event: {},", l, b, logEvent.toString());
    }
}
