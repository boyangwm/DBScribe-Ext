package fina2.regions;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;
import fina2.util.search.TreeSearcher;
import fina2.util.search.TreeSearcherBase;

@SuppressWarnings("serial")
public class RegionStructureTree extends EJBTree {
	private fina2.Main main = fina2.Main.main;
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private TreeSearcherBase treeSearcher = new TreeSearcher();

	public RegionStructureTree() {
		ui.loadIcon("fina2.node", "node.gif");
	}

	private int maxLevel;

	public void initTree() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject
					.narrow(ref, RegionStructureSessionHome.class);

			RegionStructureSession session = home.create();

			Node nodes = session.getTreeNodes(main.getUserHandle(),
					main.getLanguageHandle());
			initAllNodes(nodes, this);

			this.setFont(ui.getFont());

			// Render icons
			this.setCellRenderer(new CustomIconRenderer());

			// Drag and Drop
			this.setDragEnabled(true);
			this.setDropMode(DropMode.ON);
			this.setTransferHandler(new TreeTransferHandler());
			this.getSelectionModel().setSelectionMode(
					TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
			this.setMaxLevel();
		} catch (FinaTypeException ex) {
			Main.generalErrorHandler(ex);
			this.setModel(null);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			this.setModel(null);
		}
	}

	private void initAllNodes(Node rootNode, EJBTree tree) {

		DefaultMutableTreeNode treeNode = initNodes(rootNode);

		DefaultTreeModel model = new DefaultTreeModel(treeNode);

		tree.setModel(model);
	}

	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode initNodes(Node node) {
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
		if (!node.isLeaf()) {
			Iterator<Node> iter = node.getChildren().iterator();
			while (iter.hasNext()) {
				Node n = iter.next();
				treeNode.add(initNodes(n));
			}
		}
		return treeNode;
	}

	public void selectNode(DefaultMutableTreeNode selNode, JTree tree) {
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		TreeNode[] nodes = treeModel.getPathToRoot(selNode);
		TreePath path = new TreePath(nodes);
		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}

	// get Selection nod path label
	public String getNodePathLabel(RegionStructureNodePK pk) {
		String label = null;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject
					.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();
			label = session.getNodePathLabel(pk, main.getLanguageHandle(),
					new StringBuffer("/"));
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			ex.printStackTrace();
		}
		return label;
	}

	// MAX Level
	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel() {
		int maxLevel = 1;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject
					.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();

			Map<Integer, String> map = session.getProperties(main
					.getLanguageHandle());
			String temp = map.get(0);
			if (temp != null)
				maxLevel = Integer.parseInt(temp);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.maxLevel = maxLevel;
	}

	public TreeSearcherBase getTreeSearcher() {
		return treeSearcher;
	}

	public void setTreeSearcher(TreeSearcherBase treeSearcher) {
		this.treeSearcher = treeSearcher;
	}

	// Renders
	private class CustomIconRenderer extends DefaultTreeCellRenderer {
		private fina2.ui.UIManager ui = fina2.Main.main.ui;

		public CustomIconRenderer() {
			ui.loadIcon("fina2.country", "languages.gif");
			ui.loadIcon("fina2.state", "state.gif");
			ui.loadIcon("fina2.city", "city.gif");
			ui.loadIcon("fina2.other.regions", "other-regions.png");
		}

		public Component getTreeCellRendererComponent(JTree tree,

		Object value, boolean sel, boolean expanded, boolean leaf,

		int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel,

			expanded, leaf, row, hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

			int level = node.getLevel();

			if (level > 3) {
				setIcon(ui.getIcon("fina2.other.regions"));
			} else {
				switch (level) {
				case 1: {
					setIcon(ui.getIcon("fina2.country"));
					break;
				}
				case 2: {
					setIcon(ui.getIcon("fina2.state"));
					break;
				}
				case 3: {
					setIcon(ui.getIcon("fina2.city"));
					break;
				}
				}
			}
			return this;

		}
	}

	public PrintData createPrintDate(DefaultMutableTreeNode root) {
		return new PrintData(root);
	}

	class PrintData implements Serializable {
		private ArrayList<ArrayList<DefaultMutableTreeNode>> all = new ArrayList<ArrayList<DefaultMutableTreeNode>>();

		public PrintData(DefaultMutableTreeNode root) {
			getChildren(root, new ArrayList<DefaultMutableTreeNode>());
		}

		// Get Printed Data
		public ArrayList<TableRowImpl> getPrintedData() {
			ArrayList<TableRowImpl> al = new ArrayList<TableRowImpl>();
			int maxColumn = getMaxColumn();
			for (ArrayList<DefaultMutableTreeNode> allColumn : all) {
				TableRowImpl tableRowImpl = new TableRowImpl(allColumn,
						(maxColumn * 2));
				int count = 0;
				for (DefaultMutableTreeNode n : allColumn) {
					String nodeText = n.getUserObject().toString();
					StringTokenizer st1 = new StringTokenizer(nodeText, "[");
					StringTokenizer st2 = new StringTokenizer(st1.nextElement()
							.toString(), "]");

					String code = st2.nextElement().toString();
					String description = st2.nextElement().toString();

					tableRowImpl.setValue(count, code);
					tableRowImpl.setValue(count + 1, description);
					count += 2;
				}
				al.add(tableRowImpl);
			}
			return al;
		}

		public int getMaxColumn() {
			int max = 0;
			for (ArrayList<DefaultMutableTreeNode> n : all) {
				if (max < n.size())
					max = n.size();
			}
			return max;
		}

		@SuppressWarnings("unchecked")
		private void getChildren(DefaultMutableTreeNode node,
				ArrayList<DefaultMutableTreeNode> list) {
			Enumeration<DefaultMutableTreeNode> n = node.children();
			if (!node.isRoot())
				list.add(node);
			if (node.isLeaf()) {
				all.add((ArrayList<DefaultMutableTreeNode>) list.clone());
				list.remove(node);
			}
			while (n.hasMoreElements()) {
				getChildren(n.nextElement(), list);
				if (!n.hasMoreElements()) {
					list.remove(node);
				}
			}
		}
	}

}
