package fina2.util.search;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class TreeSearcherImpl implements TreeSearcherBase {

	@Override
	@SuppressWarnings("unchecked")
	public void searctAndSelectTreeNode(DefaultMutableTreeNode root,
			String text, JTree tree) {
		if (root == null || text == null || tree == null) {
			return;
		}

		text = text.toLowerCase();
		DefaultMutableTreeNode cur = null;

		// search word
		for (int i = 1; i < SearchWord.MAX_SEARCH_LEVEL; i++) {
			Enumeration<DefaultMutableTreeNode> r2 = root
					.breadthFirstEnumeration();
			while (r2.hasMoreElements()) {
				cur = r2.nextElement();
				String s = cur.getUserObject().toString().toLowerCase();
				if (SearchWord.searchWord(s, text, i)) {
					setSelection(cur, tree);
					return;
				}
			}
		}
		tree.setSelectionRow(0);
		tree.scrollRowToVisible(0);
	}

	// Select Node
	private void setSelection(DefaultMutableTreeNode cur, JTree tree) {
		TreePath path = new TreePath(cur.getPath());
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
	}
}
