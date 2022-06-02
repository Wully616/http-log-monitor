package robb.william.httplogmonitor.reader.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.reader.strategies.ILogReader;
import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class LogReaderFactory {

    private Map<ReaderStrategy, ILogReader> strategies;

    @Autowired
    public LogReaderFactory(Set<ILogReader> iLogReaderSet) {
        setStrategies(iLogReaderSet);
    }

    public ILogReader getStrategy(ReaderStrategy readerStrategy) {
        if (strategies == null || strategies.isEmpty()) return null;
        return strategies.get(readerStrategy);
    }

    private void setStrategies(Set<ILogReader> strategySet) {
        if (strategySet == null || strategySet.isEmpty()) return;
        strategies = new HashMap<>();
        strategySet.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getStrategyType() != null)
                .forEach(
                        strategy -> strategies.put(strategy.getStrategyType(), strategy)
                );
    }
}
