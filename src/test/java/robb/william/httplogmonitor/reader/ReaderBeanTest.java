package robb.william.httplogmonitor.reader;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import robb.william.httplogmonitor.disruptor.buffer.LogEventBuffer;
import robb.william.httplogmonitor.reader.strategies.PathCsvLogReader;
import robb.william.httplogmonitor.reader.strategies.StdinCsvLogReader;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderBeanTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConditionEvaluationReportLoggingListener())  // to print out conditional config report to log
            .withUserConfiguration(MockedBuffer.class)
            .withUserConfiguration(StdinCsvLogReader.class, PathCsvLogReader.class);

    @Test
    public void whenPathPropertyEmpty_StdinBeanExists() {
        contextRunner.withPropertyValues("path.file=")
                .run(context -> assertAll(
                        () -> assertTrue(context.containsBean("stdinCsvLogReader")),
                        () -> assertFalse(context.containsBean("pathCsvLogReader"))
                ));
    }

    @Test
    public void whenPathPropertySet_PathAndStdinBeanExists() {
        contextRunner.withPropertyValues("path.file=/path/to/file")
                .run(context -> assertAll(
                        () -> assertTrue(context.containsBean("stdinCsvLogReader")),
                        () -> assertTrue(context.containsBean("pathCsvLogReader"))
                ));
    }

    @Test
    public void whenPathAndStdinPropertySet_PathBeanExists() {
        contextRunner.withPropertyValues("path.file=/path/to/file", "path.stdin=")
                .run(context -> assertAll(
                        () -> assertFalse(context.containsBean("stdinCsvLogReader")),
                        () -> assertTrue(context.containsBean("pathCsvLogReader"))
                ));
    }

    @Configuration
    protected static class MockedBuffer {
        @Bean
        public LogEventBuffer someThirdDependency() {
            return Mockito.mock(LogEventBuffer.class);
        }
    }
}
