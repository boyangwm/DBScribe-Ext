package fina2.reportoo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import fina2.BaseFrame;
import fina2.Main;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.reportoo.server.ReportPK;
import fina2.reportoo.server.StoredReportInfo;
import fina2.reportoo.server.StoredReportsSession;
import fina2.reportoo.server.StoredReportsSessionHome;
import fina2.security.UserPK;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.treetable.JTreeTable;

public class StoredReportManagerFrame extends BaseFrame {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	BorderLayout borderLayout = new BorderLayout();
	JPanel jButtonsPanel = new JPanel();
	JScrollPane scrollPane = new JScrollPane();
	JButton jParamsButton = new JButton();
	JButton jDeleteButton = new JButton();
	JButton jReviewButton = new JButton();
	JButton printButton = new JButton();

	TreeTableModel treeTableModel = new TreeTableModel(null);
	JTreeTable jTreeTable = new JTreeTable(treeTableModel);
	private StoredReportInfo root;
	private ArrayList paths = new ArrayList();
	private IndeterminateLoading loading;

	public StoredReportManagerFrame() {
		try {

			ui.loadIcon("fina2.help", "help.gif");
			ui.loadIcon("fina2.print", "print.gif");
			ui.loadIcon("fina2.refresh", "refresh.gif");
			ui.loadIcon("fina2.close", "cancel.gif");
			ui.loadIcon("fina2.parameters", "parameters.gif");

			// PERMISSIONS
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			final boolean candelete = user.hasPermission("fina2.stored.reports.delete");
			final boolean canreview = user.hasPermission("fina2.stored.reports.view");

			loading = ui.createIndeterminateLoading(main.getMainFrame());

			jbInit();
			BaseFrame.ensureVisible(this);

			DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();

			ui.loadIcon("fina2.folder", "node.gif");
			ui.loadIcon("fina2.report", "amend.gif");

			tcr.setOpenIcon(Main.main.ui.getIcon("fina2.folder"));
			tcr.setClosedIcon(Main.main.ui.getIcon("fina2.folder"));
			tcr.setLeafIcon(Main.main.ui.getIcon("fina2.report"));

			jTreeTable.getTree().setCellRenderer(tcr);

			jReviewButton.setEnabled(false);
			jDeleteButton.setEnabled(false);
			jParamsButton.setEnabled(false);

			jTreeTable.getTree().addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent evt) {

					TreePath[] treePaths = jTreeTable.getTree().getSelectionPaths();

					if (treePaths != null) {
						StoredReportInfo rootChildren = (StoredReportInfo) treePaths[0].getLastPathComponent();
						if (rootChildren.getName().toString().trim().length() == 0) {
							jReviewButton.setEnabled(false);
							jDeleteButton.setEnabled(false);
							jParamsButton.setEnabled(false);
							return;
						}
						jDeleteButton.setEnabled(treePaths.length > 0);

						jReviewButton.setEnabled(treePaths.length > 0);

						if (treePaths.length == 1) {

							StoredReportInfo sri = (StoredReportInfo) treePaths[0].getLastPathComponent();

							jParamsButton.setEnabled(!sri.isFolder());

						} else {
							jParamsButton.setEnabled(false);
						}
					} else {
						jReviewButton.setEnabled(false);
						jDeleteButton.setEnabled(false);
						jParamsButton.setEnabled(false);
					}

				}

			});

			setTitle(ui.getString("fina2.report.storedReportManager"));

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

		fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
		final boolean candelete = user.hasPermission("fina2.stored.reports.delete");
		final boolean canreview = user.hasPermission("fina2.stored.reports.view");

		jTreeTable.setBorder(null);
		jTreeTable.setFont(ui.getFont());
		jTreeTable.getTableHeader().setFont(ui.getFont());
		jTreeTable.getTree().setFont(ui.getFont());

		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.getViewport().add(jTreeTable, null);

		getContentPane().setLayout(borderLayout);

		jButtonsPanel.setLayout(new BorderLayout());
		jButtonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));

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
		if (candelete)
			gridPanel.add(jDeleteButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Review button
		jReviewButton.setFont(ui.getFont());
		jReviewButton.setText(ui.getString("fina2.review"));
		jReviewButton.setIcon(ui.getIcon("fina2.review"));
		jReviewButton.setHorizontalAlignment(SwingConstants.LEFT);
		jReviewButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jReviewButton_actionPerformed(evt);
			}
		});
		if (canreview)
			gridPanel.add(jReviewButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

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
		if (canreview)
			gridPanel.add(jParamsButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jButtonsPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

		this.getContentPane().add(jButtonsPanel, java.awt.BorderLayout.EAST);
		this.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());

		JPanel helpButtonPanel = new JPanel();
		JPanel closeRefreshPanel = new JPanel();

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Stored_Report_Manager");
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
	 * @param b
	 *            if <code>true</code>, shows this component; otherwise, hides
	 *            this component
	 */
	public void setVisible(boolean show) {
		if (show) {
			updateTreeModel();
		}
		super.setVisible(show);

	}

	private void updateTreeModel() {
		try {
			storeTreeState();

			StoredReportsSession session = getStoredReportSession();

			LanguagePK langPK = (LanguagePK) fina2.Main.main.getLanguageHandle().getEJBObject().getPrimaryKey();
			UserPK userPK = (UserPK) fina2.Main.main.getUserHandle().getEJBObject().getPrimaryKey();
			root = session.getStoredReports(langPK, userPK);
			if (root != null)
				ui.sortByDate(root);
			treeTableModel.setRoot(root);

			jTreeTable.getTree().setRootVisible(root.getChildren().size() != 0);

			Handle lang = Main.main.getLanguageHandle();
			treeTableModel.setPattern(LocaleUtil.getDateAndTimePattern(lang));

			jTreeTable.updateUI();

			loadTreeState();
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			Main.main.ui.putConfigValue("fina2.reportoo.StoredReportManagerFrame.visible", new Boolean(false));
		}
	}

	private StoredReportsSession getStoredReportSession() throws Exception {

		InitialContext jndi = fina2.Main.getJndiContext();
		Object ref = jndi.lookup("fina2/reportoo/server/StoredReportsSession");
		StoredReportsSessionHome home = (StoredReportsSessionHome) PortableRemoteObject.narrow(ref, StoredReportsSessionHome.class);

		StoredReportsSession session = home.create();

		return session;
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
			TreePath path = (TreePath) paths.get(j);
			jTreeTable.getTree().expandPath(path);
		}
	}

	public void jParamsButton_actionPerformed(ActionEvent e) {
		try {
			TreePath treePath = jTreeTable.getTree().getSelectionPath();

			if (treePath != null) {
				StoredReportInfo storedReport = (StoredReportInfo) treePath.getLastPathComponent();

				if (!storedReport.isFolder()) {
					StoredReportsSession session = getStoredReportSession();

					ReportInfo reportInfo = session.getStoredReportInfo(new LanguagePK(storedReport.getLangId()), new ReportPK(storedReport.getReportId()), storedReport.getReportInfoHashCode());

					ReportParametersFrame paramsFrame = new ReportParametersFrame(reportInfo);
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
			StoredReportsSession session = getStoredReportSession();

			ArrayList storedReports = getSelectedReports();
			session.deleteStoredReports(storedReports);

			updateTreeModel();

			jReviewButton.setEnabled(false);
			jDeleteButton.setEnabled(false);
			jParamsButton.setEnabled(false);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	public void jReviewButton_actionPerformed(ActionEvent e) {

		Thread reviewThread = new Thread(new Runnable() {

			public void run() {
				ArrayList storedReports = getSelectedReports();
				LinkedHashMap linkedMap = new LinkedHashMap();

				for (Iterator iter = storedReports.iterator(); iter.hasNext();) {
					StoredReportInfo sri = (StoredReportInfo) iter.next();

					if (sri.isFolder()) {
						getFolderReports(sri, linkedMap);
					} else {
						linkedMap.put(sri, sri);
					}
				}
				loading.start();
				StoredReportsReviewFrame reportsReview = new StoredReportsReviewFrame();
				reportsReview.show(linkedMap.values());
				loading.stop();
				reportsReview.setVisible(true);
			}
		});

		reviewThread.start();
	}

	protected void refreshButtonActionPerformed(ActionEvent e) {
		updateTreeModel();
	}

	protected void closeButtonActionPerformed(ActionEvent e) {
		dispose();
	}

	private void getFolderReports(StoredReportInfo sri, Map reports) {

		for (Iterator iter = sri.getChildren().iterator(); iter.hasNext();) {

			StoredReportInfo child = (StoredReportInfo) iter.next();
			if (child.isFolder()) {
				getFolderReports(child, reports);
			} else {
				reports.put(child, child);
			}
		}
	}

	private ArrayList getSelectedReports() {

		ArrayList storedReports = new ArrayList();

		TreePath[] treePaths = jTreeTable.getTree().getSelectionPaths();
		for (int i = 0; treePaths != null && i < treePaths.length; i++) {
			StoredReportInfo storedReport = (StoredReportInfo) treePaths[i].getLastPathComponent();
			storedReports.add(storedReport);
		}
		return storedReports;
	}
}
