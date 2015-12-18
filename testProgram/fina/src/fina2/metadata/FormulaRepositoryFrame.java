package fina2.metadata;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;
import javax.transaction.UserTransaction;

import fina2.BaseFrame;
import fina2.Main;
import fina2.javascript.Wizard;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;

public class FormulaRepositoryFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private Wizard wizard = null;
	private String source;

	private DNDTextField codeText;

	private Collection refNodes;
	private EJBTable table;
	private MDTNode mdtNode;
	private MDTNodePK pk;
	private MDTNodePK rowPK;
	private int type;
	private int index;
	private Node node;
	private Node prevNode = null;

	/** Creates new form FRFrame */
	public FormulaRepositoryFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.print", "print.gif");

		table = new EJBTable();

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					callWizard();
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						if (table.getSelectedRow() == -1) {
							printButton.setEnabled(false);
						} else {
							printButton.setEnabled(true);
						}
					}
				});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		initComponents();

		codeText = new DNDTextField();

		codeText.setPreferredSize(new java.awt.Dimension(110, 21));
		codeText.setEditable(false);
		jPanel2.add(codeText, new java.awt.GridBagConstraints());

		codeText.getDocument().addDocumentListener(
				new javax.swing.event.DocumentListener() {
					public void changedUpdate(
							javax.swing.event.DocumentEvent documentEvent) {
					}

					public void insertUpdate(
							javax.swing.event.DocumentEvent documentEvent) {
						initDND();
					}

					public void removeUpdate(
							javax.swing.event.DocumentEvent documentEvent) {
					}
				});

		scrollPane.setViewportView(table);
		BaseFrame.ensureVisible(this);
	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(
					ref, MDTSessionHome.class);

			MDTSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));
			colNames.add(ui.getString("fina2.metadata.formulas"));

			if (!(childrenRadio.isSelected() || dependsOnRadio.isSelected() || usedByRadio
					.isSelected())) {
				table.initTable(colNames, new Vector());
			} else if (childrenRadio.isSelected()) {
				table.initTable(colNames, session.getChildren(main
						.getLanguageHandle(), pk));
			} else if (dependsOnRadio.isSelected()) {
				table.initTable(colNames, session.getDependsOn(main
						.getLanguageHandle(), pk));
			} else if (usedByRadio.isSelected()) {
				table.initTable(colNames, session.getUsedBy(main
						.getLanguageHandle(), pk));
			}

			if (table.getRowCount() > 0)
				table.setRowSelectionInterval(0, 0);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void show(Node node) {
		this.node = node;
		show();
	}

	public void show() {
		try {
			if (node != null && prevNode != node) {
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/metadata/MDTNode");
				MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(
						ref, MDTNodeHome.class);

				pk = (MDTNodePK) node.getPrimaryKey();
				mdtNode = home.findByPrimaryKey(pk);

				codeText.setText(mdtNode.getCode());
				type = mdtNode.getType();
			}
			if (prevNode != node || prevNode == null) {
				initButtons();
				initTable();
				prevNode = node;
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		if (isVisible())
			return;
		super.show();
	}

	private void initButtons() {
		try {
			if (type == MDTConstants.NODETYPE_NODE) {
				childrenRadio.setEnabled(true);
				dependsOnRadio.setEnabled(true);
				usedByRadio.setEnabled(true);
			} else if (type == MDTConstants.NODETYPE_INPUT) {
				childrenRadio.setEnabled(false);
				dependsOnRadio.setEnabled(false);
				usedByRadio.setEnabled(true);
			} else if (type == MDTConstants.NODETYPE_VARIABLE) {
				childrenRadio.setEnabled(false);
				dependsOnRadio.setEnabled(true);
				usedByRadio.setEnabled(true);
			} else {
				childrenRadio.setEnabled(false);
				dependsOnRadio.setEnabled(false);
				usedByRadio.setEnabled(false);

				childrenRadio.setSelected(false);
				dependsOnRadio.setSelected(false);
				usedByRadio.setSelected(false);
			}

			if (!(childrenRadio.isSelected() || dependsOnRadio.isSelected() || usedByRadio
					.isSelected())
					&& childrenRadio.isEnabled()) {
				childrenRadio.setSelected(true);
			} else if (!(childrenRadio.isSelected()
					|| dependsOnRadio.isSelected() || usedByRadio.isSelected())
					&& dependsOnRadio.isEnabled()) {
				dependsOnRadio.setSelected(true);
			} else if (!(childrenRadio.isSelected()
					|| dependsOnRadio.isSelected() || usedByRadio.isSelected())) {
				usedByRadio.setSelected(true);
			} else if ((!childrenRadio.isEnabled())
					&& childrenRadio.isSelected()) {
				dependsOnRadio.setSelected(true);
			} else if ((!dependsOnRadio.isEnabled())
					&& dependsOnRadio.isSelected()) {
				usedByRadio.setSelected(true);
			}
		} catch (Exception e) {
			childrenRadio.setEnabled(false);
			dependsOnRadio.setEnabled(false);
			usedByRadio.setEnabled(false);

			childrenRadio.setSelected(false);
			dependsOnRadio.setSelected(false);
			usedByRadio.setSelected(false);
		}
	}

	public void initDND() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref,
					MDTNodeHome.class);

			mdtNode = home.findByCodeExact(codeText.getText().trim());

			pk = (MDTNodePK) mdtNode.getPrimaryKey();
			type = mdtNode.getType();

			initButtons();
			initTable();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void callWizard() {
		try {
			index = table.getSelectedRow();
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref,
					MDTNodeHome.class);

			rowPK = (MDTNodePK) (table.getSelectedTableRow()).getPrimaryKey();
			mdtNode = home.findByPrimaryKey(rowPK);

			source = mdtNode.getEquation();

			fina2.security.User user = (fina2.security.User) main
					.getUserHandle().getEJBObject();

			wizard = new fina2.javascript.Wizard(this, source, user
					.hasPermission("fina2.metadata.amend"));
			wizard.show();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void setVisible(boolean v) {
		if ((v) && (wizard != null)) {
			refNodes = wizard.getRefNodes();
			if (refNodes != null) {
				source = wizard.getSource();
				try {
					InitialContext jndi = fina2.Main.getJndiContext();

					Object ref = jndi.lookup("fina2/metadata/MDTNode");
					MDTNodeHome home = (MDTNodeHome) PortableRemoteObject
							.narrow(ref, MDTNodeHome.class);
					mdtNode = home.findByPrimaryKey(rowPK);

					ref = jndi.lookup("fina2/metadata/MDTSession");
					MDTSessionHome sessionHome = (MDTSessionHome) PortableRemoteObject
							.narrow(ref, MDTSessionHome.class);
					MDTSession session = sessionHome.create();

					UserTransaction trans = main.getUserTransaction(jndi);

					trans.begin();

					try {
						mdtNode.setEquation(source);
						session.setDependentNodes((MDTNodePK) mdtNode
								.getPrimaryKey(), refNodes);
						trans.commit();
					} catch (Exception e) {
						trans.rollback();
						rowPK = null;
						Main.generalErrorHandler(e);
					}
					TableRowImpl tableRow = new TableRowImpl(rowPK, 3);
					tableRow.setValue(0, mdtNode.getCode());
					tableRow.setValue(1, mdtNode.getDescription(main
							.getLanguageHandle()));
					tableRow.setValue(2, mdtNode.getEquation());
					table.updateRow(index, tableRow);
					rowPK = null;
				} catch (Exception e) {
					Main.generalErrorHandler(e);
				}
			}
			wizard = null;
		}
		super.setVisible(v);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		buttonGroup1 = new javax.swing.ButtonGroup();
		jPanel1 = new javax.swing.JPanel();
		codeLabel = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		childrenRadio = new javax.swing.JRadioButton();
		dependsOnRadio = new javax.swing.JRadioButton();
		usedByRadio = new javax.swing.JRadioButton();
		goButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();

		setTitle(ui.getString("fina2.metadata.fr"));
		initBaseComponents();
		jPanel1.setLayout(new java.awt.GridBagLayout());

		codeLabel.setText(ui.getString("fina2.node"));
		codeLabel.setFont(ui.getFont());
		jPanel1.add(codeLabel, new java.awt.GridBagConstraints());

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel1.add(jPanel2, UIManager.getGridBagConstraints(-1, -1, -1, -1,
				-1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		childrenRadio.setFont(ui.getFont());
		childrenRadio.setText(ui.getString("fina2.metadata.children"));
		buttonGroup1.add(childrenRadio);
		jPanel1.add(childrenRadio, UIManager.getGridBagConstraints(-1, -1, -1,
				-1, -1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		dependsOnRadio.setFont(ui.getFont());
		dependsOnRadio.setText(ui.getString("fina2.metadata.dependsOn"));
		buttonGroup1.add(dependsOnRadio);
		jPanel1.add(dependsOnRadio, UIManager.getGridBagConstraints(-1, -1, -1,
				-1, -1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		usedByRadio.setFont(ui.getFont());
		usedByRadio.setText(ui.getString("fina2.metadata.usedBy"));
		buttonGroup1.add(usedByRadio);
		jPanel1.add(usedByRadio, UIManager.getGridBagConstraints(-1, -1, -1,
				-1, -1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		goButton.setFont(ui.getFont());
		goButton.setText(ui.getString("fina2.metadata.go"));
		goButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				goButtonActionPerformed(evt);
			}
		});

		jPanel1.add(goButton, UIManager.getGridBagConstraints(-1, -1, -1, -1,
				-1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel3.setLayout(new java.awt.BorderLayout());

		helpButton.setEnabled(false);
		jPanel4.add(helpButton);

		jPanel3.add(jPanel4, java.awt.BorderLayout.WEST);

		jPanel5.add(printButton);

		jPanel5.add(closeButton);

		jPanel3.add(jPanel5, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

	}// GEN-END:initComponents

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JRadioButton childrenRadio;
	private javax.swing.JLabel codeLabel;
	private javax.swing.JRadioButton dependsOnRadio;
	private javax.swing.JButton goButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JRadioButton usedByRadio;
	// End of variables declaration//GEN-END:variables

}

class DNDTextField extends javax.swing.JTextField implements
		java.awt.dnd.DropTargetListener {

	private java.awt.dnd.DropTarget dropTarget = null;

	public DNDTextField() {
		dropTarget = new java.awt.dnd.DropTarget(this, this);
	}

	public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
	}

	public void dragEnter(java.awt.dnd.DropTargetDragEvent event) {
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	public void drop(java.awt.dnd.DropTargetDropEvent event) {
		try {
			Transferable transferable = event.getTransferable();

			// we accept only Strings
			if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {

				event.acceptDrop(DnDConstants.ACTION_MOVE);
				String s = (String) transferable
						.getTransferData(DataFlavor.stringFlavor);
				setText(s);
				event.getDropTargetContext().dropComplete(true);

			} else {
				event.rejectDrop();
			}
		} catch (IOException exception) {
			event.rejectDrop();
		} catch (UnsupportedFlavorException ufException) {
			event.rejectDrop();
		}
	}

	public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
	}

	public void dropActionChanged(
			java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
	}

}
