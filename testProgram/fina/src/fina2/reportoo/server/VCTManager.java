package fina2.reportoo.server;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;

import fina2.ui.sheet.openoffice.OOIterator;

public class VCTManager {

    /**
     * Sessions used to distinguish VCTManager objects by reportdId
     */
    private static Hashtable sessions = new Hashtable();

    private Hashtable vctProcessors = new Hashtable();

    /**
     * Direct instancing is not allowed
     */
    private VCTManager() {
    }

    public static VCTManager getInstance(String reportId) {

        VCTManager retVal = (VCTManager) sessions.get(reportId);

        if (retVal == null) {
            retVal = new VCTManager();
            sessions.put(reportId, retVal);
        }

        return retVal;
    }

    public static void clean(String reportId) {
        sessions.remove(reportId);
    }

    public List processVCTIterator(OOIterator vctIterator, Connection con)
            throws Exception {
        VCTProcessor vp = VCTProcessor.newInstance(vctIterator, con);
        vctProcessors.put(vctIterator.getName().trim(), vp);
        return vp.getVctIteratorValues();
    }

    public double getVctFunctionResult(String iterName, String node,
            String agregate, String function) {

        double retVal = Double.NaN;

        VCTProcessor vctProc = (VCTProcessor) vctProcessors
                .get(iterName.trim());
        if (vctProc != null) {
            retVal = vctProc.getVctFunctionResult(node, agregate, function);
        }

        return retVal;
    }
}
