package fina2.reportoo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import fina2.Main;
import fina2.i18n.LanguagePK;
import fina2.reportoo.server.OOReportSession;
import fina2.reportoo.server.OOReportSessionHome;
import fina2.reportoo.server.ReportPK;
import fina2.security.UserPK;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

public class Generator extends javax.swing.JFrame {

	private static Logger log = Logger.getLogger(Generator.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private fina2.Main main = fina2.Main.main;

	private ReportPK reportPK;

	private ReportGeneratingProgressDialog progressDlg;

	private OOReportSession reportSession;

	private Hashtable paramValues = new Hashtable();
	// private LinkedHashMap paramValues = new LinkedHashMap();

	private boolean schedule;

	private Date scheduleTime;

	private boolean onDemand;

	private Spreadsheet totalSheet = SpreadsheetsManager.getInstance().createSpreadsheet();

	private LinkedHashMap reports = new LinkedHashMap();

	private File totalReportsFile = null;

	private boolean regenerated;

	private boolean ignoreSizePosChange;

	/** Creates new form Generator */
	public Generator() {
		try {
			ignoreSizePosChange = true;
			initComponents();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void show(fina2.ui.tree.Node node, boolean schedule, Date scheduleTime, boolean onDemand) {

		this.scheduleTime = scheduleTime;
		this.onDemand = onDemand;

		show(node, schedule);
	}

	public void show(fina2.ui.tree.Node node, boolean schedule) {

		this.schedule = schedule;
		this.reportPK = (ReportPK) node.getPrimaryKey();

		Thread t = new Thread(new Runnable() {
			public void run() {
				load(reportPK, false);
			}
		});
		t.start();
	}

	@SuppressWarnings("rawtypes")
	private boolean load(ReportPK pk, boolean regenerate) {

		try {
			initOOReportSession();

			LinkedHashMap<ReportPK, ReportInfo> infos = reportSession.getInfos(main.getUserHandle(), pk);
			Hashtable names = reportSession.getNames(main.getUserHandle(), main.getLanguageHandle(), pk);

			// Select generated report parameters
			for (java.util.Iterator iter = infos.keySet().iterator(); iter.hasNext();) {
				ReportInfo info = (ReportInfo) infos.get((ReportPK) iter.next());
				if (!selectParams(info)) {
					return false;
				}
			}

			// Generate or schedule reports based on previously selected
			// parameters
			int curNum = 0;

			for (java.util.Iterator iter = infos.keySet().iterator(); iter.hasNext();) {
				ReportPK rpk = (ReportPK) iter.next();
				if (!load(rpk, (ReportInfo) infos.get(rpk), (String) names.get(rpk), regenerate, infos.size(), ++curNum)) {
					return false;
				}
			}

			showGeneratedReports(regenerate);

			return true;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	private boolean load(ReportPK repPK, ReportInfo info, String reportName, boolean regenerate, int totalNum, int curNum) {
		try {
			LanguagePK langPK = (LanguagePK) main.getLanguageHandle().getEJBObject().getPrimaryKey();
			UserPK userPK = (UserPK) main.getUserHandle().getEJBObject().getPrimaryKey();

			if (!schedule) {

				if (progressDlg == null) {
					createProgressDialog();
				}

				progressDlg.setTitleMessage("Generated report name: " + reportName);
				progressDlg.setMessage("Starting report generation");
				progressDlg.setTotalNumber(totalNum);
				progressDlg.setProgress(0);

				ReportGenerator rg = ReportGenerator.getInstance();
				regenerated = rg.generate(langPK, repPK, userPK, info, progressDlg, regenerate);

				String sheetName = reportName + ((regenerate) ? "_REG" : "");
				reports.put(sheetName, rg.getResult());

				progressDlg.setCurrentNumber(curNum);
			} else {
				ReportSchedulerManager.getInstance().scheduleReport(langPK, repPK, userPK, scheduleTime, onDemand, info);

				if (curNum == totalNum) {
					ui.showMessageBox(main.getMainFrame(), ui.getString("fina2.title"), ui.getString("fina2.report.scheduleCreated"));
				}
			}
			return true;

		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);

			if (progressDlg != null) {
				progressDlg.dispose();
			}

			Main.generalErrorHandler(ex);
			return false;
		}
	}

	private void showGeneratedReports(boolean regenerate) throws IOException, Exception {
		if (!schedule) {
			setVisible(true);

			// Create temporaty sheet
			Spreadsheet sheet = SpreadsheetsManager.getInstance().createSpreadsheet();
			jPanel2.add(sheet.getComponent());

			progressDlg.setProgress(0);
			progressDlg.setMaxProgress(reports.size());
			progressDlg.setTitleMessage("Opening generated reports");

			for (Iterator iter = reports.keySet().iterator(); iter.hasNext();) {
				String sheetName = (String) iter.next();
				progressDlg.setMessage("Opened report: " + sheetName);
				if (totalReportsFile == null) {
					initTotalSheet(sheetName, (byte[]) reports.get(sheetName));
				} else {
					sheet.read((byte[]) reports.get(sheetName));
					totalSheet.copySheetTabs(sheet, sheetName);
				}
				progressDlg.incProgress();
			}

			if (regenerate && reports.keySet().iterator().hasNext()) {
				totalSheet.selectSheetByName((String) reports.keySet().iterator().next());
				totalSheet.unselect();
			}

			reports.clear();

			// Show regenerate button if necessary
			regenerateButton.setVisible(!regenerated);

			// Destroy temporay sheet
			jPanel2.remove(sheet.getComponent());
			sheet.dispose();

			progressDlg.dispose();
			progressDlg = null;

			ignoreSizePosChange = false;

			loadConf();
		}
	}

	private void initTotalSheet(String sheetName, byte[] reportData) throws IOException, Exception {

		totalReportsFile = File.createTempFile("FinaTotalReport", "tmp");

		FileOutputStream fos = new FileOutputStream(totalReportsFile);
		fos.write(reportData);
		fos.close();

		totalSheet.readFromURL("file:///" + totalReportsFile.getAbsolutePath().replace('\\', '/'));
		totalSheet.renameSheet(sheetName);
		totalSheet.showSheetTabs(true);
	}

	private void createProgressDialog() {

		progressDlg = new ReportGeneratingProgressDialog(Generator.this, "Generating Report(s)", true);
		progressDlg.setSize(360, 160);

		Thread thread = new Thread() {
			public void run() {
				progressDlg.setVisible(true);
			}
		};
		thread.start();
	}

	private void initOOReportSession() {
		try {
			InitialContext ctx = fina2.Main.getJndiContext();

			Object ref = ctx.lookup("fina2/reportoo/server/OOReportSession");
			OOReportSessionHome sessionHome = (OOReportSessionHome) PortableRemoteObject.narrow(ref, OOReportSessionHome.class);
			reportSession = sessionHome.create();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private boolean selectParams(ReportInfo info) {

		for (java.util.Iterator iter = info.parameters.values().iterator(); iter.hasNext();) {
			fina2.ui.sheet.Parameter it = (fina2.ui.sheet.Parameter) iter.next();
			if (it.getValues().size() == 0) {
				if (paramValues.get(new Integer(it.getType())) != null) {
					it.setValues((java.util.Vector) paramValues.get(new Integer(it.getType())));
				} else {
					SelectParameterValuesDialog dlg = new SelectParameterValuesDialog(main.getMainFrame(), true);
					dlg.show(it.getType(), "  " + it.getName() + "  ");
					if (dlg.Ok()) {
						paramValues.put(new Integer(it.getType()), dlg.getValues());
						it.setValues(dlg.getValues());
					} else {
						return false;
					}
				}
			}
		}

		for (java.util.Iterator iter = info.iterators.values().iterator(); iter.hasNext();) {
			fina2.ui.sheet.Iterator it = (fina2.ui.sheet.Iterator) iter.next();
			if (it.getType() == it.VCT_ITERATOR) {
				if ((it.getAggregateValues().size() == 0) && (it.getAggergateParameter() == null)) {
					SelectParameterValuesDialog dlg = new SelectParameterValuesDialog(main.getMainFrame(), true);
					if (it.getAggregateType() == 1) { // BANK
						dlg.show(it.BANK_ITERATOR, "  " + it.getName() + "  ");
					} else {
						dlg.show(it.PEER_ITERATOR, "  " + it.getName() + "  ");
					}
					if (dlg.Ok()) {
						it.setAggregateValues(dlg.getValues());
					} else {
						return false;
					}
				}
				if ((it.getPeriodValues().size() == 0) && (it.getPeriodParameter() == null)) {
					SelectParameterValuesDialog dlg = new SelectParameterValuesDialog(main.getMainFrame(), true);
					dlg.show(it.PERIOD_ITERATOR, "  " + it.getName() + "  ");
					if (dlg.Ok()) {
						it.setPeriodValues(dlg.getValues());
					} else {
						return false;
					}
				}
				if (it.getAggergateParameter() != null) {
					fina2.ui.sheet.Parameter p = (fina2.ui.sheet.Parameter) info.parameters.get(it.getAggergateParameter());
					if (p != null) {
						it.setAggregateValues(p.getValues());
					}
				}
				if (it.getPeriodParameter() != null) {
					fina2.ui.sheet.Parameter p = (fina2.ui.sheet.Parameter) info.parameters.get(it.getPeriodParameter());
					if (p != null) {
						it.setPeriodValues(p.getValues());
					}
				}
			} else {
				if ((it.getValues().size() == 0) && (it.getParameter() == null)) {
					if (paramValues.get(new Integer(it.getType())) != null) {
						it.setValues((java.util.Vector) paramValues.get(new Integer(it.getType())));
					} else {
						SelectParameterValuesDialog dlg = new SelectParameterValuesDialog(main.getMainFrame(), true);
						dlg.show(it.getType(), "  " + it.getName() + "  ");
						if (dlg.Ok()) {
							paramValues.put(new Integer(it.getType()), dlg.getValues());
							it.setValues(dlg.getValues());
						} else {
							return false;
						}
					}
				}
				if (it.getParameter() != null) {
					fina2.ui.sheet.Parameter p = (fina2.ui.sheet.Parameter) info.parameters.get(it.getParameter());
					if (p != null) {
						it.setValues(p.getValues());
					}
				}
			}
		}
		return true;
	}

	private void loadConf() {

		int x = 0, y = 0, w = 100, h = 200;
		boolean v = false;

		try {
			x = ((Integer) ui.getConfigValue("fina2.report.Generator.x")).intValue();
			y = ((Integer) ui.getConfigValue("fina2.report.Generator.y")).intValue();
			w = ((Integer) ui.getConfigValue("fina2.report.Generator.width")).intValue();
			h = ((Integer) ui.getConfigValue("fina2.report.Generator.height")).intValue();
			v = ((Boolean) ui.getConfigValue("fina2.report.Generator.visible")).booleanValue();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		setLocation(x, y);
		setSize(w, h);

		if (v) {
			main.addToShow(this);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents

		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		regenerateButton = new javax.swing.JButton();

		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				formComponentResized(evt);
			}

			public void componentMoved(java.awt.event.ComponentEvent evt) {
				formComponentMoved(evt);
			}

			public void componentShown(java.awt.event.ComponentEvent evt) {
				formComponentShown(evt);
			}

			public void componentHidden(java.awt.event.ComponentEvent evt) {
				formComponentHidden(evt);
			}
		});

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel3.setLayout(new java.awt.BorderLayout());

		getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

		jPanel3.add(totalSheet.getComponent(), java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.BorderLayout());

		regenerateButton.setFont(ui.getFont());
		regenerateButton.setVisible(false);
		regenerateButton.setText(ui.getString("fina2.report.regenerate"));
		regenerateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				regenerateButtonActionPerformed(evt);
			}
		});

		jPanel2.add(regenerateButton);

		jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		pack();
	} // GEN-END:initComponents

	private void regenerateButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_regenerateButtonActionPerformed
		Thread t = new Thread(new Runnable() {
			public void run() {
				load(reportPK, true);
			}
		});
		t.start();
	} // GEN-LAST:event_regenerateButtonActionPerformed

	private void formComponentHidden(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentHidden
		if (ignoreSizePosChange == false) {
			ui.putConfigValue("fina2.report.Generator.visible", new Boolean(false));
		}
	} // GEN-LAST:event_formComponentHidden

	private void formComponentShown(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentShown
		ui.putConfigValue("fina2.report.Generator.visible", new Boolean(true));
	} // GEN-LAST:event_formComponentShown

	private void formComponentMoved(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentMoved
		if (ignoreSizePosChange == false) {
			ui.putConfigValue("fina2.report.Generator.x", new Integer(getX()));
			ui.putConfigValue("fina2.report.Generator.y", new Integer(getY()));
		}
	} // GEN-LAST:event_formComponentMoved

	private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentResized
		if (ignoreSizePosChange == false) {
			ui.putConfigValue("fina2.report.Generator.width", new Integer(getWidth()));
			ui.putConfigValue("fina2.report.Generator.height", new Integer(getHeight()));
		}
	} // GEN-LAST:event_formComponentResized

	private void exitForm(java.awt.event.WindowEvent evt) { // GEN-FIRST:event_exitForm
		totalSheet.dispose();
		dispose();

		totalReportsFile.delete();
	} // GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton regenerateButton;
	private javax.swing.JTabbedPane tab;
	// End of variables declaration//GEN-END:variables
}
