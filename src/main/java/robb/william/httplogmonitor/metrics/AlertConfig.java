package robb.william.httplogmonitor.metrics;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertConfig {
    int alertThreshold;
    int alertSampleRate;

    public int getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(int alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public int getAlertSampleRate() {
        return alertSampleRate;
    }

    public void setAlertSampleRate(int alertSampleRate) {
        this.alertSampleRate = alertSampleRate;
    }
}
