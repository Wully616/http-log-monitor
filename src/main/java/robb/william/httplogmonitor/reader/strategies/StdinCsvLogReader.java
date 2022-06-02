package robb.william.httplogmonitor.reader.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;

import java.io.InputStream;

@Component
@ConditionalOnExpression("'${path.file}' == '' || '${path.stdin}' != ''")
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
        logger.info("Reading log file from stdin");
        return System.in;
    }
}
