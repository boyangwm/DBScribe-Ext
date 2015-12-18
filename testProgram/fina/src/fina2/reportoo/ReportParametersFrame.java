package fina2.reportoo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import fina2.Main;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.i18n.LocaleUtil;
import fina2.metadata.MDTSession;
import fina2.metadata.MDTSessionHome;
import fina2.period.PeriodSession;
import fina2.period.PeriodSessionHome;
import fina2.ui.sheet.Iterator;
import fina2.ui.sheet.Parameter;
import fina2.ui.table.TableRow;

public class ReportParametersFrame extends JFrame {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    JTabbedPane jTabbedPane = new JTabbedPane();
    JPanel jIteratorPanel = new JPanel();
    JPanel jParametersPanel = new JPanel();
    GridLayout gridLayout = new GridLayout();
    ReportInfo reportInfo;
    DefaultTableModel paramsTableModel = new DefaultTableModel();
    DefaultTableModel paramsValuesTableModel = new DefaultTableModel();
    JScrollPane jParamTablePanel = new JScrollPane();
    JScrollPane jParamValueTablePanel = new JScrollPane();
    JTable jParamTable = new JTable();
    JTable jParamValueTable = new JTable();
    JScrollPane jIterTablePanel = new JScrollPane();
    JScrollPane jIterValueTablePanel = new JScrollPane();
    JTable jIterTable = new JTable();
    JTable jIterValueTable = new JTable();
    DefaultTableModel iterTableModel = new DefaultTableModel();
    DefaultTableModel iterValuesTableModel = new DefaultTableModel();

    private HashMap banksMap = new HashMap();
    private HashMap<String,String> nodes=new HashMap<String,String>();
    private HashMap bankGroupsMap = new HashMap();
    private HashMap periodsMap = new HashMap();
    private String datePattern;

    public ReportParametersFrame(ReportInfo reportInfo) {
    	
        this.reportInfo = reportInfo;
        try {
            ui.loadIcon("fina2.parameters", "parameters.gif");

            jbInit();
            pack();

            paramsTableModel.addColumn(ui
                    .getString("fina2.report.parameter.name"));
            paramsTableModel.addColumn(ui
                    .getString("fina2.report.parameter.type"));

            paramsValuesTableModel.addColumn(ui
                    .getString("fina2.report.parameter.values"));

            iterTableModel
                    .addColumn(ui.getString("fina2.report.iterator.name"));
            iterTableModel
                    .addColumn(ui.getString("fina2.report.iterator.type"));
            iterTableModel.addColumn(ui
                    .getString("fina2.report.iterator.orientation"));
            iterTableModel.addColumn(ui
                    .getString("fina2.report.iterator.parameter"));

            iterValuesTableModel.addColumn(ui
                    .getString("fina2.report.iterator.values"));

            datePattern = LocaleUtil.getDatePattern(Main.main
                    .getLanguageHandle());

            ListSelectionModel rowSM2 = jIterTable.getSelectionModel();
            rowSM2.addListSelectionListener(new ListSelectionListener() {
                @SuppressWarnings("unchecked")
				public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting())
                        return;

                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                    if (!lsm.isSelectionEmpty()) {
                        int selectedRow = lsm.getMinSelectionIndex();

                        ReportInfo ri = ReportParametersFrame.this.reportInfo;
                      
                        Iterator iterator = (Iterator) ri.iterators.get(jIterTable.getValueAt(selectedRow, 0));

                        int rowCount = iterValuesTableModel.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                            iterValuesTableModel.removeRow(0);
                        }

                        iterValuesTableModel.setColumnCount(0);
                        String[] iterColumns = getIterColumns(iterator.getType());

                        for (int i = 0; i < iterColumns.length; i++) {
                            iterValuesTableModel.addColumn(iterColumns[i]);
                        }
                        
                        for (java.util.Iterator iter = iterator.getValues().iterator(); iter.hasNext();) {
                             	
                            iterValuesTableModel.addRow(getIterValues(iter.next(), iterator.getType()));
                        }
                        
                      
                    }
                    
                }
            });

            ListSelectionModel rowSM = jParamTable.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                @SuppressWarnings("unchecked")
				public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting())
                        return;

                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                    if (!lsm.isSelectionEmpty()) {
                        int selectedRow = lsm.getMinSelectionIndex();

                        ReportInfo ri = ReportParametersFrame.this.reportInfo;
                        Parameter param = (Parameter) ri.parameters
                                .get(jParamTable.getValueAt(selectedRow, 0));

                        int rowCount = paramsValuesTableModel.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                            paramsValuesTableModel.removeRow(0);
                        }

                        paramsValuesTableModel.setColumnCount(0);
                        String[] paramColumns = getParamColumns(param.getType());

                        for (int i = 0; i < paramColumns.length; i++) {
                            paramsValuesTableModel.addColumn(paramColumns[i]);
                        }

                        for (java.util.Iterator iter = param.getValues().iterator(); iter.hasNext();) {
                            paramsValuesTableModel.addRow(getParamValues(iter.next(), param.getType()));
                        }
                    }
                }
            });
        } catch (Exception ex) {
            Main.generalErrorHandler(ex);
        }
    }

    private String[] getIterColumns(int type) {

        String[] columns = null;
        switch (type) {
        case Iterator.BANK_ITERATOR:
        case Iterator.PEER_ITERATOR:
            columns = new String[] { ui.getString("fina2.code"),
                    ui.getString("fina2.description") };
            break;
        case Iterator.NODE_ITERATOR:
            columns = new String[] { ui.getString("fina2.code"),ui.getString("fina2.description") };
            break;
        case Iterator.PERIOD_ITERATOR:
            columns = new String[] { ui.getString("fina2.period.type"),
                    ui.getString("fina2.period.number"),
                    ui.getString("fina2.period.start"),
                    ui.getString("fina2.period.end") };
            break;
        }

        return (columns != null) ? columns : new String[] { ui
                .getString("fina2.report.iterator.values") };
    }

    private Object[] getIterValues(Object obj, int type) {

        Object[] values = null;
        switch (type) {
        case Iterator.BANK_ITERATOR:
            values = new Object[] { obj, banksMap.get(obj) };
            break;
        case Iterator.PEER_ITERATOR:
            values = new Object[] { obj, bankGroupsMap.get(obj) };
            break;
        case Iterator.NODE_ITERATOR: 	
            values = new Object[] {  obj ,nodes.get(obj.toString().trim()) };
            break;
        
        case Iterator.PERIOD_ITERATOR:
            TableRow periodInfo = (TableRow) periodsMap.get(obj);
            if (periodInfo != null) {
                values = new Object[] { periodInfo.getValue(0),
                        periodInfo.getValue(1), periodInfo.getValue(2),
                        periodInfo.getValue(3) };
            }
            break;
        default:
            values = new Object[] { obj };
        }

        return values;
    }

    private String[] getParamColumns(int type) {

        String[] columns = null;
        switch (type) {
        case Parameter.BANK_ITERATOR:
        case Parameter.PEER_ITERATOR:
            columns = new String[] { ui.getString("fina2.code"),
                    ui.getString("fina2.description") };
            break;
        case Parameter.NODE_ITERATOR:
            columns = new String[] { ui.getString("fina2.code"),ui.getString("fina2.description")  };
            break;
        case Parameter.PERIOD_ITERATOR:
            columns = new String[] { ui.getString("fina2.period.type"),
                    ui.getString("fina2.period.number"),
                    ui.getString("fina2.period.start"),
                    ui.getString("fina2.period.end") };

            break;
        }

        return (columns != null) ? columns : new String[] { ui
                .getString("fina2.report.parameter.values") };
    }

    private Object[] getParamValues(Object obj, int type) {

        Object[] values = null;
        switch (type) {
        case Parameter.BANK_ITERATOR:
            values = new Object[] { obj, banksMap.get(obj) };
            break;
        case Parameter.PEER_ITERATOR:
            values = new Object[] { obj, bankGroupsMap.get(obj) };
            break;
        case Parameter.NODE_ITERATOR:
            values = new Object[] { "[" + obj + "]",nodes.get(obj.toString().trim()) };
            break;
        case Parameter.PERIOD_ITERATOR:
            TableRow periodInfo = (TableRow) periodsMap.get(obj);
            if (periodInfo != null) {
                values = new Object[] { periodInfo.getValue(0),
                        periodInfo.getValue(1),
                        adjustDateFormat(periodInfo.getValue(2)),
                        adjustDateFormat(periodInfo.getValue(3)) };
            }
            break;
        default:
            values = new Object[] { obj };
        }

        return values;
    }

    private void jbInit() throws Exception {
        this.setIconImage(ui.getIcon("fina2.parameters").getImage());
        this.setTitle(ui.getString("fina2.report.parametersFrame.title"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        jParametersPanel.setLayout(gridLayout);
        gridLayout.setColumns(2);
        gridLayout.setHgap(4);
        gridLayout.setRows(1);
        gridLayout.setVgap(0);
        jParamTable.setModel(paramsTableModel);
        jParamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jParamTable.setFont(ui.getFont());
        jParamTable.getTableHeader().setFont(ui.getFont());
        jParamValueTable.setModel(paramsValuesTableModel);
        jParamValueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jParamValueTable.setFont(ui.getFont());
        jParamValueTable.getTableHeader().setFont(ui.getFont());
        jParametersPanel.setForeground(Color.black);
        jIteratorPanel.setLayout(gridLayout);
        jIterTable.setModel(iterTableModel);
        jIterValueTable.setModel(iterValuesTableModel);
        jIterValueTable.setFont(ui.getFont());
        jIterValueTable.getTableHeader().setFont(ui.getFont());
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);
        jTabbedPane.add(jIteratorPanel, ui.getString("fina2.report.iterators"));
        jIteratorPanel.add(jIterTablePanel, null);
        jIterTablePanel.getViewport().add(jIterTable, null);
        jIteratorPanel.add(jIterValueTablePanel, null);
        jIterValueTablePanel.getViewport().add(jIterValueTable, null);
        jTabbedPane.add(jParametersPanel, ui
                .getString("fina2.report.parameters"));
        jTabbedPane.setFont(ui.getFont());
        jParametersPanel.add(jParamTablePanel, null);
        jParametersPanel.add(jParamValueTablePanel, null);
        jParamValueTablePanel.getViewport().add(jParamValueTable, null);
        jParamTablePanel.getViewport().add(jParamTable, null);
    }

    public ReportInfo getReportInfo() {
        return reportInfo;
    }

    public void setReportInfo(ReportInfo reportInfo) {
        this.reportInfo = reportInfo;
    }

    /**
     * Shows or hides this component depending on the value of parameter
     * <code>b</code>.
     *
     * @param b if <code>true</code>, shows this component; otherwise, hides
     *   this component
     */
    public void setVisible(boolean show) {

        if (show && reportInfo != null) {

            boolean hasIterator = false;
            Collection iters = reportInfo.iterators.values();

            for (java.util.Iterator iter = iters.iterator(); iter.hasNext();) {
                hasIterator = true;
                Iterator iterator = (Iterator) iter.next();
                iterTableModel.addRow(new Object[] { iterator.getName(),
                        getIteratorType(iterator.getType()),
                        getOrientation(iterator.getOrientation()),
                        iterator.getParameter() });
            }

            boolean hasParameter = false;
            Collection params = reportInfo.parameters.values();

            for (java.util.Iterator iter = params.iterator(); iter.hasNext();) {

                hasParameter = true;
                Parameter param = (Parameter) iter.next();
                paramsTableModel.addRow(new Object[] { param.getName(),
                        getParameterType(param.getType()) });
            }

            load();

            if (hasIterator) {
                jIterTable.changeSelection(0, 0, false, false);
            }

            if (hasParameter) {
                jParamTable.changeSelection(0, 0, false, false);
                if (!hasIterator) {
                    jTabbedPane.setSelectedIndex(1);
                }
            }
        }

        super.setVisible(true);
    }

    private String getOrientation(int orientation) {

        return (orientation == Iterator.COL_ITERATOR) ? ui
                .getString("fina2.report.iterator.orientation.column") : ui
                .getString("fina2.report.iterator.orientation.row");
    }

    private void load() {

        loadBanksAndBankGroups();
        loadNodes();
        loadPeriods();

    }

    private void loadPeriods() {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/period/PeriodSession");
            PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject
                    .narrow(ref, PeriodSessionHome.class);

            PeriodSession periodSession = home.create();
            Collection periodInfos = periodSession.getPeriodRows(Main.main
                    .getUserHandle(), Main.main.getLanguageHandle());

            for (java.util.Iterator iter = periodInfos.iterator(); iter
                    .hasNext();) {
                TableRow periodInfo = (TableRow) iter.next();
                periodsMap.put(periodInfo.getPrimaryKey(), periodInfo);
            }
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    private void loadBanksAndBankGroups() {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/bank/BankSession");
            BankSessionHome home = (BankSessionHome) PortableRemoteObject
                    .narrow(ref, BankSessionHome.class);

            BankSession bankSession = home.create();

            Vector banks = (Vector) bankSession.getBanksRows(Main.main
                    .getUserHandle(), Main.main.getLanguageHandle());

            for (java.util.Iterator iter = banks.iterator(); iter.hasNext();) {
                TableRow bankInfo = (TableRow) iter.next();
                banksMap.put(bankInfo.getValue(0), bankInfo.getValue(1));
            }
            

            Vector bankGroups = (Vector) bankSession.getBankGroupsRows(
                    Main.main.getUserHandle(), Main.main.getLanguageHandle());

            for (java.util.Iterator iter = bankGroups.iterator(); iter
                    .hasNext();) {
                TableRow bankGroupInfo = (TableRow) iter.next();
                bankGroupsMap.put(bankGroupInfo.getValue(0), bankGroupInfo
                        .getValue(1));
            }
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

	private void loadNodes() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);
			MDTSession mdtSession = home.create();
			nodes=mdtSession.getNodeCodeDescriptions();
			
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			ex.printStackTrace();
		}
	}
    
    private String adjustDateFormat(String oldFormatDate) {

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Date date = format.parse(oldFormatDate);

            format = new SimpleDateFormat(datePattern);
            return format.format(date);

        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String getIteratorType(int type) {

        String result = "";
        switch (type) {
        case Iterator.BANK_ITERATOR:
            result = ui.getString("fina2.report.iterator.fi");
            break;
        case Iterator.PEER_ITERATOR:
            result = ui.getString("fina2.report.iterator.peer");
            break;
        case Iterator.NODE_ITERATOR:
            result = ui.getString("fina2.report.iterator.node");
            break;
        case Iterator.PERIOD_ITERATOR:
            result = ui.getString("fina2.report.iterator.period");
            break;
        case Iterator.OFFSET_ITERATOR:
            result = ui.getString("fina2.report.iterator.offset");
            break;
        case Iterator.VCT_ITERATOR:
            result = ui.getString("fina2.report.iterator.vct");
            break;
        }
        return result;
    }

    private String getParameterType(int type) {

        String result = "";
        switch (type) {
        case Parameter.BANK_ITERATOR:
            result = ui.getString("fina2.report.iterator.fi");
            break;
        case Parameter.PEER_ITERATOR:
            result = ui.getString("fina2.report.iterator.peer");
            break;
        case Parameter.NODE_ITERATOR:
            result = ui.getString("fina2.report.iterator.node");
            break;
        case Parameter.PERIOD_ITERATOR:
            result = ui.getString("fina2.report.iterator.period");
            break;
        }
        return result;
    }
}
