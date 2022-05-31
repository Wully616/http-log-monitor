package robb.william.httplogmonitor.reader.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.reader.CsvLogReader;
import robb.william.httplogmonitor.reader.model.LogLine;

import java.io.InputStream;
import java.util.stream.Stream;

@Component
public class StdinCsvLogReader extends CsvLogReader {
    Logger logger = LoggerFactory.getLogger(StdinCsvLogReader.class);

    @Override
    public ReaderStrategy getStrategyType() {
        return ReaderStrategy.STDIN;
    }

    @Override
    public InputStream getLogStream() {
        return System.in;
    }

    @Override
    public Stream<LogLine> readLog() {
        logger.info("Reading log file from stdin");
        return super.readLog();
    }
}
