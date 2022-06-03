package robb.william.httplogmonitor.reader.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


public class CsvLogReaderTest {

    @Mock
    LogEventBuffer logEventBuffer;

    CsvLogReader reader;

    @BeforeEach
    public void setup() {
        //instantiating abstract class, it needs to implement a single method thankfully.
        reader = new CsvLogReader(logEventBuffer) {
            @Override
            public ReaderStrategy getStrategyType() {
                return null;
            }
        };

        logEventBuffer = mock(LogEventBuffer.class);
    }

    @Test
    public void whenCreated_returnExpectedType() {
        assertNull(reader.getStrategyType());
    }


    @Test
    public void whenValidLineParsed_ReturnPresentOptional() throws IOException {
        Optional<CommonLogFormat> optionalLog = reader.parseLogLine("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",200,1234");
        assertTrue(optionalLog.isPresent());
        CommonLogFormat log = optionalLog.get();
        assertEquals("10.0.0.2", log.getRemoteHost());
        assertEquals("-", log.getRfc931());
        assertEquals("apache", log.getAuthUser());
        assertEquals(1549573860, log.getDate());
        assertEquals("GET /api/user HTTP/1.0", log.getRequest());
        assertEquals(200, log.getStatus());
        assertEquals(1234, log.getBytes());
    }

    @Test
    public void whenInvalidLineParsed_ReturnEmptyOptional() {
        Optional<CommonLogFormat> optionalLog = reader.parseLogLine("INVALID,LOG,LINE");
        assertFalse(optionalLog.isPresent());
    }

    @Test
    public void whenExpectedHeaderLineParsed_ReturnEmptyOptional() {
        Optional<CommonLogFormat> optionalLog = reader.parseLogLine("\"remotehost\",\"rfc931\",\"authuser\",\"date\",\"request\",\"status\",\"bytes\"");
        assertFalse(optionalLog.isPresent());
    }

    @Test
    public void whenMatchedRequest_SetFields() {
        CommonLogFormat log = new CommonLogFormat();
        log.setRequest("GET /api/user HTTP/1.0");

        assertNull(log.getVerb());
        assertNull(log.getSection());

        CommonLogFormat additionalLog = reader.extractAdditionalFields(log);
        assertEquals("GET", additionalLog.getVerb());
        assertEquals("api", additionalLog.getSection());
    }

    @Test
    public void whenNotMatchedRequest_ReturnUnchanged() {
        CommonLogFormat log = new CommonLogFormat();
        log.setRequest("INVALIDREQUEST");

        assertNull(log.getVerb());
        assertNull(log.getSection());

        CommonLogFormat additionalLog = reader.extractAdditionalFields(log);

        assertNull(additionalLog.getVerb());
        assertNull(additionalLog.getSection());

    }

    @Test
    public void whenPartialMatch_ReturnUnchanged() {
        CommonLogFormat log = new CommonLogFormat();
        log.setRequest("GET /api");

        assertNull(log.getVerb());
        assertNull(log.getSection());

        CommonLogFormat additionalLog = reader.extractAdditionalFields(log);

        assertNull(additionalLog.getVerb());
        assertNull(additionalLog.getSection());

    }
}
