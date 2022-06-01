package robb.william.httplogmonitor.reader;

import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

import java.io.InputStream;

public interface ILogReader {

    ReaderStrategy getStrategyType();

    InputStream getLogStream();

    void readLog();


}
