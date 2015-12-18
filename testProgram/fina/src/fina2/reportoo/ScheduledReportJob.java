package fina2.reportoo;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fina2.reportoo.server.ScheduledReportInfo;

public class ScheduledReportJob implements Job {

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        ScheduledReportInfo sri = null;
        sri = (ScheduledReportInfo) context.getJobDetail().getJobDataMap().get(
                ReportSchedulerManager.SCHEDULED_REPORT_INFO);

        ReportSchedulerManager.runScheduledReportGeneration(sri);
    }
}
