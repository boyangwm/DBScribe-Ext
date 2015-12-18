package fina2.util.search;

import java.io.Serializable;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public interface TreeSearcherBase extends Serializable {
	public void searctAndSelectTreeNode(DefaultMutableTreeNode root,
			String text, JTree tree);
}
