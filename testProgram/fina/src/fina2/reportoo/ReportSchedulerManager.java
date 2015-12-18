package fina2.reportoo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

import fina2.Main;
import fina2.i18n.LanguagePK;
import fina2.reportoo.server.ReportPK;
import fina2.reportoo.server.ReportsSchedulerSession;
import fina2.reportoo.server.ReportsSchedulerSessionHome;
import fina2.reportoo.server.ScheduledReportInfo;
import fina2.security.UserPK;

public class ReportSchedulerManager {

    public static final String SCHEDULED_REPORT_INFO = "ScheduledReportInfo";

    private static SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();

    private static ReportSchedulerManager instance;

    private Scheduler scheduler;

    private LanguagePK langPK;

    private UserPK userPK;

    private static ProgressControler progressControler;

    protected ReportSchedulerManager(LanguagePK langPK, UserPK userPK,
            ProgressControler pc) {

        this.langPK = langPK;
        this.userPK = userPK;
        this.progressControler = pc;
    }

    public static ReportSchedulerManager getInstance(LanguagePK langPK,
            UserPK userPK, ProgressControler pc) {

        if (instance == null) {
            instance = new ReportSchedulerManager(langPK, userPK, pc);
            instance.loadJobs();
        }
        return instance;
    }

    public static ReportSchedulerManager getInstance() {
        return instance;
    }

    public void loadJobs() {
        try {
            if (scheduler == null) {
                scheduler = schedulerFactory.getScheduler();
                scheduler.start();
            }

            ReportsSchedulerSession reportsScheduler = getScheduledReportSession();
            ScheduledReportInfo root = reportsScheduler.getScheduledReports(
                    langPK, userPK);

            ArrayList scheduledReports = new ArrayList();
            getScheduledReports(root, scheduledReports);

            initJobs(scheduledReports);
        } catch (Exception e) {
           // Main.generalErrorHandler(e);
            return;
        }
    }

    public ScheduledReportInfo getScheduledReports() {

        ReportsSchedulerSession reportsScheduler = getScheduledReportSession();
        ScheduledReportInfo root = null;
        try {
            root = reportsScheduler.getScheduledReports(langPK, userPK);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return null;
        }

        ArrayList scheduledReports = new ArrayList();
        getScheduledReports(root, scheduledReports);

        initJobs(scheduledReports);

        return root;
    }

    public ReportInfo getScheduledReportInfo(ScheduledReportInfo scheduledReport)
            throws Exception {

        ReportsSchedulerSession reportsScheduler = getScheduledReportSession();

        return reportsScheduler.getScheduledReportInfo(new LanguagePK(
                scheduledReport.getLangId()), new ReportPK(scheduledReport
                .getReportId()), scheduledReport.getReportInfoHashCode());
    }

    public void deleteScheduledReports(Collection scheduledReports) {
        try {
            ReportsSchedulerSession reportsScheduler = getScheduledReportSession();
            reportsScheduler.deleteScheduledReports(scheduledReports);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return;
        }
    }

    public void scheduleReport(LanguagePK langPK, ReportPK reportPK,
            UserPK userPK, Date scheduleTime, boolean onDemand, ReportInfo info) {
        try {
            ReportsSchedulerSession reportsScheduler = getScheduledReportSession();
            reportsScheduler.scheduleReport(langPK, reportPK, userPK,
                    scheduleTime, onDemand, info);
            getScheduledReports();
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return;
        }
    }

    public static synchronized void runScheduledReportGeneration(
            final ScheduledReportInfo sri) {

        try {
            final ReportsSchedulerSession reportScheduler = getScheduledReportSession();
            LanguagePK langPK = new LanguagePK(sri.getLangId());
            ReportPK reportPK = new ReportPK(sri.getReportId());
            UserPK userPK = new UserPK(sri.getUserId());
            int hashCode = sri.getReportInfoHashCode();

            if (reportScheduler.canProcess(sri, new Date())) {

                Thread th = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                reportScheduler.processing(sri);
                                Thread.sleep(10000);
                            } catch (Exception ex) {
                                return;
                            }
                        }
                    }
                });
                th.start();

                try {
                    ReportInfo info = reportScheduler.getScheduledReportInfo(
                            langPK, reportPK, hashCode);

                    ReportGenerator rg = ReportGenerator.getInstance();
                    rg.generate(langPK, reportPK, userPK, info,
                            progressControler, true);

                    reportScheduler.setStatus(sri,
                            ScheduledReportInfo.STATUS_DONE);

                } catch (Exception ex) {
                    if (reportScheduler != null) {
                        reportScheduler.setStatus(sri,
                                ScheduledReportInfo.STATUS_ERROR);
                    }
                    throw ex;
                } finally {
                    if (th != null) {
                        th.interrupt();
                    }
                }
            }
        } catch (Exception ex) {
            Main.generalErrorHandler(ex);
        }
    }

    private void initJobs(Collection scheduledReports) {

        deleteAllPreviousJobs();

        for (Iterator iter = scheduledReports.iterator(); iter.hasNext();) {
            ScheduledReportInfo sheduledReport = (ScheduledReportInfo) iter
                    .next();

            JobDetail jobDetail = new JobDetail("scheduledReport"
                    + sheduledReport.hashCode(), Scheduler.DEFAULT_GROUP, // job group
                    ScheduledReportJob.class); // the java class to execute

            jobDetail.getJobDataMap()
                    .put(SCHEDULED_REPORT_INFO, sheduledReport);

            SimpleTrigger trigger = new SimpleTrigger("scheduledReport"
                    + sheduledReport.hashCode(), Scheduler.DEFAULT_GROUP,
                    sheduledReport.getScheduleTime(), null, 0, 0L);
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (Exception e) {
                Main.generalErrorHandler(e);
            }
        }
    }

    private void deleteAllPreviousJobs() {
        try {
            String names[] = scheduler.getJobNames(Scheduler.DEFAULT_GROUP);
            for (int i = 0; i < names.length; i++) {
                scheduler.deleteJob(names[i], Scheduler.DEFAULT_GROUP);
            }
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    private void getScheduledReports(ScheduledReportInfo sri,
            Collection scheduledReports) {

        if (sri.isFolder()) {
            for (Iterator iter = sri.getChildren().iterator(); iter.hasNext();) {
                ScheduledReportInfo sheduledReport = (ScheduledReportInfo) iter
                        .next();
                getScheduledReports(sheduledReport, scheduledReports);
            }
        } else if (!sri.isOnDemand()
                && sri.getStatus() != ScheduledReportInfo.STATUS_DONE) {
            scheduledReports.add(sri);
        }
    }

    private static ReportsSchedulerSession getScheduledReportSession() {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi
                    .lookup("fina2/reportoo/server/ReportsSchedulerSession");
            ReportsSchedulerSessionHome home = (ReportsSchedulerSessionHome) PortableRemoteObject
                    .narrow(ref, ReportsSchedulerSessionHome.class);

            ReportsSchedulerSession session = home.create();

            return session;
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return null;
        }
    }
}
