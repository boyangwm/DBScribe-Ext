package fina2.metadata;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;

import fina2.BaseFrame;
import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;

public class MDTDependenciesFrame extends javax.swing.JFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	// private java.awt.Frame parent;

	private javax.swing.JInternalFrame parent;

	private MDTNode mdtNode;
	private Node node;
	private EJBTable table1;
	private EJBTable table2;
	private EJBTable table3;
	private MDTNodePK pk;

	/** Creates new form MDTDependenciesFrame */
	public MDTDependenciesFrame(javax.swing.JInternalFrame parent, boolean modal) {
		// super(parent, modal);
		this.parent = parent;

		table1 = new EJBTable();
		table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table2 = new EJBTable();
		table2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		initComponents();
		scrollPane1.setViewportView(table1);
		scrollPane2.setViewportView(table2);
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
			colNames.add(" ");
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));

			Collection rows = new Vector();

			table1.initTable(colNames, session.getDependencies(main
					.getLanguageHandle(), pk));

			colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));
			colNames.add(ui.getString("fina2.tables"));

			rows = new Vector();

			table2.initTable(colNames, session.getDependendedReturnDefinition(
					main.getLanguageHandle(), pk));

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void show(Node node) {

		try {
			if (node != null) {
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/metadata/MDTNode");
				MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(
						ref, MDTNodeHome.class);

				pk = (MDTNodePK) node.getPrimaryKey();
				mdtNode = home.findByPrimaryKey(pk);

				descriptionText.setText(mdtNode.getDescription(main
						.getLanguageHandle()));
				codeText.setText(mdtNode.getCode());
				int type = mdtNode.getType();

				switch (type) {
				case MDTConstants.NODETYPE_NODE:
					typeText.setText(ui.getString("fina2.node"));
					break;
				case MDTConstants.NODETYPE_INPUT:
					typeText.setText(ui.getString("fina2.input"));
					break;
				case MDTConstants.NODETYPE_VARIABLE:
					typeText.setText(ui.getString("fina2.variable"));
					break;
				default:
					typeText.setText(" ");
				}
			}
			initTable();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}
		// setLocation(parent.getLocation());
		super.show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents

		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		codeText = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		descriptionText = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		typeText = new javax.swing.JTextField();
		jPanel6 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		jButton5 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		jPanel10 = new javax.swing.JPanel();
		jPanel11 = new javax.swing.JPanel();
		scrollPane1 = new javax.swing.JScrollPane();
		jPanel12 = new javax.swing.JPanel();
		scrollPane2 = new javax.swing.JScrollPane();
		jPanel13 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();

		setTitle(ui.getString("fina2.metadata.dependencies"));
		setFont(ui.getFont());
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				formComponentResized(evt);
			}

			public void componentMoved(java.awt.event.ComponentEvent evt) {
				formComponentMoved(evt);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel2.setLayout(new java.awt.BorderLayout());

		jPanel3.setLayout(new java.awt.GridBagLayout());

		jLabel1.setText(ui.getString("fina2.code"));
		jLabel1.setFont(ui.getFont());
		jPanel3.add(jLabel1, UIManager.getGridBagConstraints(0, 0, -1, -1, -1,
				-1, GridBagConstraints.EAST, -1, new java.awt.Insets(5, 15, 5,
						0)));

		codeText.setEditable(false);
		codeText.setColumns(22);
		codeText.setFont(ui.getFont());
		jPanel3.add(codeText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1,
				-1, GridBagConstraints.WEST, -1,
				new java.awt.Insets(0, 5, 0, 0)));

		jLabel2.setText(ui.getString("fina2.description"));
		jLabel2.setFont(ui.getFont());
		jPanel3.add(jLabel2, UIManager.getGridBagConstraints(0, 1, -1, -1, -1,
				-1, GridBagConstraints.EAST, -1, new java.awt.Insets(5, 15, 5,
						0)));

		descriptionText.setEditable(false);
		descriptionText.setColumns(36);
		descriptionText.setFont(ui.getFont());
		jPanel3.add(descriptionText, UIManager.getGridBagConstraints(1, -1, -1,
				-1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0,
						5, 0, 0)));

		jLabel3.setText(ui.getString("fina2.type"));
		jLabel3.setFont(ui.getFont());
		jPanel3.add(jLabel3, UIManager.getGridBagConstraints(0, 2, -1, -1, -1,
				-1, GridBagConstraints.EAST, -1, new java.awt.Insets(5, 15, 5,
						0)));

		typeText.setEditable(false);
		typeText.setColumns(10);
		typeText.setFont(ui.getFont());
		jPanel3.add(typeText, UIManager.getGridBagConstraints(1, 2, -1, -1, -1,
				-1, GridBagConstraints.WEST, -1,
				new java.awt.Insets(0, 5, 0, 0)));

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

		jPanel6.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel7.add(helpButton);

		jPanel6.add(jPanel7, java.awt.BorderLayout.WEST);

		jButton5.setIcon(ui.getIcon("fina2.refresh"));
		jButton5.setFont(ui.getFont());
		jButton5.setText(ui.getString("fina2.refresh"));
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton5ActionPerformed(evt);
			}
		});

		jPanel8.add(jButton5);

		jButton4.setIcon(ui.getIcon("fina2.close"));
		jButton4.setFont(ui.getFont());
		jButton4.setText(ui.getString("fina2.close"));
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		jPanel8.add(jButton4);

		jPanel6.add(jPanel8, java.awt.BorderLayout.EAST);

		jPanel1.add(jPanel6, java.awt.BorderLayout.SOUTH);

		jPanel10.setLayout(new java.awt.BorderLayout());

		jPanel10.setBorder(new javax.swing.border.EtchedBorder());
		jPanel11.setLayout(new java.awt.GridLayout(1, 0));

		jPanel11
				.setBorder(new javax.swing.border.TitledBorder(null, ui
						.getString("fina2.metadata.dependencies"),
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION, ui
								.getFont()));
		scrollPane1.setPreferredSize(new java.awt.Dimension(500, 100));
		jPanel11.add(scrollPane1);

		jPanel10.add(jPanel11, java.awt.BorderLayout.CENTER);

		jPanel12.setLayout(new java.awt.CardLayout());

		jPanel12
				.setBorder(new javax.swing.border.TitledBorder(null, ui
						.getString("fina2.metadata.returndefinition"),
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION, ui
								.getFont()));
		scrollPane2.setPreferredSize(new java.awt.Dimension(500, 100));
		jPanel12.add(scrollPane2, "jScrollPane2");

		jPanel10.add(jPanel12, java.awt.BorderLayout.SOUTH);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		jButton1.setFont(ui.getFont());
		jButton1.setText("Go to");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		jPanel5.add(jButton1, UIManager.getGridBagConstraints(0, 0, -1, -1, -1,
				-1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		jPanel13.add(jPanel5);

		jPanel10.add(jPanel13, java.awt.BorderLayout.EAST);

		jPanel1.add(jPanel10, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

	}// GEN-END:initComponents

	private void formComponentMoved(java.awt.event.ComponentEvent evt) {// GEN-
		// FIRST
		// :
		// event_formComponentMoved
		ui.putConfigValue("fina2.metadata.MDTDependenciesFrame.x", new Integer(
				getX()));
		ui.putConfigValue("fina2.metadata.MDTDependenciesFrame.y", new Integer(
				getY()));
	}// GEN-LAST:event_formComponentMoved

	private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-
		// FIRST
		// :
		// event_formComponentResized
		ui.putConfigValue("fina2.metadata.MDTDependenciesFrame.width",
				new Integer(getWidth()));
		ui.putConfigValue("fina2.metadata.MDTDependenciesFrame.height",
				new Integer(getHeight()));
	}// GEN-LAST:event_formComponentResized

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-
		// FIRST
		// :
		// event_jButton1ActionPerformed
		// parent.toFront();
		TableRowImpl row = (TableRowImpl) table1.getSelectedTableRow();
		parent.show();
		((MDTAmendFrame) parent)
				.findNode((String) row.getValue(1).trim(), true);
		parent.toFront();
	}// GEN-LAST:event_jButton1ActionPerformed

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-
		// FIRST
		// :
		// event_jButton5ActionPerformed
		initTable();
	}// GEN-LAST:event_jButton5ActionPerformed

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-
		// FIRST
		// :
		// event_jButton4ActionPerformed
		hide();
	}// GEN-LAST:event_jButton4ActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_exitForm
		hide();
	}// GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JTextField codeText;
	private javax.swing.JTextField descriptionText;
	private javax.swing.JButton helpButton;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel13;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JScrollPane scrollPane1;
	private javax.swing.JScrollPane scrollPane2;
	private javax.swing.JTextField typeText;
	// End of variables declaration//GEN-END:variables
}