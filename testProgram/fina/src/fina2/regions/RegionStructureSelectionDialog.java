package fina2.regions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import fina2.ui.tree.Node;

@SuppressWarnings("serial")
public class RegionStructureSelectionDialog extends JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private RegionStructureTree tree = new RegionStructureTree();
	private Node selectionNode = null;

	private String regionStructureSelectionNodeLabel;

	private Container container;
	private JButton okButton;
	private JButton cancelButton;

	private JPanel buttonPanel;
	private JPanel searchPanel;

	private JTextField searchText;

	private JLabel searchLabel;

	public RegionStructureSelectionDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		initComponents();
		this.setSize(300, 300);
	}

	public void show() {
		tree.initTree();
		tree.setSelectionRow(0);
		setLocationRelativeTo(getParent());
		super.show();
	}

	private void initComponents() {
		container = this.getContentPane();

		// set Title
		setTitle(ui.getString("fina2.bank.regionSelectDialod"));

		// Dialog close Listener
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				isRootOrEmtySelcetion();
			}
		});

		// add Tree
		container.add(new JScrollPane(tree), BorderLayout.CENTER);

		// initial Button Panel
		buttonPanel = new JPanel();

		// Ok Button
		okButton = new JButton();
		okButton.setText(ui.getString("fina2.ok"));
		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okButtonActionPerformed(e);
			}
		});
		buttonPanel.add(okButton);

		// close Button
		cancelButton = new JButton();
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelButtonActionPerformed(e);
			}
		});
		buttonPanel.add(cancelButton);

		container.add(buttonPanel, BorderLayout.SOUTH);

		// search panel
		searchPanel = new JPanel();

		// search Label
		searchLabel = new JLabel(ui.getString("fina2.web.search"));
		searchPanel.add(searchLabel);

		// search text Field
		searchText = new JTextField(20);
		searchText.setFont(ui.getFont());
		searchText.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				searchEnterAction(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				searchEnterAction(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				searchEnterAction(e);
			}
		});
		searchPanel.add(searchText);

		container.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		selectionNode = tree.getSelectedNode();
		regionStructureSelectionNodeLabel = tree
				.getNodePathLabel((RegionStructureNodePK) selectionNode
						.getPrimaryKey());
		dispose();
	}

	// search Text Field Action.
	private void searchEnterAction(KeyEvent e) {
		tree.getTreeSearcher().searctAndSelectTreeNode(
				(DefaultMutableTreeNode) tree.getModel().getRoot(),
				searchText.getText(), tree);
	}

	// is Root selcetion
	private void isRootOrEmtySelcetion() {
		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();

		if (node == null) {
			okButton.setEnabled(false);
		} else {
			RegionStructureNodePK pk = (RegionStructureNodePK) node
					.getPrimaryKey();
			if (pk.getId() == 0 || !treeNode.isLeaf()
					|| treeNode.getLevel() + 1 < tree.getMaxLevel()) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {
		setVisible(false);
		dispose();
	}

	public String getRegionStructureSelectionNodeLabel() {
		return regionStructureSelectionNodeLabel;
	}

	public Node getSelectionNode() {
		return selectionNode;
	}

	public void setSelectionNode(Node selectionNode) {
		this.selectionNode = selectionNode;
	}

	public RegionStructureTree getTree() {
		return tree;
	}
}
