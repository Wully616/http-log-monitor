package robb.william.httplogmonitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class HttpLogMonitorApplicationTests {

	@SpyBean
	HttpLogMonitorApplication commandLineProcessor;

	@Test
	void whenContextLoads_CmdRunnerRuns() throws Exception {
		verify(commandLineProcessor, times(1)).run(any());
	}

}
