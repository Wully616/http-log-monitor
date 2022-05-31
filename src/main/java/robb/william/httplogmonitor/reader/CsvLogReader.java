package robb.william.httplogmonitor.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robb.william.httplogmonitor.reader.model.LogLine;

import java.io.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class CsvLogReader implements ILogReader {
    public static final Logger logger = LoggerFactory.getLogger(CsvLogReader.class);

    final CsvMapper mapper = new CsvMapper();

    final String expectedHeader = "\"remotehost\",\"rfc931\",\"authuser\",\"date\",\"request\",\"status\",\"bytes\"";

    final CsvSchema schema = CsvSchema.builder()
            .addColumn("remoteHost")
            .addColumn("rfc931")
            .addColumn("authUser")
            .addColumn("date")
            .addColumn("request")
            .addColumn("status")
            .addColumn("bytes")
            .build();

    final ObjectReader reader = mapper
            .readerFor(LogLine.class)
            .with(schema);

    @Override
    public InputStream getLogStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public Stream<LogLine> readLog() {
        logger.info("-- opening stream --");
        // We dont use try with resources here, because we automatically close the whole stream at the end
        InputStreamReader inputStreamReader = new InputStreamReader(getLogStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        return bufferedReader
                .lines()
                .onClose(asUncheckedAutoCloseable(bufferedReader))
                .map(parseLogLine())
                .filter(Objects::nonNull);
    }

    private Function<String, LogLine> parseLogLine() {
        return line -> {
            try {
                return reader.readValue(line);
            } catch (JsonProcessingException e) {
                if (line.equals(expectedHeader)) {
                    logger.info("Parsed CSV header: {}", line);
                } else {
                    logger.error("Unable to parse CSV line: {}", line, e);
                }
            }
            return null;
        };
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
}
