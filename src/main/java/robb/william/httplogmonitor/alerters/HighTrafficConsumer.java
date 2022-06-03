package robb.william.httplogmonitor.alerters;

import com.lmax.disruptor.EventHandler;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

@Component
@ConditionalOnProperty(prefix = "highTrafficAlert", name = "enabled", havingValue = "true")
public class HighTrafficConsumer implements EventHandler<LogEvent> {

    private final Logger logger = LoggerFactory.getLogger(HighTrafficConsumer.class);

    //Store traffic for X seconds in a circular buffer
    //Each array index is a 1 second bucket
    private final int[] circularBuffer;
    private final int bufferSize;

    private final int alertThreshold;
    private boolean shouldEvaluate;
    private boolean alertActive = false;

    private int tail = 0;
    private long headTimestamp = -1;
    private float movingSum = 0;


    public HighTrafficConsumer(@Value("${highTrafficAlert.sampleWindow}") int sampleWindow,
                               @Value("${highTrafficAlert.threshold}") int threshold) {
        this.bufferSize = Math.max(1, sampleWindow);
        this.alertThreshold = threshold;
        this.circularBuffer = new int[bufferSize];

    }

    @Override
    public void onEvent(LogEvent logEvent, long l, boolean b) {
        if (logEvent == null) return;
        CommonLogFormat log = logEvent.getCommonLogFormat();
        if (log == null) return;

        long date = log.getDate();
        handleEvent(date);
    }

    @VisibleForTesting
    protected void handleEvent(long date) {
        moveWindow(date);
        addToWindow(date);

        if (alertThreshold > 0 && shouldEvaluate) {
            checkAlert();
        }
    }

    public boolean isAlertActive() {
        return alertActive;
    }

    public int getBufferValue(int index) {
        if (circularBuffer == null || circularBuffer.length == 0) return -1;
        if (index < 0 || index >= circularBuffer.length) return -1;
        return circularBuffer[index];
    }

    @VisibleForTesting
    protected void checkAlert() {
        //calculate the window average
        float movingAvg = movingSum / bufferSize;

        if (!isAlertActive() && movingAvg > alertThreshold) {
            onAlertTriggered(movingAvg);
        }

        if (isAlertActive() && movingAvg <= alertThreshold) {
            onAlertStopped(movingAvg);
        }
    }

    @VisibleForTesting
    protected void onAlertTriggered(float value) {
        logger.warn("{} : High traffic generated an alert - hits = {}, triggered at {}", headTimestamp, value, headTimestamp);
        alertActive = true;
        //if the alert is raised, we dont need to evaluate again until the window period moves
        shouldEvaluate = false;
    }

    @VisibleForTesting
    protected void onAlertStopped(float value) {
        logger.info("{} : Recovered from high traffic - hits = {}, recovered at {}", headTimestamp, value, headTimestamp);
        alertActive = false;
    }

    @VisibleForTesting
    protected void addToWindow(long date) {
        // increment an existing element in the window
        if (date >= headTimestamp - bufferSize && date < headTimestamp + 1) {
            //this record is within the current window

            //increment sum, not removing anything yet since the window hasn't moved
            movingSum += 1;

            //get the element
            int index = getIndex(date);
            //increment the element in the window
            circularBuffer[index] += 1;
        }
    }

    @VisibleForTesting
    protected void moveWindow(long date) {
        // shift the window if we got newer data coming in
        if (date > headTimestamp) {

            //Window is moving, so we should ensure an alert validation is done
            shouldEvaluate = true;

            //remove the current tail value
            movingSum -= circularBuffer[tail];

            //shift the window
            headTimestamp = date;
            int head = getIndex(date);
            //reset the value at the head
            circularBuffer[head] = 0;

            //tail is always head + 1, looped around since its circular
            tail = getIndex(head + 1);

        }
    }

    public int getIndex(long date) {
        return (int) (date % bufferSize);
    }

}
