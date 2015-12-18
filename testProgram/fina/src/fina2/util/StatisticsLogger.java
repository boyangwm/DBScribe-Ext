package fina2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class StatisticsLogger {

	private static ThreadLocal<StatisticsLogger> statLogger = 
		new ThreadLocal<StatisticsLogger>();
	
    private Logger log;

    private List<StageInfo> stageInfos;

    private String message;

    private Priority priority;
    
    public static StatisticsLogger getLogger() {
    	return getLogger(true);
    }

    public static StatisticsLogger getLogger(boolean create) {
    	StatisticsLogger logger = statLogger.get();
    	if (logger == null && create == true) {
    		logger = new StatisticsLogger();
    		setLogger(logger);
    	}
    	return logger;
    }
    
    public static void setLogger(StatisticsLogger logger) {
    	statLogger.set(logger);
    }
    
    public StatisticsLogger(String message, Logger log, Priority priority) {
        this.stageInfos = new ArrayList<StageInfo>();
        this.message = message;
        this.log = log;
        this.priority = priority;
    }

    public StatisticsLogger(String message) {
        this(message, Logger.getLogger(StatisticsLogger.class), Level.INFO);
    }
    
    public StatisticsLogger() {
    	this("Statistics");
    }
    
    public void logStage(String stageName, Logger log, Priority priority) {
    	log.l7dlog(priority, stageName, null);
        stageInfos.add(new StageInfo(stageName, System.currentTimeMillis()));
    }

    public void logStage(String stageName, Logger log) {
    	logStage(stageName, log, priority);
    }
    
    public void logStage(String stageName) {
    	logStage(stageName, log, priority);
    }
    
    public void logMessage(String message, Logger log, Priority priority) {
    	log.l7dlog(priority, message, null);
    }

    public void logMessage(String message, Logger log) {
    	logMessage(message, log, priority);
    }
    
    public void logMessage(String message, Priority priority) {
    	logMessage(message, log, priority);
    }
    
    public void logMessage(String message) {
    	logMessage(message, log, priority);
    }

    public void reset() {
        stageInfos.clear();
    }

    public void logSummary() {

        if (stageInfos.size() > 0) {

        	String line = getLine(message.length() + 2);
            log.l7dlog(priority, "/" + line + "/", null);
            log.l7dlog(priority, "/ " + message + " /", null);
            log.l7dlog(priority, "/" + line + "/", null);
            StageInfo firstStage = (StageInfo)stageInfos.get(0);
            long currentTime = System.currentTimeMillis();
            long totalDuration = currentTime - firstStage.getStartTime();

            for (int i = 0; i < stageInfos.size(); i++) {

                StageInfo stage = (StageInfo)stageInfos.get(i);
                long stageEndTime;
                if (i < stageInfos.size() - 1) {
                    stageEndTime = ((StageInfo)stageInfos.get(i + 1)).getStartTime();
                } else {
                    stageEndTime = currentTime;
                }

                long duration = stageEndTime - stage.getStartTime();

                StringBuffer buff = new StringBuffer();
                buff.append("Stage: \"");
                buff.append(stage.getName());
                buff.append("\", duration: ");
                buff.append(duration);
                buff.append("ms (");
                buff.append((int)((double)duration / totalDuration * 100));
                buff.append("%)");
                log.l7dlog(priority, buff.toString(), null);
            }

            StringBuffer buff = new StringBuffer();
            buff.append("Total time: ");
            buff.append(totalDuration);
            buff.append("ms, stage total count: ");
            buff.append(stageInfos.size());
            buff.append(", stage average duration: ");
            buff.append(totalDuration / stageInfos.size());
            buff.append("ms");

            log.l7dlog(priority, buff.toString(), null);

            stageInfos.clear();
        }
    }
    
    private String getLine(int size) {
    	char[] chars = new char[size];
    	Arrays.fill(chars, '-');
    	return new String(chars);
    }

    class StageInfo {

        public String name;

        public long startTime;

        public StageInfo(String name, long startTime) {
            this.name = name;
            this.startTime = startTime;
        }

        public String getName() {
            return name;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
