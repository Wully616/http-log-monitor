package robb.william.httplogmonitor.reader.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.reader.strategies.ILogReader;
import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class LogReaderFactory {

    private Map<ReaderStrategy, ILogReader> strategies;

    @Autowired
    public LogReaderFactory(Set<ILogReader> iLogReaderSet) {
        createStrategy(iLogReaderSet);
    }

    public ILogReader getStrategy(ReaderStrategy readerStrategy) {
        return strategies.get(readerStrategy);
    }

    private void createStrategy(Set<ILogReader> strategySet) {
        strategies = new HashMap<>();
        strategySet.forEach(
                strategy -> strategies.put(strategy.getStrategyType(), strategy));
    }
}
