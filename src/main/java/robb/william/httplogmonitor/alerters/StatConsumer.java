package robb.william.httplogmonitor.alerters;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.disruptor.event.LogEvent;
import robb.william.httplogmonitor.reader.model.CommonLogFormat;

import java.util.*;

/*
    - For every 10 seconds of log lines, display stats about the traffic during those 10s -

    This consumer assumes an Interval is defined for the collection period.
    This consumer assumes a collection period starts from an event at timestamp - A
    This consumer will then gather statistics on the logs for the defined Interval time
    This consumer assumes a collection period ends from an event at timestamp - B. Where B > A + Interval
    A new collection period then begins again


    The log lines themselves may come in out of order due to threading on the http server.
    This means an older timestamp may fall into a later timestamp bucket. But according to the log, the Interval has still passed.

    It seems like a good trade off to print the stats when the log file indicates a new bucket has started, rather than
    increasing memory costs by holding multiple buckets and waiting for an undetermined period to wait for them to fill before printing,
    which is determined entirely by how large the variances is in the http servers logging daemon are and by our Interval value.

 */
@Component
@ConditionalOnProperty(prefix = "stats", name = "enabled", havingValue = "true")
public final class StatConsumer implements EventHandler<LogEvent> {

    private final Logger logger = LoggerFactory.getLogger(StatConsumer.class);

    private final Map<String, SectionStat> sectionStats;

    private final int interval;
    private final StringBuilder sb;
    private long currentTimeBucket = -1;

    public StatConsumer(@Value("${stats.printInterval}") int interval) {
        this.interval = interval;
        sectionStats = new HashMap<>();
        sb = new StringBuilder();
    }

    @Override
    public void onEvent(LogEvent logEvent, long l, boolean b) {
        CommonLogFormat logLine = logEvent.getCommonLogFormat();

        //Time bucket is a unix timestamp, clamped down to the nearest interval seconds
        int timeBucket = ((int) (logLine.getDate() / interval)) * interval;

        //check if the interval has passed and print the stats
        if (timeBucket > currentTimeBucket) {
            printStats(timeBucket);
            currentTimeBucket = timeBucket;
        }

        //Store the stats in the current bucket
        addToStats(logLine);

    }

    private void printStats(int timeBucket) {
        //we have entered a new bucket, print the current stats, wipe and continue
        if (sectionStats.isEmpty()) return;

        sb.append("\nHTTP Stats for last ").append(interval).append(" seconds from: ").append(currentTimeBucket)
                .append(" to ").append(timeBucket + 1);

        sectionStats.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(e -> e.getValue().getTotalRequests())))
                .forEachOrdered(e -> {
                    String section = e.getKey();
                    SectionStat stats = e.getValue();
                    sb.append("\n\tSection: ").append(section);
                    sb.append("\n--------------------------------------------");
                    sb.append("\n\tTotal Hits: ").append(stats.getTotalRequests());
                    sb.append("\n\tRemote Hosts: ").append(stats.getHosts().size());
                    sb.append("\n\tHTTP Status: ").append(stats.getResponseStatus());
                    sb.append("\n\tRequest Types: ").append(stats.getRequestMethods());
                    sb.append("\n--------------------------------------------");
                });

        logger.info(sb.toString());
        //reset the string builder so we can reuse it.
        sb.setLength(0);

        clearStats();

    }

    private void clearStats() {
        sectionStats.forEach((key, value) -> value.clearStats());
    }

    private void addToStats(CommonLogFormat log) {
        //add to the current buckets stats
        SectionStat stats = sectionStats.computeIfAbsent(log.getSection(), l -> new SectionStat());
        stats.addStats(log);
    }

    private static class SectionStat {
        private final Set<String> hosts;
        //Contains stats for http response statuses, 1xx,2xx,3xx,4xx,5xx
        private final Map<Integer, Integer> responseStatus;
        private final Map<String, Integer> requestMethods;
        private int totalRequests;
        private int bytes;

        public SectionStat() {
            hosts = new HashSet<>();

            //preserve order, prepopulate the keys
            responseStatus = new LinkedHashMap<>();
            responseStatus.put(100, 0);
            responseStatus.put(200, 0);
            responseStatus.put(300, 0);
            responseStatus.put(400, 0);
            responseStatus.put(500, 0);

            requestMethods = new LinkedHashMap<>();
            requestMethods.put("DELETE", 0);
            requestMethods.put("GET", 0);
            requestMethods.put("HEAD", 0);
            requestMethods.put("OPTIONS", 0);
            requestMethods.put("PATCH", 0);
            requestMethods.put("POST", 0);
            requestMethods.put("PUT", 0);
            requestMethods.put("TRACE", 0);

        }

        //Wipe the stats on this instance so we can reuse it
        public void clearStats() {
            totalRequests = 0;
            bytes = 0;
            hosts.clear();
            responseStatus.entrySet().forEach(e -> e.setValue(0));
            requestMethods.entrySet().forEach(e -> e.setValue(0));
        }

        public void addStats(CommonLogFormat log) {
            totalRequests++;
            bytes += log.getBytes();
            hosts.add(log.getRemoteHost());

            int status = (log.getStatus() / 100) * 100; //clamps the http status down to nearest 100
            responseStatus.merge(status, 1, Integer::sum);
            requestMethods.merge(log.getVerb(), 1, Integer::sum);

        }

        public int getTotalRequests() {
            return totalRequests;
        }

        public Set<String> getHosts() {
            return hosts;
        }

        public Map<Integer, Integer> getResponseStatus() {
            return responseStatus;
        }

        public Map<String, Integer> getRequestMethods() {
            return requestMethods;
        }


    }
}
