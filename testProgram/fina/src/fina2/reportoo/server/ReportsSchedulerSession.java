package fina2.reportoo.server;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBObject;

import fina2.FinaTypeException;
import fina2.i18n.LanguagePK;
import fina2.reportoo.ReportInfo;
import fina2.security.UserPK;

public interface ReportsSchedulerSession extends EJBObject {

    public ScheduledReportInfo getScheduledReports(LanguagePK langPK,
            UserPK userPK) throws RemoteException, FinaTypeException;

    public void deleteScheduledReports(Collection scheduledReports)
            throws RemoteException;

    public ReportInfo getScheduledReportInfo(LanguagePK langPK, ReportPK pk,
            int hashCode) throws RemoteException;

    public void scheduleReport(fina2.i18n.LanguagePK langPK, ReportPK reportPK,
            UserPK userPK, java.util.Date scheduleDate, boolean onDemand,
            ReportInfo info) throws RemoteException;

    public void setStatus(ScheduledReportInfo scheduledReport, int status)
            throws RemoteException;

    boolean canProcess(ScheduledReportInfo scheduleInfo, Date clientTime)
            throws RemoteException;

    void processing(ScheduledReportInfo scheduleInfo) throws RemoteException;
}
