package robb.william.httplogmonitor.reader;

import robb.william.httplogmonitor.reader.model.LogLine;
import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

import java.io.InputStream;
import java.util.stream.Stream;

public interface ILogReader {

    ReaderStrategy getStrategyType();

    InputStream getLogStream();

    Stream<LogLine> readLog();

}
