package robb.william.httplogmonitor.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.LogLine;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(prefix = "stats", name = "enabled", havingValue = "true")
public class StatConsumer implements EventHandler<LogEvent> {

    private final Logger logger = LoggerFactory.getLogger(StatConsumer.class);

    Map<String, Integer> sectionStats = new HashMap<>();
    LinkedHashMap<String, Integer> sectionStatsSorted = new LinkedHashMap<>();

    private static final Pattern sectionPattern = Pattern.compile("\\s\\/(\\w+|\\d+)[\\/|\\s]");
    long currentTimeBucket = -1;

    @Override
    public void onEvent(LogEvent logEvent, long l, boolean b) throws Exception {
        LogLine logLine = logEvent.getLogLine();

        //Time bucket is unix timestamp to clamp down to the nearest 10 seconds
        //1549573995 -> 1549573990
        int timeBucket = ((int)(logLine.getDate() / 10)) * 10;

        //Apache logs entries can come in unordered due to threading.
        //As soon as we detect the next timebucket, reset the stat map.
        //This means we could lose some samples outside of our time bucket window, but the next window has already started

        if(currentTimeBucket > 0 && timeBucket > currentTimeBucket){
            //we have entered a new bucket, print the current stats, wipe and continue
            logger.info("Stats - for the time period {} to {}:", currentTimeBucket, timeBucket - 1);
            //LinkedHashMap preserve the ordering of elements in which they are inserted
            sectionStatsSorted.clear();
            sectionStats.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(e -> sectionStatsSorted.put(e.getKey(),e.getValue()));
            logger.info("\tTop 10 hit sections: {}", sectionStatsSorted);
            sectionStats.clear();
        }
        if(timeBucket > currentTimeBucket) currentTimeBucket = timeBucket;
        //only count it if its within the current timebucket
        String section = "";

        //add to the current buckets stats
        Matcher matcher = sectionPattern.matcher(logLine.getRequest());
        if (matcher.find()) {
            section = matcher.group(1);
            sectionStats.merge(section, 1, Integer::sum);
        }


        //logger.info("#1 sequence: {}, endOfbatch: {}, section: {}, currentTimeBucket: {}, time: {}, count: {}",
        //        l, b,  section, currentTimeBucket, logLine.getDate() , sectionStats.getOrDefault(section, 0));

    }
}
