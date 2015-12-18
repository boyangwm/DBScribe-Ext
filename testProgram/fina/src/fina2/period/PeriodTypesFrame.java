/*
 * PeriodTypesFrame.java
 *
 * Created on October 23, 2001, 12:37 PM
 */

package fina2.period;

import java.awt.GridBagConstraints;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.ListSelectionModel;

import fina2.BaseFrame;
import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;

/**
 * 
 * @author vasop
 */
public class PeriodTypesFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	private PeriodTypeAmendAction periodTypeAmendAction;
	private PeriodTypeInsertAction periodTypeInsertAction;
	private PeriodTypeReviewAction periodTypeReviewAction;
	private PeriodTypeDeleteAction periodTypeDeleteAction;

	/** Creates new form PeriodTypesFrame */
	public PeriodTypesFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");

		table = new EJBTable();

		periodTypeAmendAction = new PeriodTypeAmendAction(main.getMainFrame(),
				table);
		periodTypeInsertAction = new PeriodTypeInsertAction(
				main.getMainFrame(), table);
		periodTypeReviewAction = new PeriodTypeReviewAction(
				main.getMainFrame(), table);
		periodTypeDeleteAction = new PeriodTypeDeleteAction(
				main.getMainFrame(), table);

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					if (periodTypeAmendAction.isEnabled()) {
						periodTypeAmendAction.actionPerformed(null);
					} else {
						if (periodTypeReviewAction.isEnabled()) {
							periodTypeReviewAction.actionPerformed(null);
						}
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						if (table.getSelectedRow() == -1) {
							periodTypeInsertAction.setEnabled(canAmend);
							periodTypeAmendAction.setEnabled(false);
							periodTypeReviewAction.setEnabled(false);
							periodTypeDeleteAction.setEnabled(false);
						} else {
							periodTypeInsertAction.setEnabled(canAmend);
							periodTypeAmendAction.setEnabled(canAmend);
							periodTypeReviewAction.setEnabled(canReview
									|| canAmend);
							periodTypeDeleteAction.setEnabled(canDelete);
						}
					}
				});

		initComponents();

		table.setPopupMenu(periodTypePopupMenu);
		scrollPane.setViewportView(table);
		BaseFrame.ensureVisible(this);
	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/PeriodSession");
			PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject
					.narrow(ref, PeriodSessionHome.class);

			PeriodSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));

			table.initTable(colNames, session.getPeriodTypeRows(main
					.getUserHandle(), main.getLanguageHandle()));

			if (table.getRowCount() > 0)
				table.setRowSelectionInterval(0, 0);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.period.PeriodTypesFrame.visible",
					new Boolean(false));
		}
	}

	public void show() {
		if (isVisible())
			return;

		try {
			fina2.security.User user = (fina2.security.User) main
					.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.periods.amend");
			canDelete = user.hasPermission("fina2.periods.delete");
			canReview = user.hasPermission("fina2.periods.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		periodTypeAmendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendItem.setVisible(canAmend);

		periodTypeInsertAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);
		createItem.setVisible(canAmend);

		periodTypeDeleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteItem.setVisible(canDelete);

		periodTypeReviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		reviewItem.setVisible(canAmend || canReview);

		initTable();

		super.show();

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		periodTypePopupMenu = new javax.swing.JPopupMenu();
		createItem = new javax.swing.JMenuItem();
		amendItem = new javax.swing.JMenuItem();
		reviewItem = new javax.swing.JMenuItem();
		deleteItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		printItem = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		refreshItem = new javax.swing.JMenuItem();
		jPanel1 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel6 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();

		createItem.setFont(ui.getFont());
		createItem.setIcon(ui.getIcon("fina2.insert"));
		createItem.setAction(periodTypeInsertAction);
		periodTypePopupMenu.add(createItem);
		amendItem.setFont(ui.getFont());
		amendItem.setIcon(ui.getIcon("fina2.amend"));
		amendItem.setAction(periodTypeAmendAction);
		periodTypePopupMenu.add(amendItem);
		reviewItem.setFont(ui.getFont());
		reviewItem.setIcon(ui.getIcon("fina2.review"));
		reviewItem.setAction(periodTypeReviewAction);
		periodTypePopupMenu.add(reviewItem);
		deleteItem.setFont(ui.getFont());
		deleteItem.setIcon(ui.getIcon("fina2.delete"));
		deleteItem.setAction(periodTypeDeleteAction);
		periodTypePopupMenu.add(deleteItem);
		periodTypePopupMenu.add(jSeparator1);
		printItem.setFont(ui.getFont());
		printItem.setText(ui.getString("fina2.print"));
		printItem.setIcon(ui.getIcon("fina2.print"));
		periodTypePopupMenu.add(printItem);
		periodTypePopupMenu.add(jSeparator2);
		refreshItem.setFont(ui.getFont());
		refreshItem.setText(ui.getString("fina2.refresh"));
		refreshItem.setIcon(ui.getIcon("fina2.refresh"));
		refreshItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refreshItemActionPerformed(evt);
			}
		});

		periodTypePopupMenu.add(refreshItem);

		setTitle(ui.getString("fina2.period.periodTypes"));
		initBaseComponents();
		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel6.setLayout(new java.awt.BorderLayout());

		jPanel6.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 5)));
		jPanel5.setLayout(new java.awt.GridBagLayout());

		createButton.setIcon(ui.getIcon("fina2.insert"));
		createButton.setFont(ui.getFont());
		createButton.setAction(periodTypeInsertAction);
		jPanel5.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		amendButton.setIcon(ui.getIcon("fina2.amend"));
		amendButton.setFont(ui.getFont());
		amendButton.setAction(periodTypeAmendAction);
		jPanel5.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, 1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		reviewButton.setIcon(ui.getIcon("fina2.review"));
		reviewButton.setFont(ui.getFont());
		reviewButton.setAction(periodTypeReviewAction);
		jPanel5.add(reviewButton, UIManager.getGridBagConstraints(0, 2, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		deleteButton.setIcon(ui.getIcon("fina2.delete"));
		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(periodTypeDeleteAction);
		jPanel5.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		jPanel6.add(jPanel5, java.awt.BorderLayout.NORTH);

		jPanel1.add(jPanel6, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Period_Types");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		jPanel4.add(printButton);

		jPanel4.add(refreshButton);

		jPanel4.add(closeButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

	}// GEN-END:initComponents

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	private void refreshItemActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPopupMenu periodTypePopupMenu;
	private javax.swing.JMenuItem createItem;
	private javax.swing.JMenuItem amendItem;
	private javax.swing.JMenuItem reviewItem;
	private javax.swing.JMenuItem deleteItem;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JMenuItem printItem;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JMenuItem refreshItem;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JButton createButton;
	private javax.swing.JButton amendButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	// End of variables declaration//GEN-END:variables

}

class PeriodTypeAmendAction extends javax.swing.AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private PeriodTypeAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	/** Creates new PeriodTypeAmendAction */
	PeriodTypeAmendAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new PeriodTypeAmendDialog(parent, true);

		ui.loadIcon("fina2.amend", "amend.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		int index = table.getSelectedRow();

		dialog.show(table.getSelectedTableRow(), true);

		table.updateRow(index, dialog.getTableRow());
	}

}

class PeriodTypeInsertAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private PeriodTypeAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	PeriodTypeInsertAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new PeriodTypeAmendDialog(parent, true);

		ui.loadIcon("fina2.insert", "insert.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.insert"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(null, true);
		if (dialog.getTableRow() != null)
			table.addRow(dialog.getTableRow());
	}

}

class PeriodTypeReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private PeriodTypeAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	PeriodTypeReviewAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new PeriodTypeAmendDialog(parent, true);
		ui.loadIcon("fina2.review", "review.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(table.getSelectedTableRow(), false);
	}

}

class PeriodTypeDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;
	private PeriodType periodType;

	PeriodTypeDeleteAction(java.awt.Frame parent, EJBTable table) {
		super();

		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(parent, ui
				.getString("fina2.period.periodTypeDeleteQuestion")))
			return;

		try {
			int index = table.getSelectedRow();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/PeriodType");
			PeriodTypeHome home = (PeriodTypeHome) PortableRemoteObject.narrow(
					ref, PeriodTypeHome.class);

			periodType = home.findByPrimaryKey((PeriodTypePK) table
					.getSelectedPK());
			periodType.remove();

			table.removeRow(index);
		} catch (Exception e) {
			Main.errorHandler(parent, Main.getString("fina2.title"), Main
					.getString("fina2.delete.periodType"));
		}

	}

}
