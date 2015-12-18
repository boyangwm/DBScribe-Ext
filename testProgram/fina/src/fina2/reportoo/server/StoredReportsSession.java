package fina2.reportoo.server;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import fina2.FinaTypeException;
import fina2.i18n.LanguagePK;
import fina2.reportoo.ReportInfo;
import fina2.security.UserPK;

public interface StoredReportsSession extends EJBObject {

    public StoredReportInfo getStoredReports(LanguagePK langPK, UserPK userPK)
            throws RemoteException, FinaTypeException;

    public void deleteStoredReports(Collection storedReports)
            throws RemoteException;

    public byte[] getStoredReport(LanguagePK langPK, ReportPK pk, int hashCode)
            throws RemoteException;

    public ReportInfo getStoredReportInfo(LanguagePK langPK, ReportPK pk,
            int hashCode) throws RemoteException;

    public void storeReport(ReportPK reportPK, LanguagePK langPK,
            UserPK userPK, ReportInfo info, byte[] report)
            throws RemoteException;
}
