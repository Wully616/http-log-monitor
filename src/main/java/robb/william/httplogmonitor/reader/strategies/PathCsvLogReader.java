package robb.william.httplogmonitor.reader.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Component
@ConditionalOnExpression("'${path.file}' != ''") //only create the bean if the path is not the default empty string
public class PathCsvLogReader extends CsvLogReader {
    private static final Logger logger = LoggerFactory.getLogger(PathCsvLogReader.class);

    String filePath;

    @Autowired
    public PathCsvLogReader(LogEventBuffer logEventBuffer, @Value("${path.file}") String filePath) {
        super(logEventBuffer);
        logger.info("Created PathCsvLogReader");
        this.filePath = filePath;
    }

    @Override
    public ReaderStrategy getStrategyType() {
        return ReaderStrategy.FILE;
    }

    @Override
    public InputStream getLogStream() {

        if(filePath != null && !filePath.isEmpty()) {
            try {
                logger.info("Reading log file from file: {}", filePath);
                File initialFile = new File(filePath);
                return new FileInputStream(initialFile);
            } catch (FileNotFoundException e) {
                logger.error("File not found. Please check the path and restart the application: {}", filePath);
            }
        } else {
            logger.error("File path argument is empty.");
        }
        return super.getLogStream();
    }

}
