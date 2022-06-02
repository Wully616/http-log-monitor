package robb.william.httplogmonitor.reader.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.LogLine;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class CsvLogReader implements ILogReader {
    public static final Logger logger = LoggerFactory.getLogger(CsvLogReader.class);

    private final CsvMapper mapper = new CsvMapper();

    private final String expectedHeader = "\"remotehost\",\"rfc931\",\"authuser\",\"date\",\"request\",\"status\",\"bytes\"";

    private final CsvSchema schema = CsvSchema.builder()
            .addColumn("remoteHost")
            .addColumn("rfc931")
            .addColumn("authUser")
            .addColumn("date")
            .addColumn("request")
            .addColumn("status")
            .addColumn("bytes")
            .build();

    private final ObjectReader reader = mapper
            .readerFor(LogLine.class)
            .with(schema);

    private final LogEventBuffer logEventBuffer;

    private boolean shouldRead = true;

    public CsvLogReader(LogEventBuffer logEventBuffer) {
        this.logEventBuffer = logEventBuffer;
    }

    @Override
    public InputStream getLogStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public void readLog() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            // We dont use try with resources here, because we automatically close the whole stream at the end
            logger.info("-- opening stream --");
            RingBuffer<LogEvent> ringBuffer = logEventBuffer.getRingBuffer();
            try (InputStreamReader inputStreamReader = new InputStreamReader(getLogStream());
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                while (shouldRead) {

                    var line = bufferedReader.readLine();
                    if (line == null) {
                        TimeUnit.SECONDS.sleep(1);
                        continue;
                    }
                    parseLogLine(line).ifPresent(value ->
                            ringBuffer.publishEvent((event, sequence, buffer) -> event.setLogLine(value)));


                }
            } catch (Exception e) {
                logger.error("Exception when reading file", e);
            }
        });


//
//        return bufferedReader
//                .lines()
//                .onClose(asUncheckedAutoCloseable(bufferedReader))
//                .map(parseLogLine())
//                .filter(Objects::nonNull)
//                .peek(logLine -> logger.info(logLine.toString()));

    }

    private Optional<LogLine> parseLogLine(String line) {

        try {
            return Optional.of(reader.readValue(line));
        } catch (JsonProcessingException e) {
            if (line.equals(expectedHeader)) {
                logger.info("Parsed CSV header: {}", line);
            } else {
                logger.error("Unable to parse CSV line: {}", line, e);
            }
        }
        return Optional.empty();

    }

    static Runnable asUncheckedAutoCloseable(AutoCloseable ac) {
        return () -> {
            try {
                logger.info("-- closing stream --");
                ac.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @PreDestroy
    public void destroy() {
        shouldRead = false;
    }
}
