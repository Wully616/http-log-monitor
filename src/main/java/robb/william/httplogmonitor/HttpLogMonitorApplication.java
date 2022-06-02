package robb.william.httplogmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import robb.william.httplogmonitor.reader.factory.LogReaderFactory;
import robb.william.httplogmonitor.reader.strategies.ILogReader;
import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

import java.io.File;
import java.io.FileInputStream;

@SpringBootApplication
public class HttpLogMonitorApplication implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(HttpLogMonitorApplication.class);


	private String filePath;

	private String stdinPath;

	private LogReaderFactory logReaderFactory;

	public static void main(String[] args) {
		SpringApplication.run(HttpLogMonitorApplication.class, args);
	}

	public HttpLogMonitorApplication(@Value("${path.file}") String filePath, @Value("${path.stdin}") String stdinPath, LogReaderFactory logReaderFactory) {
		this.filePath = filePath;
		this.stdinPath = stdinPath;
		this.logReaderFactory = logReaderFactory;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		// Get our log reader, prioritising the applications command line argument before the stdin one if the file arg is provided
		// We use a strategy pattern to allow us to extend to different types of readers

		ReaderStrategy readerStrategy = (filePath != null && !filePath.isEmpty()) ? ReaderStrategy.FILE : ReaderStrategy.STDIN;

		if(readerStrategy == ReaderStrategy.STDIN && (stdinPath != null && !stdinPath.isEmpty())){
			//Set stdinPath property to run this to simulate passing a file to std input
			FileInputStream is = new FileInputStream(new File(stdinPath));
			System.setIn(is);
		}

		ILogReader logReader = logReaderFactory.getStrategy(readerStrategy);

		// Read the log using stream
		logReader.readLog();
	}
}
