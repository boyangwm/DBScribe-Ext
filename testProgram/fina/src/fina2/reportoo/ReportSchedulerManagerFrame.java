package fina2.reportoo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.Handle;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import fina2.BaseFrame;
import fina2.Main;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.reportoo.server.ScheduledReportInfo;
import fina2.security.UserPK;
import fina2.ui.UIManager;
import fina2.ui.treetable.JTreeTable;

public class ReportSchedulerManagerFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	BorderLayout borderLayout = new BorderLayout();
	JPanel jButtonsPanel = new JPanel();
	JScrollPane scrollPane = new JScrollPane();
	JButton jParamsButton = new JButton();
	JButton jDeleteButton = new JButton();
	ScheduledReportsModel treeTableModel = new ScheduledReportsModel(null);
	JTreeTable jTreeTable = new JTreeTable(treeTableModel);
	JButton jRefreshButton = new JButton();
	JButton jRunButton = new JButton();
	JButton printButton = new JButton();
	ReportSchedulerManager schedulerManager;
	private ScheduledReportInfo root;
	private ArrayList paths = new ArrayList();
	private boolean permissionDenied = false;

	public ReportSchedulerManagerFrame() {
		try {
			ui.loadIcon("fina2.help", "help.gif");
			ui.loadIcon("fina2.print", "print.gif");
			ui.loadIcon("fina2.refresh", "refresh.gif");
			ui.loadIcon("fina2.close", "cancel.gif");
			ui.loadIcon("fina2.parameters", "parameters.gif");
			ui.loadIcon("fina2.run", "run.gif");

			jbInit();
			BaseFrame.ensureVisible(this);
			LanguagePK langPK = (LanguagePK) fina2.Main.main
					.getLanguageHandle().getEJBObject().getPrimaryKey();
			UserPK userPK = (UserPK) fina2.Main.main.getUserHandle()
					.getEJBObject().getPrimaryKey();

			ProgressControler pc = new ProgressControler() {
				public void incProgress() {
				}

				public void setMaxProgress(int maxProgress) {
				}

				public void setMessage(String message) {
					if (Main.main != null && Main.main.getMainFrame() != null) {
						Main.main.getMainFrame().setStatusText(message);
					}
				}

				public void setProgress(int progress) {
				}
			};

			schedulerManager = ReportSchedulerManager.getInstance(langPK,
					userPK, pc);

			DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();

			ui.loadIcon("fina2.folder", "node.gif");
			ui.loadIcon("fina2.report", "amend.gif");

			tcr.setOpenIcon(Main.main.ui.getIcon("fina2.folder"));
			tcr.setClosedIcon(Main.main.ui.getIcon("fina2.folder"));
			tcr.setLeafIcon(Main.main.ui.getIcon("fina2.report"));

			jTreeTable.getTree().setCellRenderer(tcr);
			jTreeTable.getTree().addTreeSelectionListener(
					new TreeSelectionListener() {
						public void valueChanged(TreeSelectionEvent evt) {

							TreePath[] treePaths = jTreeTable.getTree()
									.getSelectionPaths();
							if (treePaths != null) {
								jRunButton.setEnabled(treePaths.length > 0);
								jDeleteButton.setEnabled(treePaths.length > 0);

								if (treePaths.length == 1) {

									ScheduledReportInfo scheduledReport = (ScheduledReportInfo) treePaths[0]
											.getLastPathComponent();
									jParamsButton.setEnabled(!scheduledReport
											.isFolder());
								} else {
									jParamsButton.setEnabled(false);
								}
							} else {
								jRunButton.setEnabled(false);
								jDeleteButton.setEnabled(false);
								jParamsButton.setEnabled(false);
							}
						}
					});

			setTitle(fina2.Main.main.ui
					.getString("fina2.report.reportSchedulerManager"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	}

	private void jbInit() throws Exception {

		this.setClosable(true);
		this.setIconifiable(true);
		this.setMaximizable(true);
		this.setResizable(true);
        initBaseComponents();
		jTreeTable.setBorder(null);
		jTreeTable.setFont(ui.getFont());
		jTreeTable.getTableHeader().setFont(ui.getFont());
		jTreeTable.getTree().setFont(ui.getFont());

		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.getViewport().add(jTreeTable, null);

		getContentPane().setLayout(borderLayout);

		jButtonsPanel.setLayout(new BorderLayout());
		jButtonsPanel.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 5)));

		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridBagLayout());

		// Delete button
		jDeleteButton.setFont(ui.getFont());
		jDeleteButton.setText(ui.getString("fina2.delete"));
		jDeleteButton.setIcon(ui.getIcon("fina2.delete"));
		jDeleteButton.setHorizontalAlignment(SwingConstants.LEFT);
		jDeleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jDeleteButton_actionPerformed(evt);
			}
		});
		gridPanel.add(jDeleteButton, UIManager.getGridBagConstraints(0, 0, -1,
				-1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		// Parameters button
		jParamsButton.setFont(ui.getFont());
		jParamsButton.setText(ui.getString("fina2.reportoo.parameters"));
		jParamsButton.setIcon(ui.getIcon("fina2.parameters"));
		jParamsButton.setHorizontalAlignment(SwingConstants.LEFT);
		jParamsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jParamsButton_actionPerformed(evt);
			}
		});
		gridPanel.add(jParamsButton, UIManager.getGridBagConstraints(0, 1, -1,
				-1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		// Run button
		jRunButton.setEnabled(false);
		jRunButton.setFont(ui.getFont());
		jRunButton.setText(ui
				.getString("fina2.report.schedulerManagerFrame.run"));
		jRunButton.setIcon(ui.getIcon("fina2.run"));
		jRunButton.setHorizontalAlignment(SwingConstants.LEFT);
		jRunButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRun_actionPerformed(evt);
			}
		});
		gridPanel.add(jRunButton, UIManager.getGridBagConstraints(0, 2, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		jButtonsPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

		this.getContentPane().add(jButtonsPanel, java.awt.BorderLayout.EAST);
		this.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());

		JPanel helpButtonPanel = new JPanel();
		JPanel closeRefreshPanel = new JPanel();
		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Schedules_Manager");
		} else {
			helpButton.setEnabled(false);
		}
		helpButtonPanel.add(helpButton);

		southPanel.add(helpButtonPanel, java.awt.BorderLayout.WEST);

		/*
		 * printButton.setIcon(ui.getIcon("fina2.print"));
		 * printButton.setFont(ui.getFont());
		 * printButton.setText(ui.getString("fina2.print"));
		 * printButton.addActionListener(new java.awt.event.ActionListener() {
		 * public void actionPerformed(java.awt.event.ActionEvent evt) {
		 * printButtonActionPerformed(evt); } });
		 * closeRefreshPanel.add(printButton);
		 */


		closeRefreshPanel.add(refreshButton);

		closeRefreshPanel.add(closeButton);

		southPanel.add(closeRefreshPanel, java.awt.BorderLayout.EAST);

		getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);
	}

	/**
	 * Shows or hides this component depending on the value of parameter
	 * <code>b</code>.
	 * 
	 * @param show
	 *            if <code>true</code>, shows this component; otherwise, hides
	 *            this component
	 */
	public void setVisible(boolean show) {
		try {

			if (permissionDenied) {

				super.setVisible(false);

				Main.main.ui.showMessageBox(Main.main.getMainFrame(),
						Main.main.ui
								.getString("fina2.security.permissionDenied"));
				Main.main.ui.putConfigValue(
						"fina2.reportoo.ReportSchedulerManagerFrame.visible",
						new Boolean(false));

				return;
			} else if (show) {
				updateTreeModel();
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}

		super.setVisible(show);
	}

	private void updateTreeModel() {
		try {
			storeTreeState();

			root = schedulerManager.getScheduledReports();
			if (root == null)
				return;
			treeTableModel.setRoot(root);

			jTreeTable.getTree().setRootVisible(root.getChildren().size() != 0);

			Handle lang = Main.main.getLanguageHandle();
			treeTableModel.setPattern(LocaleUtil.getDateAndTimePattern(lang));
			jTreeTable.updateUI();
			loadTreeState();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void jParamsButton_actionPerformed(ActionEvent e) {

		try {
			TreePath treePath = jTreeTable.getTree().getSelectionPath();
			if (treePath != null) {
				ScheduledReportInfo scheduledReport = (ScheduledReportInfo) treePath
						.getLastPathComponent();

				if (!scheduledReport.isFolder()) {

					ReportInfo reportInfo = schedulerManager
							.getScheduledReportInfo(scheduledReport);

					ReportParametersFrame paramsFrame = new ReportParametersFrame(
							reportInfo);
					paramsFrame.setLocationRelativeTo(this);
					paramsFrame.setVisible(true);
				}
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	public void jDeleteButton_actionPerformed(ActionEvent e) {
		try {
			ArrayList scheduledReports = getSelectedReports();
			schedulerManager.deleteScheduledReports(scheduledReports);

			updateTreeModel();

		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	protected void refreshButtonActionPerformed(ActionEvent e) {
			updateTreeModel();
	}

	public void jRun_actionPerformed(ActionEvent e) {
		Thread schedulerThread = new Thread(new Runnable() {
			public void run() {
				ArrayList scheduledReports = getSelectedReports();
				try {
					LinkedHashMap linkedMap = new LinkedHashMap();
					for (Iterator iter = scheduledReports.iterator(); iter
							.hasNext();) {
						ScheduledReportInfo sri = (ScheduledReportInfo) iter
								.next();
						if (sri.isFolder()) {
							getFolderReports(sri, linkedMap);
						} else {
							linkedMap.put(sri, sri);
						}
					}

					for (Iterator iter = linkedMap.values().iterator(); iter
							.hasNext();) {
						ScheduledReportInfo sri = (ScheduledReportInfo) iter
								.next();
						ReportSchedulerManager
								.runScheduledReportGeneration(sri);
					}

					updateTreeModel();
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
				}
			}
		});

		schedulerThread.start();
	}

	private ArrayList getSelectedReports() {
		ArrayList scheduledReports = new ArrayList();
		TreePath[] treePaths = jTreeTable.getTree().getSelectionPaths();
		for (int i = 0; treePaths != null && i < treePaths.length; i++) {
			ScheduledReportInfo scheduledReport = (ScheduledReportInfo) treePaths[i]
					.getLastPathComponent();
			scheduledReports.add(scheduledReport);
		}
		return scheduledReports;
	}

	private void getFolderReports(ScheduledReportInfo sri, Map reports) {
		for (Iterator iter = sri.getChildren().iterator(); iter.hasNext();) {
			ScheduledReportInfo child = (ScheduledReportInfo) iter.next();
			if (child.isFolder()) {
				getFolderReports(child, reports);
			} else {
				reports.put(child, child);
			}
		}
	}

	public void storeTreeState() {
		if (root != null) {
			paths.clear();

			TreePath tp = new TreePath(root);
			Enumeration nodes = jTreeTable.getTree().getExpandedDescendants(tp);
			while (nodes != null && nodes.hasMoreElements()) {
				paths.add(nodes.nextElement());
			}
		}
	}

	public void loadTreeState() {
		for (int j = 0; j < paths.size(); j++) {
			TreePath Path = (TreePath) paths.get(j);
			jTreeTable.getTree().expandPath(Path);
		}
	}

	protected void closeButtonActionPerformed(ActionEvent e) {
		dispose();
	}
}

