package robb.william.httplogmonitor.reader.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CsvLogReader implements ILogReader {
    public static final Logger logger = LoggerFactory.getLogger(CsvLogReader.class);

    private final CsvMapper mapper = new CsvMapper().configure(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS, true);

    private final String expectedHeader = "\"remotehost\",\"rfc931\",\"authuser\",\"date\",\"request\",\"status\",\"bytes\"";

    private static final Pattern sectionPattern = Pattern.compile("(\\w+)\\s\\/(\\w+|\\d+)[\\/|\\s]");

    private static final CsvSchema schema = CsvSchema.builder()
            .addColumn("remoteHost")
            .addColumn("rfc931")
            .addColumn("authUser")
            .addColumn("date")
            .addColumn("request")
            .addColumn("status")
            .addColumn("bytes")
            .build();

    private final ObjectReader reader = mapper
            .readerFor(CommonLogFormat.class)
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
        Runnable task = () -> {
            InputStream inputStream = getLogStream();
            if (inputStream != null) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    while (shouldRead) {

                        var line = bufferedReader.readLine();
                        if (line == null) {
                            TimeUnit.SECONDS.sleep(1);
                            continue;
                        }
                        parseLogLine(line).ifPresent(value -> {
                            // Extract any additional data from the fields here so we only do it once.
                            CommonLogFormat log = extractAdditionalFields(value);
                            //Publish the event
                            logEventBuffer.publish(log);
                        });

                    }
                } catch (IOException | InterruptedException e) {
                    logger.error("Exception when reading file. Please restart the application", e);
                }
            } else {
                logger.error("Log input data stream is null");
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
    }


    @VisibleForTesting
    public CommonLogFormat extractAdditionalFields(CommonLogFormat log) {
        Matcher matcher = sectionPattern.matcher(log.getRequest());
        if (matcher.find()) {
            log.setVerb(matcher.group(1).toUpperCase());
            log.setSection(matcher.group(2));
        }
        return log;
    }

    @VisibleForTesting
    public Optional<CommonLogFormat> parseLogLine(String line) {

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

    @PreDestroy
    public void preDestroy() {
        shouldRead = false;
    }
}
