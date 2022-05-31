package robb.william.httplogmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootApplication
public class HttpLogMonitorApplication {

	public static void main(String[] args) throws FileNotFoundException {
		//Run this to simulate passing a file to std input
		FileInputStream is = new FileInputStream(new File("F:\\Development\\Interviews\\http-log-monitor\\InputData\\Log_File.txt"));
		System.setIn(is);
		SpringApplication.run(HttpLogMonitorApplication.class, args);
	}

}
