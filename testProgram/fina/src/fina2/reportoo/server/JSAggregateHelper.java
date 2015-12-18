package fina2.reportoo.server;

import java.sql.SQLException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

public class JSAggregateHelper {

    public static final int BANK_MODE = 1;
    public static final int PEER_MODE = 2;
    public static final int ALL_MODE = 3;
    public static final int PCT_MODE = 4;
    public static final int VCT_MODE = 5;
    public static final int SEL_MODE = 6;

    private int mode;
    private String reportID;
    private String bankCode;
    private long bankId;
    private String peerCode;
    private String peerFunction;
    private int periodId;
    private OOPeriodPK period;
    private String periodFunction;
    private int offset;
    private String allFunction;
    private String iterName;
    private String groupByValue;
    private String bankParameterName;
    private String versionCode;
    private Object pctf[];

    private OOReportSessionBean bean;

    // No direct instancing
    private JSAggregateHelper() {
    }

    /** Creates new JSAggregateHelper for BANKVALUE*/
    public static JSAggregateHelper bankValueHelper(OOReportSessionBean bean,
            String reportID, String bankCode, int periodId,
            String periodFunction, int offset, String versionCode) {

        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.mode = BANK_MODE;
        helper.reportID = reportID;
        helper.bankCode = bankCode;
        helper.periodId = periodId;
        helper.periodFunction = periodFunction;
        helper.offset = offset;
        helper.versionCode = versionCode;

        return helper;
    }

    /** Creates new JSAggregateHelper for VCTVALUE*/
    public static JSAggregateHelper vctValueHelper(OOReportSessionBean bean,
            String reportID, String iterName, String groupByValue,
            String function) {

        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.mode = VCT_MODE;
        helper.reportID = reportID;
        helper.iterName = iterName;
        helper.groupByValue = groupByValue;
        helper.allFunction = function;

        return helper;
    }

    /** Creates new JSAggregateHelper for PEERVALUE*/
    public static JSAggregateHelper peerValueHelper(OOReportSessionBean bean,
            String reportID, String peerCode, String peerFunction,
            int periodId, String periodFunction, int offset, String versionCode) {

        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.mode = PEER_MODE;
        helper.reportID = reportID;
        helper.peerCode = peerCode;
        helper.peerFunction = peerFunction;
        helper.periodId = periodId;
        helper.periodFunction = periodFunction;
        helper.offset = offset;
        helper.versionCode = versionCode;

        return helper;
    }

    /** Creates new JSAggregateHelper for ALLBANKSVALUE*/
    public static JSAggregateHelper allBankHelper(OOReportSessionBean bean,
            String reportID, String allFunction, int periodId,
            String periodFunction, int offset, String versionCode) {

        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.mode = ALL_MODE;
        helper.reportID = reportID;
        helper.allFunction = allFunction;
        helper.periodId = periodId;
        helper.periodFunction = periodFunction;
        helper.offset = offset;
        helper.versionCode = versionCode;

        return helper;
    }

    /** Creates new JSAggregateHelper for PCTVALUE*/
    public static JSAggregateHelper pctValueHelper(OOReportSessionBean bean,
            String reportID, long bankId, OOPeriodPK period,
            String periodFunction, String versionCode) {

        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.mode = PCT_MODE;
        helper.reportID = reportID;
        helper.bankId = bankId;
        helper.period = period;
        helper.periodFunction = periodFunction;
        helper.versionCode = versionCode;
        return helper;
    }

    /** Creates new JSAggregateHelper for SELBANKSVALUE*/
    public static JSAggregateHelper selBankHelper(OOReportSessionBean bean,
            String reportID, String bankParameterName, String allFunction,
            int periodId, String periodFunction, int offset) {
        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.mode = SEL_MODE;
        helper.reportID = reportID;
        helper.bankParameterName = bankParameterName;
        helper.allFunction = allFunction;
        helper.periodId = periodId;
        helper.periodFunction = periodFunction;
        helper.offset = offset;
        return helper;
    }

    /** Creates new JSAggregateHelper for PCTVALEX*/
    public static JSAggregateHelper pctValExHelper(OOReportSessionBean bean,
            String reportID, String bankCode, String versionCode, Object pctf[]) {

        JSAggregateHelper helper = new JSAggregateHelper();
        helper.bean = bean;
        helper.reportID = reportID;
        helper.bankCode = bankCode;
        helper.versionCode = versionCode;
        helper.pctf = pctf;
        return helper;
    }

    public double getBankValuesForPct(int argIdx) throws Exception {

        if (argIdx >= 0 && argIdx < pctf.length) {
            PctParameters params = bean
                    .parsePctParameters((String) pctf[argIdx]);
            return bean.getBankValuesForPct(reportID, params.nodeCode,
                    bankCode, params.period, params.periodFunction,
                    params.periodOffset, versionCode);
        } else {
            throw new IllegalStateException("Missing parameters for argument {"
                    + argIdx + " }");
        }
    }

    public double getValue(String nodeCode) throws SQLException {
        double d = Double.NaN;
        switch (mode) {
        case BANK_MODE:
            d = bean.bankvaluever(reportID, nodeCode, bankCode, periodId,
                    periodFunction, offset, versionCode);
            break;
        case PEER_MODE:
            d = bean.peervaluever(reportID, nodeCode, peerCode, peerFunction,
                    periodId, periodFunction, offset, versionCode);
            break;
        case ALL_MODE:
            d = bean.allbanksvaluever(reportID, nodeCode, allFunction,
                    periodId, periodFunction, offset, versionCode);
            break;
        case VCT_MODE:
            d = bean.vctvalue(reportID, nodeCode, iterName, groupByValue,
                    allFunction);
            break;
        case PCT_MODE:
            d = bean.loadBankValuesForPct(reportID, nodeCode, period, bankId,
                    periodFunction, versionCode);
            break;
        case SEL_MODE:
            d = bean.selbanksvaluever(reportID, nodeCode, bankParameterName,
                    allFunction, periodId, periodFunction, offset, versionCode);
            break;
        }

        if (Double.isNaN(d)) {
            try {
                d = Double.valueOf(nodeCode).doubleValue();
            } catch (Exception e) {
                d = 0;
            }
        }

        return d;
    }

    public double sign(double d) {
        if (d == 0)
            return 0;
        if (d > 0)
            return 1;
        else
            return -1;
    }

    public static double evalReportAggregate(
            fina2.reportoo.server.JSAggregateHelper helper, String source)
            throws JavaScriptException {
        source = "function report_aggregate() {\n" + source + "\n}";

        Context cx = Context.enter();

        try {
            Scriptable scope = cx.initStandardObjects(null);

            //JSTreeInterceptor tree = new JSTreeInterceptor();

            Scriptable jsArgs = Context.toObject(helper, scope);
            scope.put("helper", scope, jsArgs);

            cx.evaluateString(scope, source, "<script>", 1, null);

            Object f = scope.get("report_aggregate", scope);
            Object[] functionArgs = new Object[0];
            Object result = ((Function) f).call(cx, scope, scope, functionArgs);

            //return tree.getDependentNodes();

            String res = Context.toString(result);
            Context.exit();
            return Double.valueOf(res).doubleValue();
        } catch (JavaScriptException e) {
            Context.exit();
            throw e;
        } catch (Exception e) {
            return 0; //Double.NaN;
        }
    }
}
