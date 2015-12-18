package fina2.reportoo;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import com.sun.star.comp.beans.LocalOfficeConnection;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.sheet.addin.InvokedFunction;
import com.sun.star.sheet.addin.XFinaAddinMgmt;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import fina.addin.FinaAddInMode;
import fina2.Main;
import fina2.i18n.LanguagePK;
import fina2.period.PeriodPK;
import fina2.reportoo.repository.Formula;
import fina2.reportoo.repository.RepositorySession;
import fina2.reportoo.repository.RepositorySessionHome;
import fina2.reportoo.server.OOReportSession;
import fina2.reportoo.server.OOReportSessionHome;
import fina2.reportoo.server.Report;
import fina2.reportoo.server.ReportHome;
import fina2.reportoo.server.ReportPK;
import fina2.reportoo.server.StoredReportsSession;
import fina2.reportoo.server.StoredReportsSessionHome;
import fina2.security.UserPK;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.sheet.openoffice.OOIterator;

public class ReportGenerator {

    private static Logger log = Logger.getLogger(ReportGenerator.class);

    private static ReportGenerator instance;

    private static final int STAGES_COUNT = 8;

    private byte[] reportResult;

    private ArrayList stageInfos;

    private XFinaAddinMgmt addinMgmt;

    private Spreadsheet sheet;

    private ProgressControler progressControler;

    private LanguagePK langPK;

    private ReportPK reportPK;

    private UserPK userPK;

    private ReportInfo info;

    private OOReportSession reportSession;

    private StoredReportsSession storedReportsSession;

    private Collection formulas;

    private LocalOfficeConnection officeConnection;

    private ReportGenerator() {
    }

    public static ReportGenerator getInstance() {
        if (instance == null) {
            instance = new ReportGenerator();
        }
        return instance;
    }

    public synchronized boolean generate(LanguagePK langPK, ReportPK reportPK,
            UserPK userPK, ReportInfo info, ProgressControler pc,
            boolean regenerate) throws Exception {

        boolean regenerated = false;

        try {
            init(langPK, reportPK, userPK, info, pc);

            if (regenerate || !isGenerated()) {

                regenerated = true;

                preGeneration();
                generate();
            }
            logStatistics();

        } catch (Throwable th) {
            progressControler.setMessage("Error");
            log.error("Error during report generation", th);
            throw new Exception("Error occured during report generation",th);
        } finally {
            postGeneration();
        }

        return regenerated;
    }

    public byte[] getResult() {
        return this.reportResult;
    }

    private void init(LanguagePK langPK, ReportPK reportPK, UserPK userPK,
            ReportInfo info, ProgressControler pc) {

        this.progressControler = pc;
        this.progressControler.setMaxProgress(STAGES_COUNT);

        this.langPK = langPK;
        this.reportPK = reportPK;
        this.userPK = userPK;
        this.info = info;
        this.sheet = null;
        this.reportResult = null;
        this.formulas = null;
        this.stageInfos = new ArrayList();
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/reportoo/server/OOReportSession");
            OOReportSessionHome reportSessionHome = (OOReportSessionHome) PortableRemoteObject
                    .narrow(ref, OOReportSessionHome.class);

            this.reportSession = reportSessionHome.create();

            Object ref2 = jndi
                    .lookup("fina2/reportoo/server/StoredReportsSession");
            StoredReportsSessionHome home = (StoredReportsSessionHome) PortableRemoteObject
                    .narrow(ref2, StoredReportsSessionHome.class);

            this.storedReportsSession = home.create();

            calcVCTIterators();
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    private void preGeneration() throws Exception {

        initAddinMgmt();

        initSheet();

        reportSession.preCalculation(this.sheet.getID(), langPK, info);

        addinMgmt.setMode(FinaAddInMode.GENERATOR_MODE.getMode());

        processRepositoryFunctions();

        processIterators();
    }

    private void generate() throws Exception {

        initStage("Caching report functions and it's arguments");
        sheet.recalculate();

        boolean incProgress = true;
        while (addinMgmt.isInvokedFunctions()) {

            initStage("Calculating cached report functions", incProgress);
            InvokedFunction[] results = calculate(addinMgmt
                    .getInvokedFunctions());
            addinMgmt.setFunctionInvocationResults(results);

            incProgress = false;
            sheet.recalculate();
        }

        sheet.convertToValues();

        storeSheet();
    }

    private void postGeneration() {
        try {
            if (addinMgmt != null) {
                addinMgmt.clean();
                addinMgmt.setMode(FinaAddInMode.DESIGN_MODE.getMode());
            }

            if (officeConnection != null) {
                officeConnection.dispose();
            }

            if (sheet != null) {
                reportSession.postCalculation(sheet.getID());
                sheet.dispose();
            }
        } catch (Throwable th) {
            log.error(th);
        }
    }

    private void storeSheet() throws IOException {

        initStage("Storing generated report");

        this.reportResult = sheet.getDocumentContent();

        storedReportsSession.storeReport(this.reportPK, this.langPK,
                this.userPK, this.info, this.reportResult);
    }

    private void initSheet() {
        try {
            if (sheet == null) {

                initStage("Getting report template");
                Report report = getReport(this.reportPK);

                initStage("Initializing OO sheet");
                sheet = SpreadsheetsManager.getInstance().createSpreadsheet(
                        report.getTemplate());

                sheet.setAutoCalculate(false);

                // Copy language specific strings
                byte[] langReport = report.getLangTemplate(this.langPK);
                if (langReport != null) {

                    Spreadsheet langSheet = SpreadsheetsManager.getInstance()
                            .createSpreadsheet(langReport);

                    langSheet.setAutoCalculate(false);
                    langSheet.clearFormulas(0, 0, langSheet.getLastRow(),
                            langSheet.getLastCol());
                    langSheet.copyStrings(sheet);
                    langSheet.dispose();
                }
            }
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return;
        }
    }

    private Report getReport(ReportPK reportPK) {
        try {
            InitialContext jndi = new InitialContext();
            Object reportRef = jndi.lookup("fina2/reportoo/server/Report");

            ReportHome reportHome = (ReportHome) PortableRemoteObject.narrow(
                    reportRef, ReportHome.class);

            return reportHome.findByPrimaryKey(reportPK);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return null;
        }
    }

    private void processIterators() {

        if (info.footer == 0) {
            info.footer = 500;
        }

        initStage("Processing iterators");
        Hashtable iters = info.iterators;
        Hashtable sort = new Hashtable();
        for (java.util.Iterator iter = iters.values().iterator(); iter
                .hasNext();) {
            OOIterator it = (OOIterator) iter.next();
            it.setSheet(sheet);

            int i = it.getEnd() - it.getStart();

            Vector v = (Vector) sort.get(new Integer(i));
            if (v == null) {
                v = new Vector();
                sort.put(new Integer(i), v);
            }
            v.add(it);
        }
        Vector _iters = new Vector();
        for (int i = 0; _iters.size() < iters.size(); i++) {
            Vector v = (Vector) sort.get(new Integer(i));
            if (v != null) {
                _iters.addAll(v);
            }
        }
        for (java.util.Iterator iter = _iters.iterator(); iter.hasNext();) {
            OOIterator it = (OOIterator) iter.next();

            int orientation = it.getOrientation();
            int s = it.getStart();
            int e = it.getEnd();

            int size = e - s + 1; // number of cells in iterator
            int vsize = it.getValues().size(); // number of items in values
            int isize = size; // / vsize; // number of cells per item

            if (orientation == it.ROW_ITERATOR) {
                if (it.getValues().size() > 1) {
                    for (int i = it.getValues().size() - 1; i >= 0; i--) {
                        if (i == 0) {
                            replaceFormula(sheet, s, 0, e, sheet.getLastCol(),
                                    it, i);
                        } else {
                            sheet.insertRange(e + 1, 0, e + isize, 0,
                                    sheet.INSERT_ROWS);
                            sheet.copyRange(s, 0, e, 250, e + 1, 0);

                            replaceFormula(sheet, e + 1, 0, e + isize + 1,
                                    sheet.getLastCol(), it, i);
                        }
                    }
                } else {
                    replaceFormula(sheet, s, 0, e, sheet.getLastCol(), it, 0);
                }
            } else {
                if (it.getValues().size() > 1) {
                    for (int i = it.getValues().size() - 1; i >= 0; i--) {

                        int row = info.footer;
                        if (info.footer > sheet.getLastRow())
                            row = sheet.getLastRow();

                        if (i == 0) {
                            replaceFormula(sheet, info.header, s, row, e, it, i);
                        } else {
                            sheet.insertRange(info.header, e + 1, info.footer,
                                    e + isize, sheet.INSERT_COLUMNS);

                            sheet.copyRange(info.header, s, info.footer, e,
                                    info.header, e + 1);

                            for (int iw = s; iw <= e; iw++) {
                                int w = sheet.getColWidth(info.header, iw,
                                        info.footer, iw);
                                sheet.setColWidth(info.header,
                                        e + 1 + (iw - s), info.footer - 2, e
                                                + 1 + (iw - s), w);
                            }

                            replaceFormula(sheet, info.header, e + 1, row, e
                                    + isize + 1, it, i);
                        }
                    }
                } else {

                    int row = info.footer;
                    if (info.footer > sheet.getLastRow())
                        row = sheet.getLastRow();

                    replaceFormula(sheet, info.header, s, row, e, it, 0);
                }
            }
        }

        for (java.util.Iterator iter = info.parameters.values().iterator(); iter
                .hasNext();) {
            fina2.ui.sheet.Parameter p = (fina2.ui.sheet.Parameter) iter.next();
            replaceParamFormula(sheet, 0, 0, sheet.getLastRow(), sheet
                    .getLastCol(), p, 0);
        }
    }

    private void replaceFormula(fina2.ui.sheet.Spreadsheet sheet, int r1,
            int c1, int r2, int c2, fina2.ui.sheet.Iterator it, int i) {

        switch (it.getType()) {
        case fina2.ui.sheet.Iterator.PERIOD_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curperiod()",
                    "CURPERIOD()", String.valueOf(((PeriodPK) ((Vector) it
                            .getValues()).get(i)).getId()), false);
            break;
        case fina2.ui.sheet.Iterator.BANK_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
                    //"com.sun.star.sheet.addin.CalcAddins.curbank()",
                    "CURBANK()",
                    "\"" + ((Vector) it.getValues()).get(i) + "\"", false);
            break;
        case fina2.ui.sheet.Iterator.NODE_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curnode()",
                    "CURNODE()", "\""
                            + ((Vector) it.getValues()).get(i).toString()
                            + "\"", false);
            break;
        case fina2.ui.sheet.Iterator.OFFSET_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
                    //"com.sun.star.sheet.addin.CalcAddins.curoffset()",
                    "CUROFFSET()", ((Vector) it.getValues()).get(i).toString(),
                    false);
            break;
        case fina2.ui.sheet.Iterator.PEER_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curpeergroup()",
                    "CURPEERGROUP()", "\""
                            + ((Vector) it.getValues()).get(i).toString()
                            + "\"", false);
            break;
        case fina2.ui.sheet.Iterator.VCT_ITERATOR:
            Object value = "#N/A";
            Vector values = (Vector) it.getValues();
            if (values != null && values.size() > i)
                value = values.get(i);

            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curvctvalue()",
                    "CURVCTVALUE()", "\"" + value + "\"", false);
            break;
        }
    }

    private void replaceParamFormula(fina2.ui.sheet.Spreadsheet sheet, int r1,
            int c1, int r2, int c2, fina2.ui.sheet.Parameter it, int i) {
        switch (it.getType()) {
        case fina2.ui.sheet.Iterator.PERIOD_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curperiod()",
                    "CURPERIOD()", String.valueOf(((PeriodPK) ((Vector) it
                            .getValues()).get(i)).getId()), false);
            break;
        case fina2.ui.sheet.Iterator.BANK_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
                    //"com.sun.star.sheet.addin.CalcAddins.curbank()",
                    "CURBANK()",
                    "\"" + ((Vector) it.getValues()).get(i) + "\"", false);
            break;
        case fina2.ui.sheet.Iterator.NODE_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curnode()",
                    "CURNODE()", "\""
                            + ((Vector) it.getValues()).get(i).toString()
                            + "\"", false);
            break;
        case fina2.ui.sheet.Iterator.OFFSET_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
                    //"com.sun.star.sheet.addin.CalcAddins.curoffset()",
                    "CUROFFSET()", ((Vector) it.getValues()).get(i).toString(),
                    false);
            break;
        case fina2.ui.sheet.Iterator.PEER_ITERATOR:
            sheet.replaceFunction(r1, c1, r2, c2,
            //"com.sun.star.sheet.addin.CalcAddins.curpeergroup()",
                    "CURPEERGROUP()", "\""
                            + ((Vector) it.getValues()).get(i).toString()
                            + "\"", false);
            break;
        }
    }

    private void calcVCTIterators() {

        initStage("Calculating VCT iterator value", false);
        if (info == null)
            return;

        ArrayList vctIterators = new ArrayList();

        for (Iterator iter = info.iterators.values().iterator(); iter.hasNext();) {
            OOIterator vctIter = (OOIterator) iter.next();
            if (vctIter.getType() == vctIter.VCT_ITERATOR) {

                vctIterators.add(vctIter);
            }
        }

        if (vctIterators.size() > 0) {

            initSheet();
            List values = null;
            try {
                values = reportSession.calculateVCTValues(this.sheet.getID(),
                        vctIterators);
            } catch (Exception e) {
                Main.generalErrorHandler(e);
            }
            for (int i = 0; i < vctIterators.size(); i++) {
                OOIterator vctIter = (OOIterator) vctIterators.get(i);
                vctIter.setValues((Collection) values.get(i));
            }
        }
    }

    private void initAddinMgmt() {
        try {
            officeConnection = new LocalOfficeConnection();
            officeConnection
                    .setUnoUrl("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");

            XComponentContext xRemoteContext = officeConnection
                    .getComponentContext();
            XMultiComponentFactory xRemoteServiceManager = xRemoteContext
                    .getServiceManager();

            Object fina = xRemoteServiceManager.createInstanceWithContext(
                    "com.sun.star.sheet.addin.CalcAddins", xRemoteContext);

            this.addinMgmt = (XFinaAddinMgmt) UnoRuntime.queryInterface(
                    XFinaAddinMgmt.class, fina);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    private InvokedFunction[] calculate(InvokedFunction[] invFuncks)
            throws RemoteException {

        return getInvokedFunctions(reportSession.calculate(this.sheet.getID(),
                getFinaFunctions(invFuncks)));
    }

    private void initStage(String stageName) {

        initStage(stageName, true);
    }

    private void initStage(String stageName, boolean incProgress) {

        log.info(stageName);

        progressControler.setMessage(stageName);
        if (incProgress) {
            progressControler.incProgress();
        }

        StageInfo stageInfo = new StageInfo();

        stageInfo.stageName = stageName;
        stageInfo.stageStartTime = System.currentTimeMillis();

        stageInfos.add(stageInfo);
    }

    private FinaFunction[] getFinaFunctions(InvokedFunction[] invFuncks) {

        FinaFunction[] finaFuncs = new FinaFunction[invFuncks.length];

        for (int i = 0; i < invFuncks.length; i++) {

            finaFuncs[i] = new FinaFunction();
            finaFuncs[i].setFunctionName(invFuncks[i].functionName);
            finaFuncs[i].setArguments(invFuncks[i].arguments);
            finaFuncs[i].setResult(invFuncks[i].result);
        }

        return finaFuncs;
    }

    private InvokedFunction[] getInvokedFunctions(FinaFunction[] finaFuncks) {

        InvokedFunction[] invFuncks = new InvokedFunction[finaFuncks.length];

        for (int i = 0; i < invFuncks.length; i++) {

            invFuncks[i] = new InvokedFunction();
            invFuncks[i].functionName = finaFuncks[i].getFunctionName();
            invFuncks[i].arguments = finaFuncks[i].getArguments();
            invFuncks[i].result = finaFuncks[i].getResult();
        }

        return invFuncks;
    }

    private boolean isGenerated() {
        try {
            initStage("Getting previously generated report");
            this.reportResult = storedReportsSession.getStoredReport(langPK,
                    reportPK, info.hashCode());

            return (this.reportResult != null);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return false;
        }
    }

    private void processRepositoryFunctions() throws Exception {

        for (int r = 0; r <= sheet.getLastRow(); r++) {
            for (int c = 0; c <= sheet.getLastCol(); c++) {

                String cellFormula = sheet.getCellFormula(r, c);
                if (cellFormula
                        .startsWith("=com.sun.star.sheet.addin.CalcAddins.repository(")) {

                    Formula formula = getFormula(getFormulaId(cellFormula));
                    Hashtable values = parseFormulaParams(cellFormula);

                    String formulaScript = formula.getFormula();
                    String script = "";

                    for (int i = 0; i < formulaScript.length(); i++) {
                        char ch = formulaScript.charAt(i);
                        script += ch;
                        String param = isRepositoryParam(script, values
                                .keySet().iterator());
                        if (param != null) {
                            script = script.substring(0, script.length()
                                    - param.length() - 1)
                                    + values.get(param);
                        }
                    }
                    sheet.setCellFormula(r, c, script);
                }
            }
        }
    }

    private Formula getFormula(int formulaId) {

        Formula result = null;
        if (this.formulas == null) {
            try {
                InitialContext ctx = fina2.Main.getJndiContext();
                Object ref = ctx
                        .lookup("fina2/reportoo/repository/RepositorySession");
                RepositorySessionHome repoSessionHome = (RepositorySessionHome) PortableRemoteObject
                        .narrow(ref, RepositorySessionHome.class);

                RepositorySession repositorySession = repoSessionHome.create();

                this.formulas = repositorySession.getFormulas();
            } catch (Exception e) {
                Main.generalErrorHandler(e);
            }
        }

        Iterator iter = this.formulas.iterator();
        while (iter.hasNext()) {
            Formula formula = (Formula) iter.next();
            if (formula.getId() == formulaId) {
                result = formula;
            }
        }

        return result;
    }

    private int getFormulaId(String formula) {

        String formulaId = formula.substring(formula.indexOf('(') + 1,
                formula.indexOf(';')).trim();

        return Integer.valueOf(formulaId).intValue();
    }

    private Hashtable parseFormulaParams(String s) {
        Hashtable params = new Hashtable();
        String sv = s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')).trim();

        java.util.StringTokenizer st = new java.util.StringTokenizer(sv, "=;");
        while (true) {
            if (!st.hasMoreTokens())
                break;
            String name = st.nextToken();

            if (!st.hasMoreTokens())
                break;
            String value = st.nextToken();
            params.put(name, value);
        }
        return params;
    }

    private String isRepositoryParam(String s, java.util.Iterator iter) {

        while (iter.hasNext()) {
            String p = (String) iter.next();
            if (s.endsWith("@" + p))
                return p;
        }
        return null;
    }

    private void logStatistics() {

        if (stageInfos.size() > 0) {

            progressControler.setProgress(STAGES_COUNT);
            progressControler.setMessage("Done");

            log.info("/------------------------------/");
            log.info("/ Report generation statistics /");
            log.info("/------------------------------/");
            StageInfo firstStage = (StageInfo) stageInfos.get(0);
            long currentTime = System.currentTimeMillis();
            long totalDuration = currentTime - firstStage.stageStartTime;

            for (int i = 0; i < stageInfos.size(); i++) {

                StageInfo stage = (StageInfo) stageInfos.get(i);
                long stageEndTime;
                if (i < stageInfos.size() - 1) {
                    stageEndTime = ((StageInfo) stageInfos.get(i + 1)).stageStartTime;
                } else {
                    stageEndTime = currentTime;
                }

                long duration = stageEndTime - stage.stageStartTime;

                StringBuffer buff = new StringBuffer();
                buff.append("Stage: \"");
                buff.append(stage.stageName);
                buff.append("\", duration: ");
                buff.append(duration);
                buff.append("ms (");
                buff.append((int) ((double) duration / totalDuration * 100));
                buff.append("%)");
                log.info(buff.toString());
            }

            StringBuffer buff = new StringBuffer();
            buff.append("Total report generation time: ");
            buff.append(totalDuration);
            buff.append("ms, stage total count: ");
            buff.append(stageInfos.size());
            buff.append(", stage average duration: ");
            buff.append(totalDuration / stageInfos.size());
            buff.append("ms");

            log.info(buff.toString());
        }
    }
}

class StageInfo {

    public String stageName;

    public long stageStartTime;
}
