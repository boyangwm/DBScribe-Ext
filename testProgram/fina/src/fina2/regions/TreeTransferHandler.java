package fina2.regions;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import fina2.Main;
import fina2.ui.tree.Node;

@SuppressWarnings("serial")
public class TreeTransferHandler extends TransferHandler {
	DataFlavor nodesFlavor;
	DataFlavor[] flavors = new DataFlavor[1];
	DefaultMutableTreeNode[] nodesToRemove;

	public TreeTransferHandler() {
		try {
			String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + fina2.ui.tree.Node[].class.getName() + "\"";
			nodesFlavor = new DataFlavor(mimeType);
			flavors[0] = nodesFlavor;
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFound: " + e.getMessage());
		}
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		support.setShowDropLocation(true);
		if (!support.isDataFlavorSupported(nodesFlavor)) {
			return false;
		}

		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		JTree tree = (JTree) support.getComponent();

		int dropRow = tree.getRowForPath(dl.getPath());

		int[] selRows = tree.getSelectionRows();

		for (int i = 0; i < selRows.length; i++) {
			if (selRows[i] == dropRow) {
				return false;
			}
		}

		int action = support.getDropAction();
		if (action == MOVE) {
			return haveCompleteNode(tree, dl.getPath());
		}
		if (action == COPY) {
			return false;
		}

		TreePath dest = dl.getPath();
		DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest.getLastPathComponent();
		TreePath path = tree.getPathForRow(selRows[0]);
		DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		if (firstNode.getChildCount() > 0 && target.getLevel() < firstNode.getLevel()) {
			return false;
		}
		return true;
	}

	private boolean haveCompleteNode(JTree tree, TreePath dest) {
		int[] selRows = tree.getSelectionRows();
		TreePath path = tree.getPathForRow(selRows[0]);

		DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest.getLastPathComponent();

		DefaultMutableTreeNode first = (DefaultMutableTreeNode) path.getLastPathComponent();

		int targetLevel = target.getLevel();
		int firstLevel = first.getLevel();

		if (((targetLevel + 1) != firstLevel)) {
			return false;
		}

		int childCount = first.getChildCount();

		// TODO Node child
		// if (childCount > 0 && selRows.length == 1) {
		// return false;
		// }

		for (int i = 1; i < selRows.length; i++) {
			path = tree.getPathForRow(selRows[i]);
			DefaultMutableTreeNode next = (DefaultMutableTreeNode) path.getLastPathComponent();

			if (first.isNodeChild(next)) {
				if (childCount > selRows.length - 1) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JTree tree = (JTree) c;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null) {

			List<Node> copies = new ArrayList<Node>();

			List<DefaultMutableTreeNode> toRemove = new ArrayList<DefaultMutableTreeNode>();

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();

			Node n = (Node) node.getUserObject();

			DefaultMutableTreeNode copy = copy(n);

			copies.add((Node) copy.getUserObject());

			toRemove.add(node);

			for (int i = 1; i < paths.length; i++) {

				DefaultMutableTreeNode next = (DefaultMutableTreeNode) paths[i].getLastPathComponent();

				Node ne = (Node) next.getUserObject();
				if (next.getLevel() < node.getLevel()) {
					break;
				} else if (next.getLevel() > node.getLevel()) {
					copy.add(copy(ne));
				} else {
					copies.add((Node) copy(ne).getUserObject());
					toRemove.add(next);
				}
			}

			Node[] nodes = copies.toArray(new Node[copies.size()]);

			nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);

			return new NodesTransferable(nodes);
		}
		return null;
	}

	private DefaultMutableTreeNode copy(Node node) {
		return new DefaultMutableTreeNode(node);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if ((action & MOVE) == MOVE) {
			JTree tree = (JTree) source;
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			for (int i = 0; i < nodesToRemove.length; i++) {
				model.removeNodeFromParent(nodesToRemove[i]);
			}
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		Node[] nodes = null;
		try {
			Transferable t = support.getTransferable();
			nodes = (Node[]) t.getTransferData(nodesFlavor);
		} catch (UnsupportedFlavorException ufe) {
			System.out.println("UnsupportedFlavor: " + ufe.getMessage());
		} catch (java.io.IOException ioe) {
			System.out.println("I/O error: " + ioe.getMessage());
		}

		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();

		int childIndex = dl.getChildIndex();
		TreePath dest = dl.getPath();

		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();

		RegionStructureTree tree = (RegionStructureTree) support.getComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		int index = childIndex;

		if (childIndex == -1) {
			index = parent.getChildCount();
		}

		for (int i = 0; i < nodes.length; i++) {
			DefaultMutableTreeNode insertedNode = initNodes(nodes[i]);
			model.insertNodeInto(insertedNode, parent, index++);
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/regions/RegionStructureNode");
				RegionStructureNodeHome home = (RegionStructureNodeHome) PortableRemoteObject.narrow(ref, RegionStructureNodeHome.class);
				Node n = (Node) nodes[i];
				RegionStructureNode region = home.findByPrimaryKey((RegionStructureNodePK) n.getPrimaryKey());
				Node nodeParent = (Node) parent.getUserObject();
				RegionStructureNodePK parentPk = (RegionStructureNodePK) nodeParent.getPrimaryKey();
				region.setParentId(parentPk.getId());
			} catch (Exception ex) {
				Main.generalErrorHandler(ex);
				ex.printStackTrace();
			}

			tree.scrollPathToVisible(dest);
			tree.setSelectionPath(dest);
			tree.expandPath(dest);
		}
		return true;
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

	public class NodesTransferable implements Transferable {
		Node[] nodes;

		public NodesTransferable(Node[] nodes) {
			this.nodes = nodes;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			return nodes;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return nodesFlavor.equals(flavor);
		}
	}
}
