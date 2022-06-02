package robb.william.httplogmonitor.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;

@Component
@ConditionalOnProperty(prefix = "highTrafficAlert", name = "enabled", havingValue = "true")
public class HighTrafficAlertConsumer implements EventHandler<LogEvent> {

    private final Logger logger = LoggerFactory.getLogger(HighTrafficAlertConsumer.class);

    //Store traffic for X seconds in a circular buffer
    //Each array index is a 1 second bucket
    private final int[] circularBuffer;
    private final int bufferSize;

    int alertHitsPerSecond;
    int head = 0;
    int tail = 0;
    long headTimestamp = -1;
    int movingSum = 0;
    boolean alertActive = false;

    public HighTrafficAlertConsumer(@Value("${highTrafficAlert.sampleWindow}") int sampleWindow,
                                    @Value("${highTrafficAlert.threshold}") int threshold) {
        this.bufferSize = sampleWindow;
        this.alertHitsPerSecond = threshold;
        this.circularBuffer = new int[bufferSize];

    }

    @Override
    public void onEvent(LogEvent logEvent, long l, boolean b) throws Exception {
        //logger.info("#2 sequence: {}, endOfbatch: {}, event: {},", l, b, logEvent.toString());
        long date = logEvent.getLogLine().getDate();

        moveWindow(date);
        addToWindow(date);
        checkAlert();
    }


    private void checkAlert(){
        //calculate the window average
        int movingAvg = movingSum / bufferSize;
        //logger.info("date: {}, avg: {}, headTimestamp: {}, tailTimestamp: {}, head: {}, tail: {}", date, movingAvg, headTimestamp, headTimestamp-120,head,tail);

        if(!alertActive && movingAvg >= alertHitsPerSecond){
            logger.info("High traffic generated an alert - hits = {}, triggered at {}", movingAvg, headTimestamp);
            alertActive = true;
        }

        if(alertActive && movingAvg < alertHitsPerSecond){
            logger.info("Recovered from high traffic - hits = {}, recovered at {}", movingAvg, headTimestamp);
            alertActive = false;
        }
    }

    private void addToWindow(long date){
        // increment an existing element in the window
        if(date >= headTimestamp - bufferSize && date < headTimestamp + 1){
            //this record is within the current window

            //increment sum, not removing anything yet since the window hasn't moved
            movingSum += 1;

            //get the element
            int index = (int)(date % bufferSize);
            //increment the element in the window
            circularBuffer[index] += 1;
        }
    }

    private void moveWindow(long date){
        // shift the window if we got newer data coming in
        if(date > headTimestamp){
            //remove the current tail value
            movingSum -= circularBuffer[tail];

            //shift the window
            headTimestamp = date;
            head = (int)(date % bufferSize);
            //reset the value at the head
            circularBuffer[head] = 0;

            //tail is always head + 1, looped around since its circular
            tail = (head + 1) % bufferSize;

        }
    }

}
