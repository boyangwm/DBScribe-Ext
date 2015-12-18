package fina2.util.search;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class TreeSearcher implements TreeSearcherBase {

	private TreeSearcherBase treeSearcher;

	public TreeSearcher() {
		treeSearcher = new TreeSearcherImpl();
	}

	@Override
	public void searctAndSelectTreeNode(DefaultMutableTreeNode root,
			String text, JTree tree) {
		treeSearcher.searctAndSelectTreeNode(root, text, tree);
	}
}
