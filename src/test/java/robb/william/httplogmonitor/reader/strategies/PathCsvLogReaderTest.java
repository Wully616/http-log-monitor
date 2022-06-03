package robb.william.httplogmonitor.reader.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class PathCsvLogReaderTest {

    @Mock
    LogEventBuffer logEventBuffer;

    @BeforeEach
    public void initMocks() {
        logEventBuffer = mock(LogEventBuffer.class);
    }

    @Test
    public void whenCreated_returnExpectedType() {
        ILogReader reader = new PathCsvLogReader(logEventBuffer, "");
        assertEquals(ReaderStrategy.FILE, reader.getStrategyType());
    }

    @Test
    public void whenNoPath_returnEmptyStream() throws IOException {
        ILogReader reader = new PathCsvLogReader(logEventBuffer, "");
        InputStream stream = reader.getLogStream();
        var data = stream.read();
        //end of data is -1
        assertEquals(-1, data);
    }

    @Test
    public void whenInvalidPath_returnEmptyStream() throws IOException {
        ILogReader reader = new PathCsvLogReader(logEventBuffer, Path.of("", "src/test/resources/testdata/").toAbsolutePath().toString());
        InputStream stream = reader.getLogStream();
        var data = stream.read();
        //end of data is -1
        assertEquals(-1, data);
    }

    @Test
    public void whenNoPath_NoPublishesHappen() {
        PathCsvLogReader reader = new PathCsvLogReader(logEventBuffer, "");
        PathCsvLogReader readerSpy = spy(reader);
        readerSpy.readLog();
        verify(readerSpy, times(0)).parseLogLine(any());
        verify(readerSpy, times(0)).extractAdditionalFields(any());
        verify(logEventBuffer, times(0)).publish(any());
    }

    @Test
    public void whenValidPath_returnStreamWithData() throws IOException {
        ILogReader reader = new PathCsvLogReader(logEventBuffer, Path.of("", "src/test/java/resources/testdata/valid-data.csv").toAbsolutePath().toString());
        InputStream stream = reader.getLogStream();
        var data = stream.read();
        //end of data is -1, so this would be anything except that
        assertNotEquals(-1, data);
    }

    @Test
    public void whenValidPath_publishData() throws InterruptedException {
        PathCsvLogReader reader = new PathCsvLogReader(logEventBuffer, Path.of("", "src/test/java/resources/testdata/valid-data.csv").toAbsolutePath().toString());
        PathCsvLogReader readerSpy = spy(reader);
        readerSpy.readLog();
        TimeUnit.MILLISECONDS.sleep(500);
        verify(logEventBuffer, atLeastOnce()).publish(any());
    }

    @Test
    public void whenInvalidData_DoNotPublishData() throws InterruptedException {
        PathCsvLogReader reader = new PathCsvLogReader(logEventBuffer, Path.of("", "src/test/java/resources/testdata/invalid-data.csv").toAbsolutePath().toString());
        PathCsvLogReader readerSpy = spy(reader);
        readerSpy.readLog();

        readerSpy.readLog();
        TimeUnit.MILLISECONDS.sleep(500);
        verify(logEventBuffer, never()).publish(any());
    }
}
