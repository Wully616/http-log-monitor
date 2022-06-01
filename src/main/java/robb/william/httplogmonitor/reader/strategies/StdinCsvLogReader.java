package robb.william.httplogmonitor.reader.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;
import robb.william.httplogmonitor.reader.CsvLogReader;

import java.io.InputStream;

@Component
public class StdinCsvLogReader extends CsvLogReader {
    Logger logger = LoggerFactory.getLogger(StdinCsvLogReader.class);

    @Autowired
    public StdinCsvLogReader(LogEventBuffer logEventBuffer) {
        super(logEventBuffer);
    }

    @Override
    public ReaderStrategy getStrategyType() {
        return ReaderStrategy.STDIN;
    }

    @Override
    public InputStream getLogStream() {
        return System.in;
    }

    @Override
    public void readLog() {
        logger.info("Reading log file from stdin");
        super.readLog();
    }
}
