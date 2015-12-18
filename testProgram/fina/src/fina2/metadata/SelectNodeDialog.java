/*
 * SelectNodeDialog.java
 *
 * Created on 5 Ноябрь 2001 г., 1:02
 */

package fina2.metadata;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import fina2.Main;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

/**
 *
 * @author  Sh Shalamberidze
 */
public class SelectNodeDialog extends javax.swing.JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private EJBTree tree;

    private boolean ok;

    /** Creates new form SelectNodeDialog */
    public SelectNodeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        ui.loadIcon("fina2.ok", "ok.gif");
        ui.loadIcon("fina2.cancel", "cancel.gif");
        ui.loadIcon("fina2.help", "help.gif");
        ui.loadIcon("fina2.node", "node.gif");
        ui.loadIcon("fina2.input", "input.gif");
        ui.loadIcon("fina2.variable", "variable.gif");

        tree = new EJBTree();
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            DefaultMutableTreeNode node;
            String sync = "sync";

            public void treeCollapsed(TreeExpansionEvent evt) {
            }

            public synchronized void treeExpanded(TreeExpansionEvent evt) {
                node = (DefaultMutableTreeNode) evt.getPath()
                        .getLastPathComponent();
                Thread thread = new Thread() {
                    public void run() {
                        synchronized (sync) {
                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
                                    .getFirstChild();
                            if (((MDTNodePK) ((Node) child.getUserObject())
                                    .getPrimaryKey()).getId() != -1)
                                return;
                            try {
                                InitialContext jndi = fina2.Main
                                        .getJndiContext();
                                Object ref = jndi
                                        .lookup("fina2/metadata/MDTSession");
                                MDTSessionHome home = (MDTSessionHome) PortableRemoteObject
                                        .narrow(ref, MDTSessionHome.class);

                                MDTSession session = home.create();

                                Collection nodes = session.getChildNodes(main
                                        .getUserHandle(), main
                                        .getLanguageHandle(),
                                        (MDTNodePK) ((Node) node
                                                .getUserObject())
                                                .getPrimaryKey());

                                prepareNodes(
                                        (javax.swing.tree.DefaultTreeModel) tree
                                                .getModel(), node, nodes);
                                ((javax.swing.tree.DefaultTreeModel) tree
                                        .getModel())
                                        .removeNodeFromParent(child);

                            } catch (Exception e) {
                                Main.generalErrorHandler(e);
                            }
                        }
                    }
                };
                thread.start();
            }
        });

        initComponents();

        scrollPane.setViewportView(tree);
    }

    private synchronized void prepareNodes(
            javax.swing.tree.DefaultTreeModel model,
            DefaultMutableTreeNode parent, Collection nodes) {
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Node node = (Node) iter.next();
            DefaultMutableTreeNode n = new DefaultMutableTreeNode(node);
            model.insertNodeInto(n, parent, parent.getChildCount());
            if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_NODE) {
                model.insertNodeInto(new DefaultMutableTreeNode(new Node(
                        new MDTNodePK(-1), ui
                                .getString("fina2.metadata.loading"),
                        new Integer(-1))), n, 0);
            }
            //parent.add(n);
        }
    }

    public void show() {
        initTree();
        setLocationRelativeTo(getParent());
        super.show();
    }

    public boolean isOk() {
        return ok;
    }

    public Node getNode() {
        return tree.getSelectedNode();
    }

    public Collection getNodes() {
        return tree.getSelectedNodes();
    }

    private void initTree() {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/metadata/MDTSession");
            MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(
                    ref, MDTSessionHome.class);

            MDTSession session = home.create();

            Node rootNode = new Node(new MDTNodePK(0), "        ", new Integer(
                    -1));
            tree.initTree(rootNode);

            DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNode);
            Collection nodes = session.getChildNodes(main.getUserHandle(), main
                    .getLanguageHandle(), new MDTNodePK(0));

            prepareNodes((javax.swing.tree.DefaultTreeModel) tree.getModel(),
                    root, nodes);

            ((javax.swing.tree.DefaultTreeModel) tree.getModel()).setRoot(root);
            /*tree.initTree(
                session.getTreeNodes(main.getUserHandle(), main.getLanguageHandle())
            );*/

            tree.addIcon(new Integer(-1), ui.getIcon("fina2.node"));

            tree.addIcon(new Integer(MDTConstants.NODETYPE_NODE), ui
                    .getIcon("fina2.node"));
            tree.addIcon(new Integer(MDTConstants.NODETYPE_INPUT), ui
                    .getIcon("fina2.input"));
            tree.addIcon(new Integer(MDTConstants.NODETYPE_VARIABLE), ui
                    .getIcon("fina2.variable"));

            tree.setRootVisible(false);
            //tree.setSelectionRow(0);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        helpButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        helpButton.setIcon(ui.getIcon("fina2.help"));
        helpButton.setFont(ui.getFont());
        helpButton.setText(ui.getString("fina2.help"));
        helpButton.setEnabled(false);
        jPanel2.add(helpButton);

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        okButton.setIcon(ui.getIcon("fina2.ok"));
        okButton.setFont(ui.getFont());
        okButton.setText(ui.getString("fina2.ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel3.add(okButton);

        cancelButton.setIcon(ui.getIcon("fina2.cancel"));
        cancelButton.setFont(ui.getFont());
        cancelButton.setText(ui.getString("fina2.cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel3.add(cancelButton);

        jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        scrollPane.setPreferredSize(new java.awt.Dimension(350, 350));
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        ok = false;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        ok = true;
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        ok = false;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
