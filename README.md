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

The application will print information to the console window in Stdout, but a full log file will still be written to `./logs/http-log-monitor.log``

## Build Instructions:

1. Downloaded minimum Java JDK 13
2. Run `./mvnw.cmd package` for windows or `./mvnw package for unix` from the project directory
3. Find the built jar in the target directory: `./target/http-log-monitor-0.0.1-SNAPSHOT.jar`
4. Run the application with `java -jar http-log-monitor-0.0.1-SNAPSHOT.jar`

## How to run

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



## Future improvements

One issue within the application is the sampling problem of the log files. The log entries can come in at different times, due to threading.
As such, since the windows and buckets move forward based on when a newer date is detected in the logs, some "older" entries could fall into a "newer" bucket.
On average for the statistics it may not be an issue since the appropriate interval time has passed from the logs reference,
and the trade off for on-time logging every 2 minutes vs the increased memory cost of waiting for a bucket to fill, without knowing the variance in log entries, is preferable.
However, it might be useful to log the variance in the log entries, as it could indicate an issue with the http thread stalling somewhere.

The application could be switched to use GraalVM to increase startup time, and reduce memory and CPU consumption.

The consumers for stats and alerting are single purpose beans, it would be good to have these loaded as pluggable jars so additional consumers
could be added without recompiling the main application.
Additionally, the Statistics consumer could be refactored allow for better configuration of the individual statistics to collect.

Since this application is using the lmax disruptor, dependency graphs for consumers could be set up to reduce any duplication of processing that each consumer does,
for example it may be beneficial to aggregate the data over a 1 second period before it is handed over to other consumers for processing.

Additional monitoring of this application could be added to monitor for slow consumers and the buffer becoming full.
The buffer size of the disruptor could also be tuned via the `--eventBuffer.size=16` parameter to tweak performance and memory consumption.


## Example output


### Statistics
```
INFO r.w.h.alerters.StatConsumer : 1549573860 : HTTP Stats for last 10 seconds from: 1549573860 to 1549573869
	Section: api
--------------------------------------------
	Total Hits: 54
	Total Bytes: 66278
	Remote Hosts: 5
	HTTP Status: {100=0, 200=45, 300=0, 400=7, 500=2}
	Request Types: {DELETE=0, GET=42, HEAD=0, OPTIONS=0, PATCH=0, POST=12, PUT=0, TRACE=0}
--------------------------------------------
	Section: report HTTP
--------------------------------------------
	Total Hits: 27
	Total Bytes: 33474
	Remote Hosts: 5
	HTTP Status: {100=0, 200=22, 300=0, 400=3, 500=2}
	Request Types: {DELETE=0, GET=18, HEAD=0, OPTIONS=0, PATCH=0, POST=9, PUT=0, TRACE=0}
```

### High Traffic Alert

#### Triggered
```
WARN r.w.h.alerters.HighTrafficConsumer : 1549573957 : High traffic generated an alert - hits = 10.008333, triggered at 1549573957
```

#### Recovered
```
INFO r.w.h.alerters.HighTrafficConsumer : 1549574049 : Recovered from high traffic - hits = 9.908334, recovered at 1549574049
```



- William Robb