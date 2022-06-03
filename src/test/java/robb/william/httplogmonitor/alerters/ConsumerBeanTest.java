package robb.william.httplogmonitor.alerters;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumerBeanTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConditionEvaluationReportLoggingListener())  // to print out conditional config report to log
            .withUserConfiguration(StatConsumer.class, HighTrafficConsumer.class);

    @Test
    public void whenStatPropertyFalse_BeanDoesNotExist() {
        contextRunner.withPropertyValues("stats.enabled=false")
                .run(context -> assertAll(
                        () -> assertFalse(context.containsBean("statConsumer"))
                ));
    }

    @Test
    public void whenStatPropertyTrue_BeanExists() {
        contextRunner.withPropertyValues("stats.enabled=true", "stats.printInterval=1")
                .run(context -> assertAll(
                        () -> assertTrue(context.containsBean("statConsumer"))
                ));
    }


    @Test
    public void whenAlertPropertyFalse_BeanDoesNotExist() {
        contextRunner.withPropertyValues("highTrafficAlert.enabled=false")
                .run(context -> assertAll(
                        () -> assertFalse(context.containsBean("highTrafficConsumer"))
                ));
    }

    @Test
    public void whenAlertPropertyTrue_BeanExists() {
        contextRunner.withPropertyValues("highTrafficAlert.enabled=true", "highTrafficAlert.sampleWindow=1", "highTrafficAlert.threshold=1")
                .run(context -> assertAll(
                        () -> assertTrue(context.containsBean("highTrafficConsumer"))
                ));
    }

}
