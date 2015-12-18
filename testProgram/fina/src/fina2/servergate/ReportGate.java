package fina2.servergate;

import fina2.reportoo.server.OOReportSession;
import fina2.reportoo.server.OOReportSessionHome;

public class ReportGate {

    /** Private constructor to avoid creating the instances */
    private ReportGate() {
    }

    /** Returns bank session */
    public static OOReportSession getReportSession() throws Exception {

        OOReportSessionHome home = (OOReportSessionHome) fina2.Main
                .getRemoteObject("fina2/reportoo/server/OOReportSession",
                        OOReportSessionHome.class);

        return home.create();
    }

    /** Sets the given report sequence */
    public static void setReportSequence(int reportId, int index)
            throws Exception {
        OOReportSession session = getReportSession();
        session.setReportSequence(reportId, index);
    }

}
