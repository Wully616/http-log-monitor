package robb.william.httplogmonitor.alerters;

import org.junit.jupiter.api.Test;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatConsumerTest {

    private static CommonLogFormat getCommonLogFormat(String host, String section, String verb, int status, int bytes, long date) {
        CommonLogFormat log = new CommonLogFormat();
        log.setRemoteHost(host);
        log.setSection(section);
        log.setVerb(verb);
        log.setStatus(status);
        log.setBytes(bytes);
        log.setDate(date);

        return log;
    }

    @Test
    public void whenInvalidKey_returnOptionalEmpty() {
        StatConsumer consumer = new StatConsumer(1);
        Optional<StatConsumer.SectionStat> sectionStatOptional = consumer.getSectionStat("");
        assertTrue(sectionStatOptional.isEmpty());
        sectionStatOptional = consumer.getSectionStat(null);
        assertTrue(sectionStatOptional.isEmpty());
    }

    @Test
    public void whenMissingKey_returnOptionalEmpty() {
        StatConsumer consumer = new StatConsumer(1);
        Optional<StatConsumer.SectionStat> sectionStatOptional = consumer.getSectionStat("api");
        assertTrue(sectionStatOptional.isEmpty());
    }

    @Test
    public void whenValid_returnOptional() {
        CommonLogFormat log = getCommonLogFormat("host1", "section1", "GET", 200, 1234, 0);
        StatConsumer consumer = new StatConsumer(1);
        consumer.addToStats(log);
        Optional<StatConsumer.SectionStat> sectionStatOptional = consumer.getSectionStat("section1");
        assertTrue(sectionStatOptional.isPresent());
        StatConsumer.SectionStat stat = sectionStatOptional.get();
        assertTrue(stat.getHosts().contains("host1"));
        assertEquals(1, stat.getResponseStatus().get(200));
        assertEquals(5, stat.getResponseStatus().size()); //default keys added
        assertEquals(1, stat.getRequestMethods().get("GET"));
        assertEquals(8, stat.getRequestMethods().size());
        assertEquals(1, stat.getTotalRequests());
        assertEquals(1234, stat.getBytes());
    }

    @Test
    public void whenClear_clearSectionStats() {
        CommonLogFormat log = getCommonLogFormat("host1", "section1", "GET", 200, 1234, 0);
        StatConsumer consumer = new StatConsumer(1);
        consumer.addToStats(log);

        Optional<StatConsumer.SectionStat> sectionStatOptional = consumer.getSectionStat("section1");
        assertTrue(sectionStatOptional.isPresent());
        StatConsumer.SectionStat stat = sectionStatOptional.get();
        assertTrue(stat.getHosts().contains("host1"));
        assertEquals(1, stat.getHosts().size());
        assertEquals(1, stat.getResponseStatus().get(200));
        assertEquals(5, stat.getResponseStatus().size()); //default keys added
        assertEquals(1, stat.getRequestMethods().get("GET"));
        assertEquals(8, stat.getRequestMethods().size()); //default keys added
        assertEquals(1, stat.getTotalRequests());
        assertEquals(1234, stat.getBytes());

        stat.clearStats();
        assertTrue(stat.getHosts().isEmpty());
        assertEquals(0, stat.getResponseStatus().get(200));
        assertEquals(5, stat.getResponseStatus().size()); //default keys added
        assertEquals(0, stat.getRequestMethods().get("GET"));
        assertEquals(8, stat.getRequestMethods().size()); //default keys added
        assertEquals(0, stat.getTotalRequests());
        assertEquals(0, stat.getBytes());

    }

    @Test
    public void whenNullEvent_ReturnEarly() {
        StatConsumer spyConsumer = spy(new StatConsumer(1));
        spyConsumer.onEvent(null, 0, false);

        verify(spyConsumer, never()).getTimeBucket(anyLong());
        verify(spyConsumer, never()).printStats(anyInt());
        verify(spyConsumer, never()).addToStats(any());
    }

    @Test
    public void whenNullLog_ReturnEarly() {
        StatConsumer spyConsumer = spy(new StatConsumer(1));
        LogEvent spyEvent = spy(new LogEvent());
        spyConsumer.onEvent(spyEvent, 0, false);

        CommonLogFormat log = verify(spyEvent, times(1)).getCommonLogFormat();
        assertNull(log);
        verify(spyConsumer, never()).getTimeBucket(anyLong());
        verify(spyConsumer, never()).printStats(anyInt());
        verify(spyConsumer, never()).addToStats(any());
    }

    @Test
    public void whenFirstConsume_PrintStatsReturnsEarly() {
        CommonLogFormat log = getCommonLogFormat("host1", "section1", "GET", 200, 1234, 0);
        LogEvent logEvent = new LogEvent();
        logEvent.setCommonLogFormat(log);

        StatConsumer spyConsumer = spy(new StatConsumer(1));
        spyConsumer.onEvent(logEvent, 0, false);
        verify(spyConsumer, never()).clearStats();
    }

    @Test
    public void whenMultipleOfSameDate_StatsSummed() {
        CommonLogFormat log = getCommonLogFormat("host1", "section1", "GET", 200, 1234, 0);
        LogEvent logEvent = new LogEvent();
        logEvent.setCommonLogFormat(log);

        StatConsumer spyConsumer = spy(new StatConsumer(1));

        //Multiple events of the same timestamp
        spyConsumer.onEvent(logEvent, 0, false);
        spyConsumer.onEvent(logEvent, 0, false);

        StatConsumer.SectionStat stat = spyConsumer.getSectionStat("section1").get();
        assertTrue(stat.getHosts().contains("host1"));
        assertEquals(1, stat.getHosts().size());
        assertEquals(2, stat.getResponseStatus().get(200));
        assertEquals(2, stat.getRequestMethods().get("GET"));
        assertEquals(2, stat.getTotalRequests());
        assertEquals(2468, stat.getBytes());

    }

    @Test
    public void whenDifferentSectionsOfSameDate_StatsSeparate() {
        LogEvent logEvent = new LogEvent();
        StatConsumer spyConsumer = spy(new StatConsumer(1));

        //Multiple events of the same timestamp
        logEvent.setCommonLogFormat(getCommonLogFormat("host1", "section1", "GET", 200, 1234, 0));
        spyConsumer.onEvent(logEvent, 0, false);

        logEvent.setCommonLogFormat(getCommonLogFormat("host1", "section2", "POST", 100, 5678, 0));
        spyConsumer.onEvent(logEvent, 0, false);

        StatConsumer.SectionStat stat = spyConsumer.getSectionStat("section1").get();
        assertTrue(stat.getHosts().contains("host1"));
        assertEquals(1, stat.getHosts().size());
        assertEquals(1, stat.getResponseStatus().get(200));
        assertEquals(1, stat.getRequestMethods().get("GET"));
        assertEquals(1, stat.getTotalRequests());
        assertEquals(1234, stat.getBytes());

        stat = spyConsumer.getSectionStat("section2").get();
        assertTrue(stat.getHosts().contains("host1"));
        assertEquals(1, stat.getHosts().size());
        assertEquals(1, stat.getResponseStatus().get(100));
        assertEquals(1, stat.getRequestMethods().get("POST"));
        assertEquals(1, stat.getTotalRequests());
        assertEquals(5678, stat.getBytes());

    }

    @Test
    public void whenNewWindowAdded_PrintFirstWindowStats() {
        LogEvent logEvent = new LogEvent();


        StatConsumer spyConsumer = spy(new StatConsumer(1));

        //Multiple events but of different timestamp
        logEvent.setCommonLogFormat(getCommonLogFormat("host1", "section1", "GET", 200, 1234, 0));
        spyConsumer.onEvent(logEvent, 0, false);

        verify(spyConsumer, times(1)).printStats(anyInt());
        verify(spyConsumer, never()).clearStats();
        verify(spyConsumer, times(1)).addToStats(logEvent.getCommonLogFormat());

        logEvent.setCommonLogFormat(getCommonLogFormat("host1", "section2", "POST", 100, 5678, 1));
        spyConsumer.onEvent(logEvent, 0, false);
        verify(spyConsumer, times(2)).printStats(anyInt());
        verify(spyConsumer, times(1)).clearStats();
        verify(spyConsumer, times(1)).addToStats(logEvent.getCommonLogFormat());


    }

}
