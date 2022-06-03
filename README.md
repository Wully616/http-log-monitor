# http-log-monitor
HTTP log monitoring console program

This reads a Common Log Formatted HTTP log file in the following format:

```
"remotehost","rfc931","authuser","date","request","status","bytes"
"10.0.0.1","-","apache",1549574332,"GET /api/user HTTP/1.0",200,1234
```

It can be provided a log file to monitor by piping it to the application via StdIn,
or by passing the path to the file as an application argument.
 
It will print statistics about the web traffic every time an interval in `seconds` passes in the logs.

It will print an alert when the average traffic over a window in `seconds` in the logs is higher than a threshold in `hits per second`.
The alert will recover when the average traffic has dropped below the threshold.


# Build Instructions:

1. Downloaded minimum Java JDK 13
2. Run `./mvnw.cmd package` for windows or `./mvnw package for unix` from the project directory
3. Find the built jar in the target directory: `./target/http-log-monitor-0.0.1-SNAPSHOT.jar`
4. Run the application with `java -jar http-log-monitor-0.0.1-SNAPSHOT.jar`

# How to run

Pass the file in to monitor using `--path.file=./path/to/file.csv`

`java -jar http-log-monitor-0.0.1-SNAPSHOT.jar --path.file=./InputData/Log_File.txt`

Or pass the file via stdin

`cat ./InputData/Log_File.txt | java -jar http-log-monitor-0.0.1-SNAPSHOT.jar`

## Arguments

### High Traffic Alert
`--highTrafficAlert.enabled=<true/false>` : Enable or disable the high traffic alert. **Default enabled**

`--highTrafficAlert.threshold=10` : Set the alert thresholds `in hits per second`. **Default 10**

`--highTrafficAlert.sampleWindow=120` : Set the window in `seconds` to sample the average traffic. **Default 120**

### Statistics
 `--stats.enabled=<true/false>` : Enable or disable the statistics printing. **Default enabled**
 
 `--stats.printInterval=10` : Set how often to print statistics collected since the last print. **Default 10**



### notes
The apache log entries can come in at different times. due to threading.

It might be useful to log the variance in the log entires? As it could indicate an issue with the http thread stalling somewhere

use grallvm to reduce memory/cpu

make the modules for stats and alerting more dynamic so perhaps plugins could be loaded in.

allow for better configuration of the individual statistics to collect