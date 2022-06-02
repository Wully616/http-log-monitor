package robb.william.httplogmonitor.reader.factory;

import org.junit.jupiter.api.Test;
import robb.william.httplogmonitor.reader.strategies.ILogReader;
import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LogReaderFactoryTest {

    @Test
    public void whenNullRequested_returnNull() {
        LogReaderFactory factory = new LogReaderFactory(Collections.emptySet());
        assertNull(factory.getStrategy(null));
    }

    @Test
    public void whenCreatedWithNoStrategies_returnNull() {
        LogReaderFactory factory = new LogReaderFactory(Collections.emptySet());
        assertNull(factory.getStrategy(ReaderStrategy.FILE));
        assertNull(factory.getStrategy(ReaderStrategy.STDIN));
    }

    @Test
    public void whenCreatedWithNullStrategySet_returnNull() {
        LogReaderFactory factory = new LogReaderFactory(null);
        assertNull(factory.getStrategy(ReaderStrategy.FILE));
        assertNull(factory.getStrategy(ReaderStrategy.STDIN));
    }

    @Test
    public void whenCreatedWithNullStrategies_returnNull() {
        Set<ILogReader> readerSet = new HashSet<>();
        readerSet.add(null);
        LogReaderFactory factory = new LogReaderFactory(readerSet);
        assertNull(factory.getStrategy(ReaderStrategy.FILE));
        assertNull(factory.getStrategy(ReaderStrategy.STDIN));
    }

    @Test
    public void whenCreatedWithInvalidStrategies_returnNull() {

        ILogReader invalidReader = new ILogReader() {
            @Override
            public ReaderStrategy getStrategyType() {
                return null;
            }

            @Override
            public InputStream getLogStream() {
                return null;
            }

            @Override
            public Runnable readLog() {
                return () -> {
                };
            }
        };

        LogReaderFactory factory = new LogReaderFactory(Set.of(invalidReader));
        assertNull(factory.getStrategy(ReaderStrategy.FILE));
        assertNull(factory.getStrategy(ReaderStrategy.STDIN));
    }

    @Test
    public void whenCreatedWithValidStrategies_returnRequestedOne() {

        ILogReader validReader = new ILogReader() {
            @Override
            public ReaderStrategy getStrategyType() {
                return ReaderStrategy.FILE;
            }

            @Override
            public InputStream getLogStream() {
                return null;
            }

            @Override
            public Runnable readLog() {
                return () -> {
                };
            }
        };

        LogReaderFactory factory = new LogReaderFactory(Set.of(validReader));
        assertNotNull(factory.getStrategy(ReaderStrategy.FILE));

    }
}
