package robb.william.httplogmonitor.alerters;

import org.junit.jupiter.api.Test;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HighTrafficConsumerTest {

    @Test
    public void whenCreatedWithInvalidSize_BufferSizeIsDefault() {
        HighTrafficConsumer consumer = new HighTrafficConsumer(-1, 0);
        //buffer should be of size 1
        assertEquals(0, consumer.getBufferValue(0));
        //should be invalid and return -1
        assertEquals(-1, consumer.getBufferValue(1));
    }

    @Test
    public void whenCreatedWithValidSize_BufferSizeIsTheProvidedValue() {
        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 0);
        //buffer should be of size 1
        assertEquals(0, consumer.getBufferValue(0));
        //should be invalid and return -1
        assertEquals(0, consumer.getBufferValue(1));
    }

    @Test
    public void whenCreated_ReturnAlertFalse() {
        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 0);
        assertFalse(consumer.isAlertActive());
    }

    @Test
    public void whenNullLog_ReturnEarly() {
        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 1);
        HighTrafficConsumer spyConsumer = spy(consumer);
        spyConsumer.onEvent(new LogEvent(), 0, false);
        verify(spyConsumer, times(0)).handleEvent(anyLong());
    }

    @Test
    public void whenZeroThreshold_ExpectAlertLogicNotCalled() {
        LogEvent event = new LogEvent();
        event.setCommonLogFormat(new CommonLogFormat());

        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 0);
        HighTrafficConsumer spyConsumer = spy(consumer);

        spyConsumer.onEvent(event, 0, false);
        verify(spyConsumer, times(1)).handleEvent(anyLong());
        verify(spyConsumer, times(1)).moveWindow(anyLong());
        verify(spyConsumer, times(1)).addToWindow(anyLong());
        verify(spyConsumer, times(0)).checkAlert();
    }

    @Test
    public void whenValidThreshold_ExpectAlertLogicCalled() {
        LogEvent event = new LogEvent();
        event.setCommonLogFormat(new CommonLogFormat());

        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 1);
        HighTrafficConsumer spyConsumer = spy(consumer);

        spyConsumer.onEvent(event, 0, false);
        verify(spyConsumer, times(1)).handleEvent(anyLong());
        verify(spyConsumer, times(1)).moveWindow(anyLong());
        verify(spyConsumer, times(1)).addToWindow(anyLong());
        verify(spyConsumer, times(1)).checkAlert();
    }

    @Test
    public void whenEventsHappen_ExpectBufferToPopulate() {
        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 2);
        assertEquals(0, consumer.getBufferValue(0));
        assertEquals(0, consumer.getBufferValue(1));

        consumer.handleEvent(0);
        assertEquals(1, consumer.getBufferValue(0));
        assertEquals(0, consumer.getBufferValue(1));

        consumer.handleEvent(0); //same date again, buffer increments 0
        assertEquals(2, consumer.getBufferValue(0));
        assertEquals(0, consumer.getBufferValue(1));


        consumer.handleEvent(1); //next date, increments buffer 1

        assertEquals(2, consumer.getBufferValue(0));
        assertEquals(1, consumer.getBufferValue(1));

    }

    @Test
    public void whenEventOutsideWindow_MoveWindow() {
        HighTrafficConsumer consumer = new HighTrafficConsumer(2, 2);
        assertEquals(0, consumer.getBufferValue(0));
        assertEquals(0, consumer.getBufferValue(1));

        consumer.handleEvent(0);
        consumer.handleEvent(0);
        assertEquals(2, consumer.getBufferValue(0));
        assertEquals(0, consumer.getBufferValue(1));

        consumer.handleEvent(2); //outside of window, rolls over to index 0
        assertEquals(1, consumer.getBufferValue(0));
        assertEquals(0, consumer.getBufferValue(1));

        consumer.handleEvent(2); //outside of window, rolls over to index 0
        assertEquals(2, consumer.getBufferValue(0));

        consumer.handleEvent(3); //outside of window, rolls over to index 1
        assertEquals(2, consumer.getBufferValue(0));
        assertEquals(1, consumer.getBufferValue(1));

    }

    @Test
    public void whenOverThreshold_TriggerAlert() {
        HighTrafficConsumer spyConsumer = spy(new HighTrafficConsumer(2, 2));

        assertEquals(0, spyConsumer.getBufferValue(0));
        assertEquals(0, spyConsumer.getBufferValue(1));
        assertFalse(spyConsumer.isAlertActive()); // no alert yet


        spyConsumer.handleEvent(0);
        spyConsumer.handleEvent(0);
        verify(spyConsumer, times(2)).checkAlert();
        assertFalse(spyConsumer.isAlertActive()); // no alert yet

        spyConsumer.handleEvent(1);
        spyConsumer.handleEvent(1);
        spyConsumer.handleEvent(1);
        //alert should be triggered since movingAvg = 2
        verify(spyConsumer, times(5)).checkAlert();
        assertTrue(spyConsumer.isAlertActive()); // more than threshold 2

        spyConsumer.handleEvent(1); //another event in same window
        verify(spyConsumer, times(5)).checkAlert(); // alert doesnt need to be checked again
        assertTrue(spyConsumer.isAlertActive()); // alert is still active

    }


    @Test
    public void whenAlertTriggeredAndTrafficDrops_StopAlert() {
        HighTrafficConsumer spyConsumer = spy(new HighTrafficConsumer(2, 1));
        assertFalse(spyConsumer.isAlertActive()); // no alert yet
        spyConsumer.handleEvent(0); //0.5
        spyConsumer.handleEvent(1); //1
        assertFalse(spyConsumer.isAlertActive()); // no alert yet
        spyConsumer.handleEvent(1); //1.5
        assertTrue(spyConsumer.isAlertActive()); // alert triggered
        verify(spyConsumer, times(3)).checkAlert();
        verify(spyConsumer, times(1)).onAlertTriggered(1.5f);


        //traffic comes in on same window
        spyConsumer.handleEvent(1); //index 0, total should still be the same
        assertTrue(spyConsumer.isAlertActive());
        verify(spyConsumer, times(3)).checkAlert(); //alert is not checked again since alert is active
        verify(spyConsumer, times(1)).onAlertTriggered(1.5f); //no duplicate alert

        //traffic comes in new window
        spyConsumer.handleEvent(2); //index 0
        verify(spyConsumer, times(4)).checkAlert(); //alert is checked since its a new window
        verify(spyConsumer, times(1)).onAlertTriggered(1.5f); //no duplicate alert
        assertTrue(spyConsumer.isAlertActive()); // still active alert


        //traffic comes in new window, drops average, alert stops
        spyConsumer.handleEvent(3); //index 1
        verify(spyConsumer, times(5)).checkAlert(); //alert is checked since its a new window
        verify(spyConsumer, times(1)).onAlertStopped(1f); //alert stops
        assertFalse(spyConsumer.isAlertActive()); //alert stops

    }
}
