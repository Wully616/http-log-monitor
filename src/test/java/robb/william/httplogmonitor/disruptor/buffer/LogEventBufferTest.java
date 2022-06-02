package robb.william.httplogmonitor.disruptor.buffer;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

import static org.junit.jupiter.api.Assertions.*;

public class LogEventBufferTest {
    LogEventBuffer buffer;

    @AfterEach
    public void haltDisruptor() {
        buffer.getLogEventDisruptor().halt();
    }

    @Test
    public void whenBufferTooSmall_thenSetDefaultSize() {
        buffer = new LogEventBuffer(null, 0);
        assertEquals(2, buffer.getRingBuffer().remainingCapacity());
        Disruptor<LogEvent> logEventDisruptor = buffer.getLogEventDisruptor();
        assertFalse(logEventDisruptor.hasStarted());
    }

    @Test
    public void whenCreatedWithoutConsumer_disruptorHalted() {
        buffer = new LogEventBuffer(null, 2);
        Disruptor<LogEvent> logEventDisruptor = buffer.getLogEventDisruptor();

        assertEquals(2, buffer.getRingBuffer().remainingCapacity());
        assertFalse(logEventDisruptor.hasStarted());
    }

    @Test
    public void whenCreatedWithConsumer_disruptorStarted() {
        EventHandler<LogEvent>[] consumers = new EventHandler[1];
        consumers[0] = (event, sequence, endOfBatch) -> System.out.println(sequence);

        buffer = new LogEventBuffer(consumers, 2);
        Disruptor<LogEvent> logEventDisruptor = buffer.getLogEventDisruptor();
        assertTrue(logEventDisruptor.hasStarted());

        var activeConsumers = buffer.getLogEventConsumers();
        assertEquals(1, activeConsumers.length);
    }

    @Test
    public void whenNothingPublished_HolderIsNull() {
        buffer = new LogEventBuffer(null, 2);
        Disruptor<LogEvent> logEventDisruptor = buffer.getLogEventDisruptor();
        var eventZero = logEventDisruptor.get(0);
        var eventOne = logEventDisruptor.get(1);

        assertNotSame(eventOne, eventZero);
        assertNull(eventZero.getLogLine());
        assertNull(eventOne.getLogLine());

    }

    @Test
    public void whenPublished_HolderHasData() {
        buffer = new LogEventBuffer(null, 2);
        Disruptor<LogEvent> logEventDisruptor = buffer.getLogEventDisruptor();
        CommonLogFormat log = new CommonLogFormat();
        buffer.publish(log);
        var eventZero = logEventDisruptor.get(0);
        assertNotNull(eventZero.getLogLine());
        assertEquals(log, eventZero.getLogLine());
    }


}
