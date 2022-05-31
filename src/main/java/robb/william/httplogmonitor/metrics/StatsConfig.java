package robb.william.httplogmonitor.metrics;

import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsConfig {
    int statsPrintInterval;

    public int getStatsPrintInterval() {
        return statsPrintInterval;
    }

    public void setStatsPrintInterval(int statsPrintInterval) {
        this.statsPrintInterval = statsPrintInterval;
    }
}
