package fina2.security.users;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import fina2.ui.treetable.JTreeTable;
import fina2.ui.treetable.JTreeTable.TreeTableCellEditor;

/**
 * A views with a tree representation
 */
public abstract class AbstractTreeView<K> extends AbstractView<K> {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private JTreeTable tree;

	/** Creates the instance of the class */
	public AbstractTreeView(ModeType modeType, K key) {
		super(modeType, key);
	}

	/** Inits the view tree */
	protected final void initTree(TreeViewModel model, Icon branchIcon,
			Icon leafIcon) {

		tree = createTree(model, branchIcon, leafIcon);
		tree.setFont(ui.getFont());
		tree.getTableHeader().setFont(ui.getFont());
		JScrollPane scrollPane = initScrollPane(tree);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	/** Creates the tree */
	private JTreeTable createTree(TreeViewModel model, Icon branchIcon,
			Icon leafIcon) {

		JTreeTable treeTable = new JTreeTable(model);
		treeTable.setFont(ui.getFont());
		treeTable.getTableHeader().setFont(ui.getFont());

		/* ------------------------------------------------------------------ */
		/* General settings */

		treeTable.getTree().setRootVisible(false);
		treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/* ------------------------------------------------------------------ */
		/* Setting the icons */

		DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();
		tcr.setFont(fina2.Main.main.ui.getFont());
		tcr.setOpenIcon(branchIcon);
		tcr.setClosedIcon(branchIcon);
		tcr.setLeafIcon(leafIcon);
		treeTable.getTree().setCellRenderer(tcr);

		/* ------------------------------------------------------------------ */
		/* Setting the columns */

		/* Review column */
		CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer();
		treeTable.getColumnModel().getColumn(1)
				.setCellRenderer(checkBoxRenderer);

		if (model.hasAmendColumn()) {
			/* There is Amend column */

			/* Text column */
			treeTable.getColumnModel().getColumn(0).setPreferredWidth(300);

			/* Amend column */
			treeTable.getColumnModel().getColumn(2)
					.setCellRenderer(checkBoxRenderer);
		} else {
			/* There is no Amend column */

			/* Text column */
			treeTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		}

		/* The created tree */
		return treeTable;
	}

	/** Creates the scroll pane to store a tree component */
	private JScrollPane initScrollPane(JTreeTable treeTable) {

		JScrollPane scrollPane = new JScrollPane(treeTable);
		scrollPane.getViewport().setBackground(Color.white);

		/* Making the space around */
		Border border = scrollPane.getBorder();
		Border margin = new EmptyBorder(12, 12, 12, 12);
		scrollPane.setBorder(new CompoundBorder(margin, border));

		return scrollPane;
	}

	public JTreeTable getTreeTable() {
		return tree;
	}

	public void setTreeTable(JTreeTable tree) {
		this.tree = tree;
	}

}