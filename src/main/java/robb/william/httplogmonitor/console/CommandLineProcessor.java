package robb.william.httplogmonitor.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import robb.william.httplogmonitor.config.FileConfig;
import robb.william.httplogmonitor.metrics.AlertConfig;
import robb.william.httplogmonitor.metrics.StatsConfig;
import robb.william.httplogmonitor.reader.ILogReader;
import robb.william.httplogmonitor.reader.LogReaderFactory;
import robb.william.httplogmonitor.reader.strategies.ReaderStrategy;

@Component
public class CommandLineProcessor implements ApplicationRunner {
    Logger logger = LoggerFactory.getLogger(CommandLineProcessor.class);

    private FileConfig fileConfig;
    private AlertConfig alertConfig;
    private StatsConfig statsConfig;
    private LogReaderFactory logReaderFactory;
    public CommandLineProcessor(FileConfig fileConfig, AlertConfig alertConfig, StatsConfig statsConfig, LogReaderFactory logReaderFactory) {
        this.fileConfig = fileConfig;
        this.alertConfig = alertConfig;
        this.statsConfig = statsConfig;
        this.logReaderFactory = logReaderFactory;
    }

    @Override
    public void run(ApplicationArguments args){
        logger.info(args.getOptionNames().toString());
        logger.info(args.getNonOptionArgs().toString());

        if(args.containsOption(Argument.FILE.arg)){
            logger.info(args.getOptionValues(Argument.FILE.arg).get(0).toString());
            fileConfig.setFilePath(args.getOptionValues(Argument.FILE.arg).get(0));
        }

        // Get our log reader, prioritising the applications command line argument before the stdin one if the file arg is provided
        // We use a strategy pattern to allow us to extend to different types of readers
        ReaderStrategy readerStrategy = args.containsOption(Argument.FILE.arg) ? ReaderStrategy.FILE : ReaderStrategy.STDIN;

        ILogReader logReader = logReaderFactory.getStrategy(readerStrategy);

        // Read the log using stream
        logReader.readLog();

    }

}
