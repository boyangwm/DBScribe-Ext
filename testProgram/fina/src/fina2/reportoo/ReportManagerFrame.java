/*
 * ReportManagerFrame.java
 *
 * Created on January 7, 2002, 11:15 PM
 */

package fina2.reportoo;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import fina2.BaseFrame;
import fina2.Main;
import fina2.reportoo.server.Report;
import fina2.reportoo.server.ReportHome;
import fina2.reportoo.server.ReportPK;
import fina2.servergate.ReportGate;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

public class ReportManagerFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

	private IndeterminateLoading loading;

	private EJBTree tree;

	private boolean sa = false;
	private boolean canAmend = false;
	private boolean canGenerate = false;
	private boolean canSchedule = false;

	private AmendFolderDialog folderDialog;

	/** Creates new form ReportManagerFrame */
	public ReportManagerFrame() {
		loading = ui.createIndeterminateLoading(main.getMainFrame());
		addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
			public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
				ui.putConfigValue("fina2.reportoo.ReportManagerFrame.visible", new Boolean(false));
				setVisible(false);
				dispose();
			}
		});

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.folder", "node.gif");
		ui.loadIcon("fina2.report", "amend.gif");
		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.up", "up.gif");
		ui.loadIcon("fina2.down", "down.gif");

		tree = new EJBTree();

		tree.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					Node node = tree.getSelectedNode();
					if (node == null)
						return;

					int type = ((Integer) node.getType()).intValue();
					if (type != ReportConstants.NODETYPE_REPORT)
						return;

					if (amendButton.isEnabled()) {
						amendButtonActionPerformed(null);
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {

				Node node = tree.getSelectedNode();

				if (node == null) {
					createButton.setEnabled(false);
					amendButton.setEnabled(false);
					deleteButton.setEnabled(false);
					generateButton.setEnabled(false);
					scheduleButton.setEnabled(false);
					return;
				}

				int type = ((Integer) node.getType()).intValue();

				if (type == ReportConstants.NODETYPE_REPORT) {
					folderButton.setEnabled(false);
					createButton.setEnabled(false);
					generateButton.setEnabled(canGenerate);
					scheduleButton.setEnabled(canGenerate);
					amendButton.setEnabled(canAmend);
					deleteButton.setEnabled(canAmend);
				} else {
					folderButton.setEnabled(canAmend);
					createButton.setEnabled(canAmend);
					amendButton.setEnabled(canAmend);

					if (tree.getSelectedTreeNode().getChildCount() > 0) {
						generateButton.setEnabled(canGenerate);
						scheduleButton.setEnabled(canGenerate);
					} else {
						generateButton.setEnabled(false);
						scheduleButton.setEnabled(false);
					}

					if (tree.getSelectedTreeNode().getChildCount() == 0) {
						deleteButton.setEnabled(canAmend);
					} else {
						deleteButton.setEnabled(false);
					}
				}

				if (type == ReportConstants.NODETYPE_FOLDER && (((ReportPK) node.getPrimaryKey()).getId() != 0)) {
					folderButton.setEnabled(false);
				} else {
					folderButton.setEnabled(canAmend);
				}

				// Change Folder Button enable.
				if (type == ReportConstants.NODETYPE_REPORT && (((ReportPK) node.getPrimaryKey()).getId() != 0)) {
					folderButton.setEnabled(false);
				}

				if (type == -1) {
					createButton.setEnabled(canAmend);
					folderButton.setEnabled(canAmend);
					generateButton.setEnabled(false);
					scheduleButton.setEnabled(false);
					amendButton.setEnabled(false);
					deleteButton.setEnabled(false);
				}
			}
		});

		initComponents();

		scrollPane.setViewportView(tree);
		BaseFrame.ensureVisible(this);
		folderDialog = new AmendFolderDialog(main.getMainFrame(), true, this);
		jbInit();
	}

	public void show() {
		if (isVisible())
			return;

		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.report.amend");
			canGenerate = user.hasPermission("fina2.report.generate");
			canSchedule = user.hasPermission("fina2.reports.scheduler.add");
			sa = user.getLogin().equals("sa");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		createButton.setEnabled(canAmend);
		createButton.setVisible(canAmend);

		amendButton.setEnabled(canAmend);
		amendButton.setVisible(canAmend);

		deleteButton.setEnabled(canAmend);
		deleteButton.setVisible(canAmend);

		folderButton.setEnabled(canAmend);
		folderButton.setVisible(canAmend);

		generateButton.setEnabled(canGenerate);
		scheduleButton.setVisible(canGenerate && canSchedule);
		generateButton.setVisible(canGenerate);

		initTree();

		super.show();

	}

	@SuppressWarnings("unused")
	public void initTree() {
		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			if (!canAmend) {
				ui.putConfigValue("fina2.reportoo.ReportManagerFrame.visible", new Boolean(false));
				// throw new FinaTypeException(Type.PERMISSIONS_DENIED, new
				// String[] { "fina2.report.amend" });
			}
			if (!canGenerate) {
				ui.putConfigValue("fina2.reportoo.ReportManagerFrame.visible", new Boolean(false));
				// throw new FinaTypeException(Type.PERMISSIONS_DENIED, new
				// String[] { "fina2.report.generate" });
			}
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/server/OOReportSession");
			fina2.reportoo.server.OOReportSessionHome home = (fina2.reportoo.server.OOReportSessionHome) PortableRemoteObject.narrow(ref, fina2.reportoo.server.OOReportSessionHome.class);

			fina2.reportoo.server.OOReportSession session = home.create();

			tree.initTree(session.getTreeNodes(main.getUserHandle(), main.getLanguageHandle()));

			tree.addIcon(new Integer(-1), ui.getIcon("fina2.folder"));

			tree.addIcon(new Integer(ReportConstants.NODETYPE_FOLDER), ui.getIcon("fina2.folder"));

			tree.addIcon(new Integer(ReportConstants.NODETYPE_REPORT), ui.getIcon("fina2.report"));

			tree.setRootVisible(true);
			tree.setSelectionRow(0);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
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
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		generateButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		folderButton = new javax.swing.JButton();
		scheduleButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();
		upButton = new javax.swing.JButton();
		downButton = new javax.swing.JButton();

		setTitle(ui.getString("fina2.report.reportManager"));
		initBaseComponents();
		jPanel1.setLayout(new java.awt.BorderLayout());
		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Report_Manager");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel2.add(helpButton);

		jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

		jPanel3.add(refreshButton);

		jPanel3.add(closeButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel6.setLayout(new java.awt.GridBagLayout());

		createButton.setIcon(ui.getIcon("fina2.create"));
		createButton.setFont(ui.getFont());
		createButton.setText(ui.getString("fina2.create"));
		createButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				createButtonActionPerformed(evt);
			}
		});
		jPanel6.add(createButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		amendButton.setIcon(ui.getIcon("fina2.amend"));
		amendButton.setFont(ui.getFont());
		amendButton.setText(ui.getString("fina2.amend"));
		amendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				amendButtonActionPerformed(evt);
			}
		});
		jPanel6.add(amendButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		generateButton.setFont(ui.getFont());
		generateButton.setText(ui.getString("fina2.report.generate"));
		generateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				generateButtonActionPerformed(evt);
			}
		});
		jPanel6.add(generateButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		deleteButton.setIcon(ui.getIcon("fina2.delete"));
		deleteButton.setFont(ui.getFont());
		deleteButton.setText(ui.getString("fina2.delete"));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});
		jPanel6.add(deleteButton, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		folderButton.setIcon(ui.getIcon("fina2.folder"));
		folderButton.setFont(ui.getFont());
		folderButton.setText(ui.getString("fina2.report.folder"));
		folderButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				folderButtonActionPerformed(evt);
			}
		});
		jPanel6.add(folderButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		scheduleButton.setFont(ui.getFont());
		scheduleButton.setText(ui.getString("fina2.report.schedule"));
		scheduleButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				scheduleButtonActionPerformed(evt);
			}
		});
		jPanel6.add(scheduleButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		jPanel5.add(jPanel6, java.awt.BorderLayout.NORTH);

		jPanel4.add(jPanel5, java.awt.BorderLayout.EAST);

		jPanel4.add(scrollPane, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

		// ---------------------------------------------------------------------
		// Up button

		upButton.setIcon(ui.getIcon("fina2.up"));
		upButton.setFont(ui.getFont());
		upButton.setText(ui.getString("fina2.up"));
		upButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				upButtonPressed();
			}
		});
		jPanel6.add(upButton, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(20, 5, 0, 5)));

		// ---------------------------------------------------------------------
		// Down button

		downButton.setIcon(ui.getIcon("fina2.down"));
		downButton.setFont(ui.getFont());
		downButton.setText(ui.getString("fina2.down"));
		downButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				downButtonPressed();
			}
		});
		jPanel6.add(downButton, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

	} // GEN-END:initComponents

	/** Action handler for upButton */
	private void upButtonPressed() {

		DefaultMutableTreeNode currentNode = tree.getSelectedTreeNode();

		if (currentNode == null) {
			// No selected node
			return;
		}

		MutableTreeNode parent = (MutableTreeNode) (tree.getSelectedTreeNode()).getParent();
		int nodeIndex = parent.getIndex((MutableTreeNode) currentNode);

		if (nodeIndex == 0) {
			// Can't move selected node upper
			return;
		}

		// ---------------------------------------------------------------------
		// Update database

		try {

			// The current node
			Node node = (Node) currentNode.getUserObject();
			ReportPK pk = (ReportPK) (node.getPrimaryKey());
			int id = pk.getId();
			ReportGate.setReportSequence(id, nodeIndex - 1);

			// The previous node
			DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) parent.getChildAt(nodeIndex - 1);
			node = (Node) prevNode.getUserObject();
			pk = (ReportPK) (node.getPrimaryKey());
			id = pk.getId();
			ReportGate.setReportSequence(id, nodeIndex);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}

		// ---------------------------------------------------------------------
		// Update GUI

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.removeNodeFromParent(currentNode);
		model.insertNodeInto(currentNode, parent, nodeIndex - 1);
		TreePath selectionPath = new TreePath(currentNode.getPath());
		tree.setSelectionPath(selectionPath);

	}

	/** Action handler for downButton */
	private void downButtonPressed() {

		DefaultMutableTreeNode currentNode = tree.getSelectedTreeNode();

		if (currentNode == null) {
			// No selected node
			return;
		}

		MutableTreeNode parent = (MutableTreeNode) (tree.getSelectedTreeNode()).getParent();
		int nodeIndex = parent.getIndex((MutableTreeNode) currentNode);
		int childCount = parent.getChildCount();

		if (nodeIndex == (childCount - 1)) {
			// Can't move selected node below
			return;
		}

		// ---------------------------------------------------------------------
		// Update database

		try {

			// The current node
			Node node = (Node) currentNode.getUserObject();
			ReportPK pk = (ReportPK) (node.getPrimaryKey());
			int id = pk.getId();
			ReportGate.setReportSequence(id, nodeIndex + 1);

			// The next node
			DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) parent.getChildAt(nodeIndex + 1);
			node = (Node) prevNode.getUserObject();
			pk = (ReportPK) (node.getPrimaryKey());
			id = pk.getId();
			ReportGate.setReportSequence(id, nodeIndex);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}

		// ---------------------------------------------------------------------
		// Update GUI

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.removeNodeFromParent(currentNode);
		model.insertNodeInto(currentNode, parent, nodeIndex + 1);
		TreePath selectionPath = new TreePath(currentNode.getPath());
		tree.setSelectionPath(selectionPath);

	}

	private void scheduleButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_scheduleButtonActionPerformed
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		ScheduleSelectionDialog dlg = new ScheduleSelectionDialog(null, "Select schedule", true);
		dlg.setSize(280, 140);
		dlg.setVisible(true);

		if (dlg.isOK()) {
			Generator generator = new Generator();
			generator.show(node, true, dlg.getScheduleTime(), dlg.isOnDemand());
		}
	} // GEN-LAST:event_scheduleButtonActionPerformed

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTree();
	}

	private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		Generator generator = new Generator();
		generator.show(node, false);
		generator.setExtendedState(Generator.MAXIMIZED_BOTH);
		generator.setVisible(true);

		UIManager.resizeOooSheetPage(generator);
	}

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		if (!ui.showConfirmBox(main.getMainFrame(), ui.getString("fina2.report.deleteQuestion")))
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/reportoo/server/Report");
			ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

			ReportPK pk = (ReportPK) node.getPrimaryKey();
			Report report = home.findByPrimaryKey(pk);

			report.remove();

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(tree.getSelectedTreeNode());

		} catch (Exception e) {
			Main.errorHandler(main.getMainFrame(), Main.getString("fina2.title"), Main.getString("fina2.delete.item"));
		}
	}

	private void folderButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;
		ReportPK pk = (ReportPK) node.getPrimaryKey();

		folderDialog.show(null, pk);

	}

	public void addFolderNode() {
		initTree();
	}

	private void amendButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		final ReportPK pk = (ReportPK) node.getPrimaryKey();
		if (((Integer) node.getType()).intValue() == ReportConstants.NODETYPE_REPORT) {

			Thread t = new Thread() {
				public void run() {
					Designer designer = new Designer();
					loading.start();
					designer.setExtendedState(Designer.MAXIMIZED_BOTH);
					designer.show(tree, tree.getSelectedTreeNode(), pk, null);
					loading.stop();
					designer.setVisible(true);
					// TODO Test Nick
					UIManager.resizeOooSheetPage(designer);
				}
			};
			t.start();
		} else {
			folderDialog.show(pk, null);
			if (folderDialog.isOk()) {
				node.setLabel(folderDialog.getName());
				((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(tree.getSelectedTreeNode());
			}
		}
	}

	private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;
		final ReportPK pk = (ReportPK) node.getPrimaryKey();
		Thread t = new Thread() {
			public void run() {
				Designer designer = new Designer();
				loading.start();
				designer.setSize(d.width, d.height - 35);
				designer.show(tree, tree.getSelectedTreeNode(), null, pk);
				loading.stop();
				designer.setVisible(true);
				// TODO Test Nick
				UIManager.resizeOooSheetPage(designer);
			}
		};
		t.start();

	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ui.putConfigValue("fina2.reportoo.ReportManagerFrame.visible", new Boolean(false));
		setVisible(false);
		dispose();
	}

	/** Exit the Application */
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton amendButton;
	private javax.swing.JButton createButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton folderButton;
	private javax.swing.JButton generateButton;
	private javax.swing.JButton scheduleButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JButton upButton;
	private javax.swing.JButton downButton;

	private void jbInit() {
	}
	// End of variables declaration//GEN-END:variables

}
