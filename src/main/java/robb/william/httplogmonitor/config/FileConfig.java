package robb.william.httplogmonitor.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class FileConfig {
    String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
