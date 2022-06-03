package robb.william.httplogmonitor.reader.strategies;

import java.io.InputStream;

public interface ILogReader {

    ReaderStrategy getStrategyType();

    InputStream getLogStream();

    void readLog();


}
