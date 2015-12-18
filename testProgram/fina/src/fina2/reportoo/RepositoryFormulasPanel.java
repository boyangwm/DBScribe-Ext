package fina2.reportoo;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import fina2.Main;
import fina2.reportoo.repository.Folder;
import fina2.reportoo.repository.Formula;
import fina2.reportoo.repository.RepositorySession;
import fina2.reportoo.repository.RepositorySessionHome;

public class RepositoryFormulasPanel extends JPanel {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    public RepositoryFormulasPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initTree(Formula formulaToSelect) {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi
                    .lookup("fina2/reportoo/repository/RepositorySession");
            RepositorySessionHome home = (RepositorySessionHome) PortableRemoteObject
                    .narrow(ref, RepositorySessionHome.class);

            // Get repository tree (formulas)
            RepositorySession session = home.create();
            Folder froot = session.getRepositoryTree();

            DefaultMutableTreeNode root = new DefaultMutableTreeNode(froot);
            TreeNode nodeToSelect = createNode(froot, root, true,
                    formulaToSelect);

            DefaultTreeModel model = new DefaultTreeModel(root);
            formulasTree.setModel(model);

            if (nodeToSelect != null) {
                formulasTree.setSelectionPath(new TreePath(model
                        .getPathToRoot(nodeToSelect)));
            } else {
                formulasTree.setSelectionPath(new TreePath(root.getPath()));
            }
        } catch (Exception ex) {
            Main.generalErrorHandler(ex);
        }
    }

    public JTree getFormulasTree() {
        return formulasTree;
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        scrollPane.setPreferredSize(new java.awt.Dimension(220, 60));
        this.add(scrollPane, java.awt.BorderLayout.CENTER);
        scrollPane.getViewport().add(formulasTree);

        DefaultTreeModel model = (DefaultTreeModel) formulasTree.getModel();
        model.setRoot(new DefaultMutableTreeNode(new Folder("    ")));
        formulasTree.setRootVisible(true);
    }

    JScrollPane scrollPane = new JScrollPane();
    BorderLayout borderLayout1 = new BorderLayout();
    JTree formulasTree = new JTree();

    private TreeNode createNode(Object n, DefaultMutableTreeNode parent,
            boolean root, Formula formulaToSelect) {

        TreeNode result = null;

        if (n instanceof Folder) {
            DefaultMutableTreeNode node = parent;

            if (root == false) {
                node = new DefaultMutableTreeNode(n);
                parent.add(node);
            }

            Folder f = (Folder) n;
            for (Iterator iter = f.getChildren().iterator(); iter.hasNext();) {
                TreeNode treeNode = createNode(iter.next(), node, false,
                        formulaToSelect);
                if (treeNode != null) {
                    result = treeNode;
                }
            }
        } else {
            Formula f = (Formula) n;

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(n);
            parent.add(node);

            if (formulaToSelect != null && formulaToSelect.getId() == f.getId()) {
                result = node;
            }
        }
        return result;
    }
}
