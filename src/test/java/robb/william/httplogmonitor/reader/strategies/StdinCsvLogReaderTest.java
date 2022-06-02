package robb.william.httplogmonitor.reader.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StdinCsvLogReaderTest {

    @Mock
    LogEventBuffer logEventBuffer;

    @BeforeEach
    public void initMocks() {
        logEventBuffer = mock(LogEventBuffer.class);
    }


    @Test
    public void whenCreated_returnExpectedType() {
        ILogReader reader = new StdinCsvLogReader(logEventBuffer);
        assertEquals(ReaderStrategy.STDIN, reader.getStrategyType());
    }

    @Test
    public void whenNullStdIn_returnNullStream() {
        System.setIn(null);
        ILogReader reader = new StdinCsvLogReader(logEventBuffer);
        InputStream stream = reader.getLogStream();
        assertNull(stream);
    }

    @Test
    public void whenEmptyStdIn_returnEmptyStream() throws IOException {
        System.setIn(new ByteArrayInputStream(new byte[0]));
        ILogReader reader = new StdinCsvLogReader(logEventBuffer);
        InputStream stream = reader.getLogStream();
        var data = stream.read();
        //end of data is -1
        assertEquals(-1, data);
    }

    @Test
    public void whenNoStdin_NoPublishesHappen() {
        System.setIn(null);
        StdinCsvLogReader reader = new StdinCsvLogReader(logEventBuffer);
        StdinCsvLogReader readerSpy = spy(reader);
        readerSpy.readLog();
        verify(readerSpy, times(0)).parseLogLine(any());
        verify(readerSpy, times(0)).extractAdditionalFields(any());
        verify(logEventBuffer, times(0)).publish(any());
    }

    @Test
    public void whenValidStdin_returnStreamWithData() throws IOException {
        setupStdIn(Path.of("", "src/test/java/resources/testdata/valid-data.csv").toAbsolutePath().toString());
        ILogReader reader = new StdinCsvLogReader(logEventBuffer);
        InputStream stream = reader.getLogStream();
        var data = stream.read();
        //end of data is -1, so this would be anything except that
        assertNotEquals(-1, data);
    }

    @Test
    public void whenValidStdin_publishData() throws IOException, InterruptedException {
        setupStdIn(Path.of("", "src/test/java/resources/testdata/valid-data.csv").toAbsolutePath().toString());
        StdinCsvLogReader reader = new StdinCsvLogReader(logEventBuffer);
        StdinCsvLogReader readerSpy = spy(reader);

        readerSpy.readLog();
        TimeUnit.MILLISECONDS.sleep(500);
        verify(logEventBuffer, atLeastOnce()).publish(any());
    }


    private void setupStdIn(String path) throws FileNotFoundException {
        FileInputStream is = new FileInputStream(new File(path));
        System.setIn(is);
    }

}
