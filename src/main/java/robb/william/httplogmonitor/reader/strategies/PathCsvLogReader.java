package robb.william.httplogmonitor.reader.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.config.FileConfig;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;
import robb.william.httplogmonitor.reader.CsvLogReader;

import java.io.*;

@Component
public class PathCsvLogReader extends CsvLogReader {
    private static final Logger logger = LoggerFactory.getLogger(PathCsvLogReader.class);

    private FileConfig fileConfig;

    @Autowired
    public PathCsvLogReader(LogEventBuffer logEventBuffer, FileConfig fileConfig) {
        super(logEventBuffer);
        this.fileConfig = fileConfig;
    }

    @Override
    public ReaderStrategy getStrategyType() {
        return ReaderStrategy.FILE;
    }

    @Override
    public InputStream getLogStream() {
        String path = fileConfig.getFilePath();

        if(path != null && !path.isEmpty()) {
            try {
                File initialFile = new File(fileConfig.getFilePath());
                return new FileInputStream(initialFile);
            } catch (FileNotFoundException e) {
                logger.error("File not found: {}", fileConfig.getFilePath(), e);
            }
        } else {
            logger.error("File path argument is empty.");
        }
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public void readLog() {
        logger.info("Reading log file from file: {}", fileConfig.getFilePath());
        super.readLog();
    }
}
