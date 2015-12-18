/*
 * ReturnTypesFrame.java
 *
 * Created on 31 ������� 2001 �., 23:09
 */

package fina2.returns;

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
 * @author Administrator
 */
public class ReturnTypesFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;

	private ReturnTypesAmendAction amendAction;
	private ReturnTypesInsertAction insertAction;
	private ReturnTypesReviewAction reviewAction;
	private ReturnTypesDeleteAction deleteAction;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	/** Creates new form ReturnTypesFrame */
	public ReturnTypesFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");

		this.setFont(ui.getFont());

		table = new EJBTable();

		amendAction = new ReturnTypesAmendAction(main.getMainFrame(), table);
		insertAction = new ReturnTypesInsertAction(main.getMainFrame(), table);
		reviewAction = new ReturnTypesReviewAction(main.getMainFrame(), table);
		deleteAction = new ReturnTypesDeleteAction(main.getMainFrame(), table);

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					if (amendAction.isEnabled()) {
						amendAction.actionPerformed(null);
					} else {
						if (reviewAction.isEnabled()) {
							reviewAction.actionPerformed(null);
						}
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (table.getSelectedRow() == -1) {
					insertAction.setEnabled(canAmend);
					amendAction.setEnabled(false);
					reviewAction.setEnabled(false);
					deleteAction.setEnabled(false);
				} else {
					insertAction.setEnabled(canAmend);
					amendAction.setEnabled(canAmend);
					deleteAction.setEnabled(canDelete);
					reviewAction.setEnabled(canReview || canAmend);
				}
			}
		});

		initComponents();

		table.setPopupMenu(popupMenu);

		scrollPane.setViewportView(table);
		BaseFrame.ensureVisible(this);

	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject
					.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));

			table.initTable(
					colNames,
					session.getReturnTypesRows(main.getUserHandle(),
							main.getLanguageHandle()));

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.returns.ReturnTypesFrame.visible",
					new Boolean(false));
		}
	}

	public void show() {
		if (isVisible())
			return;

		try {

			fina2.security.User user = (fina2.security.User) main
					.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.returns.definition.amend");
			canDelete = user.hasPermission("fina2.returns.definition.delete");
			canReview = user.hasPermission("fina2.returns.definition.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendMenuItem.setVisible(canAmend);

		insertAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);
		createMenuItem.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteMenuItem.setVisible(canDelete);

		reviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		reviewMenuItem.setVisible(canAmend || canReview);

		initTable();
		super.show();

	}

	private void initComponents() {// GEN-BEGIN:initComponents
		popupMenu = new javax.swing.JPopupMenu();
		createMenuItem = new javax.swing.JMenuItem();
		amendMenuItem = new javax.swing.JMenuItem();
		reviewMenuItem = new javax.swing.JMenuItem();
		deleteMenuItem = new javax.swing.JMenuItem();
		jPanel3 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();

		createMenuItem.setFont(ui.getFont());
		createMenuItem.setText("Item");
		createMenuItem.setAction(insertAction);
		popupMenu.add(createMenuItem);
		amendMenuItem.setFont(ui.getFont());
		amendMenuItem.setText("Item");
		amendMenuItem.setAction(amendAction);
		popupMenu.add(amendMenuItem);
		reviewMenuItem.setFont(ui.getFont());
		reviewMenuItem.setText("Item");
		reviewMenuItem.setAction(reviewAction);
		popupMenu.add(reviewMenuItem);
		deleteMenuItem.setFont(ui.getFont());
		deleteMenuItem.setText("Item");
		deleteMenuItem.setAction(deleteAction);
		popupMenu.add(deleteMenuItem);

		setTitle(ui.getString("fina2.returns.returnTypesAction"));
		setFont(ui.getFont());
		initBaseComponents();

		jPanel3.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Return_Types");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel7.add(helpButton);

		jPanel3.add(jPanel7, java.awt.BorderLayout.WEST);

		jPanel8.add(printButton);

		jPanel8.add(refreshButton);

		jPanel8.add(closeButton);

		jPanel3.add(jPanel8, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 5)));
		jPanel6.setLayout(new java.awt.GridBagLayout());

		createButton.setFont(ui.getFont());
		createButton.setAction(insertAction);
		jPanel6.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1,
				-1, -1, -1, java.awt.GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		jPanel6.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1,
				-1, -1, -1, java.awt.GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		reviewButton.setFont(ui.getFont());
		reviewButton.setAction(reviewAction);
		jPanel6.add(reviewButton, UIManager.getGridBagConstraints(0, 2, -1, -1,
				-1, -1, -1, java.awt.GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		jPanel6.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1,
				-1, -1, -1, java.awt.GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		jPanel5.add(jPanel6, java.awt.BorderLayout.NORTH);

		jPanel4.add(jPanel5, java.awt.BorderLayout.EAST);

		scrollPane.setFont(ui.getFont());
		jPanel4.add(scrollPane, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

	}// GEN-END:initComponents

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	/** Exit the Application */

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPopupMenu popupMenu;
	private javax.swing.JMenuItem createMenuItem;
	private javax.swing.JMenuItem amendMenuItem;
	private javax.swing.JMenuItem reviewMenuItem;
	private javax.swing.JMenuItem deleteMenuItem;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JButton createButton;
	private javax.swing.JButton amendButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JScrollPane scrollPane;
	// End of variables declaration//GEN-END:variables

}

class ReturnTypesAmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnTypeAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	ReturnTypesAmendAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ReturnTypeAmendDialog(parent, true);

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

class ReturnTypesInsertAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnTypeAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	ReturnTypesInsertAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ReturnTypeAmendDialog(parent, true);

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

class ReturnTypesReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnTypeAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	ReturnTypesReviewAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ReturnTypeAmendDialog(parent, true);
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

class ReturnTypesDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;
	private ReturnType returnType;

	ReturnTypesDeleteAction(java.awt.Frame parent, EJBTable table) {
		super();

		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(parent,
				ui.getString("fina2.returns.returnTypeDeleteQuestion")))
			return;

		try {
			int index = table.getSelectedRow();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnType");
			ReturnTypeHome home = (ReturnTypeHome) PortableRemoteObject.narrow(
					ref, ReturnTypeHome.class);

			returnType = home.findByPrimaryKey((ReturnTypePK) table
					.getSelectedPK());
			returnType.remove();

			table.removeRow(index);
		} catch (Exception e) {
			Main.errorHandler(parent, Main.getString("fina2.title"),
					Main.getString("fina2.delete.returnType"));
		}

	}

}