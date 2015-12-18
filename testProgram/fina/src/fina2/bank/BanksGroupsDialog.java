package fina2.bank;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import fina2.Main;
import fina2.ui.table.TableRow;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;
import fina2.ui.tree.TreeCheckBoxCellRenderer;

public class BanksGroupsDialog extends JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private Node node;
    private BankPK pk;
    private boolean canAmend;

    public static final int NA = 0;
    public static final int OK = 1;
    public static final int CANCEL = 2;

    private int result = NA;
    private TreeCellRenderer renderer = null;
    private DefaultMutableTreeNode root = null;
    private DefaultTreeModel model = null;
    EJBTree jTree = null;
    Frame owner = null;

    public BanksGroupsDialog(Frame owner, boolean modal) {
        super(owner, modal);

        this.owner = owner;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ui.loadIcon("fina2.cancel", "cancel.gif");
        ui.loadIcon("fina2.cancel", "cancel.gif");
        ui.loadIcon("fina2.default", "default.gif");
        ui.loadIcon("fina2.help", "help.gif");
        ui.loadIcon("fina2.ok", "ok.gif");
        ui.loadIcon("fina2.node.default", "node_def.gif");

        jTree = new EJBTree();
        renderer = new TreeCheckBoxCellRenderer(jTree);

        jbInit();
        setTitle(ui.getString("fina2.bank.bankGroups"));

        pack();
    }

    public BanksGroupsDialog() {
        this(new Frame(), false);
    }

    private void jbInit() {
        this.getContentPane().setLayout(borderLayoutDialog);
        jPanelBottom.setLayout(borderLayoutBottom);

        helpButton.setIcon(ui.getIcon("fina2.help"));
        helpButton.setFont(ui.getFont());
        helpButton.setText(ui.getString("fina2.help"));
        helpButton.setEnabled(false);

        cancelButton.setIcon(ui.getIcon("fina2.cancel"));
        cancelButton.setFont(ui.getFont());
        cancelButton.setText(ui.getString("fina2.cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setIcon(ui.getIcon("fina2.ok"));
        okButton.setFont(ui.getFont());
        okButton.setText(ui.getString("fina2.ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jTree.setModel(model);
        jTree
                .addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
                    public void treeExpanded(
                            javax.swing.event.TreeExpansionEvent event) {
                        TreePath path = event.getPath();
                        if (path.getPathCount() == 2) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                                    .getLastPathComponent();
                            Enumeration enu = node.depthFirstEnumeration();
                            while (enu.hasMoreElements()) {
                                DefaultMutableTreeNode n = (DefaultMutableTreeNode) enu
                                        .nextElement();
                                if (n.getDepth() != 0) {
                                    continue;
                                }
                                ((DefaultTreeModel) jTree.getModel())
                                        .nodeChanged(n);
                            }
                        }
                    }

                    public void treeCollapsed(
                            javax.swing.event.TreeExpansionEvent event) {
                    }
                });
        jTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                TreePath path = jTree.getSelectionPath();
                TreePath locationPath = jTree.getPathForLocation(e.getX(), e
                        .getY());
                if (path != null && path.getPathCount() > 2
                        && path.equals(locationPath)) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path
                            .getLastPathComponent();
                    jTree.getPathForLocation(e.getX(), e.getY());
                    Node node = (Node) ((DefaultMutableTreeNode) treeNode)
                            .getUserObject();

                    boolean isSelected = node.isChecked();
                    Enumeration enu = treeNode.getParent().children();
                    while (enu.hasMoreElements()) {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) enu
                                .nextElement();
                        Node nn = (Node) n.getUserObject();
                        nn.setChecked(false);
                        nn.setDefaultNode(false);
                    }

                    node.setChecked(!isSelected);
                    model.nodeChanged(treeNode);
                }
            }

            public void mousePressed(java.awt.event.MouseEvent e) {
            }
        });
        jTree.setFont(ui.getFont());

        this.getContentPane().add(jPanelBottom, java.awt.BorderLayout.SOUTH);
        jPanelBottom.add(jPanelBottomLeft, java.awt.BorderLayout.WEST);
        jPanelBottomLeft.add(helpButton);
        jPanelBottomRight.add(okButton);
        jPanelBottomRight.add(cancelButton);
        jPanelBottom.add(jPanelBottomRight, java.awt.BorderLayout.EAST);
        this.getContentPane().add(jTree, java.awt.BorderLayout.CENTER);
    }

    BorderLayout borderLayoutDialog = new BorderLayout();
    JPanel jPanelBottom = new JPanel();
    BorderLayout borderLayoutBottom = new BorderLayout();
    JPanel jPanelBottomLeft = new JPanel();
    JPanel jPanelBottomRight = new JPanel();
    JButton helpButton = new JButton();
    JButton cancelButton = new JButton();
    JButton okButton = new JButton();

    public int show(Node node, BankPK pk, boolean canAmend) {
        this.node = node;
        this.pk = pk;
        this.canAmend = canAmend;

        initTree();

        jTree.setEnabled(canAmend);
        setSize(new Dimension(400, 300));
        setLocationRelativeTo(getParent());
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }

        // all results except OK button clicking will return CANCEL
        setResult(CANCEL);
        show();
        return getResult();
    }

    private synchronized void prepareNodes(DefaultMutableTreeNode parent,
            Collection criterionRows, Collection bankGroupRows,
            Collection assignedBankGroups) {
        for (Iterator iter = criterionRows.iterator(); iter.hasNext();) {
            TableRow criterionRow = (TableRow) iter.next();

            Node criterionNode = new Node(criterionRow.getPrimaryKey(),
                    criterionRow.getValue(1), new Integer(
                            BanksConstants.NODETYPE_BANK_CRITERION_NODE));
            if (String.valueOf(BankCriterionConstants.DEF_CRITERION).equals(
                    criterionRow.getValue(2))) { // is default criterion
                criterionNode.setDefaultNode(true);
                criterionNode.setType(new Integer(
                        BanksConstants.NODETYPE_DEF_BANK_CRITERION_NODE));
            }

            DefaultMutableTreeNode criterionTreeNode = new DefaultMutableTreeNode(
                    criterionNode);
            parent.add(criterionTreeNode);
            String criterionId = String.valueOf(((BankCriterionPK) criterionRow
                    .getPrimaryKey()).getId());
            for (Iterator bankGroupIter = bankGroupRows.iterator(); bankGroupIter
                    .hasNext();) {
                TableRow bankGroupRow = (TableRow) bankGroupIter.next();
                if (criterionId.equals(bankGroupRow.getValue(2))) {
                    Node bankGroupNode = new Node(bankGroupRow.getPrimaryKey(),
                            bankGroupRow.getValue(3), new Integer(
                                    BanksConstants.NODETYPE_BANK_GROUP_NODE));
                    bankGroupNode.setChecked(false);
                    for (Iterator assignedIter = assignedBankGroups.iterator(); assignedIter
                            .hasNext();) {
                        BankGroupPK assignedBankGroup = (BankGroupPK) assignedIter
                                .next();
                        if (assignedBankGroup.equals(bankGroupRow
                                .getPrimaryKey())) {
                            bankGroupNode.setChecked(true);
                            break;
                        }
                    }
                    DefaultMutableTreeNode bankGroupTreeNode = new DefaultMutableTreeNode(
                            bankGroupNode);
                    criterionTreeNode.add(bankGroupTreeNode);
                }
            }
        }
    }

    public void initTree() {
        try {
            Node rootNode = new Node(new BankPK(0), ui
                    .getString("fina2.bank.criterions"), new Integer(
                    BanksConstants.NODETYPE_ROOT));

            root = new DefaultMutableTreeNode(rootNode);
            model = new DefaultTreeModel(root);
            jTree.setModel(model);

            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/bank/BankSession");
            BankSessionHome home = (BankSessionHome) PortableRemoteObject
                    .narrow(ref, BankSessionHome.class);
            BankSession session = home.create();

            Object bankRef = jndi.lookup("fina2/bank/Bank");
            BankHome bankHome = (BankHome) PortableRemoteObject.narrow(bankRef,
                    BankHome.class);
            Bank bank = bankHome.findByPrimaryKey(pk);

            Collection assignedBankGroups = bank.getBankGroupPKs();

            prepareNodes(root, session.getBankCriterionRows(main
                    .getUserHandle(), main.getLanguageHandle()), session
                    .getBankGroupsRows(main.getUserHandle(), main
                            .getLanguageHandle()), assignedBankGroups);
            model.setRoot(root);

            jTree.addIcon(new Integer(-1), ui.getIcon("fina2.node"));
            jTree.addIcon(new Integer(BanksConstants.NODETYPE_BANK_GROUP), ui
                    .getIcon("fina2.node"));
            jTree.addIcon(new Integer(BanksConstants.NODETYPE_BANK_GROUP_NODE),
                    ui.getIcon("fina2.node"));
            jTree.addIcon(new Integer(
                    BanksConstants.NODETYPE_BANK_CRITERION_NODE), ui
                    .getIcon("fina2.node"));
            jTree.addIcon(new Integer(
                    BanksConstants.NODETYPE_DEF_BANK_CRITERION_NODE), ui
                    .getIcon("fina2.node.default"));

            jTree.setRootVisible(true);
            jTree.setSelectionRow(0);
            jTree.setFont(ui.getFont());
            jTree.setCellRenderer(renderer);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setResult(CANCEL);
        setVisible(false);
        dispose();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (canAmend) {
            Collection selectedBankGroups = new LinkedList();
            boolean hasDefultCriterionGroupAssigned = false;

            Enumeration criterionEnum = root.children();
            while (criterionEnum.hasMoreElements()) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) criterionEnum
                        .nextElement();
                Node criterionNode = (Node) treeNode.getUserObject();
                Enumeration bankGroupEnum = treeNode.children();
                while (bankGroupEnum.hasMoreElements()) {
                    Node node = (Node) ((DefaultMutableTreeNode) bankGroupEnum
                            .nextElement()).getUserObject();
                    if (node.isChecked()) {
                        selectedBankGroups.add(node.getPrimaryKey());
                        if (criterionNode.isDefaultNode()) {
                            hasDefultCriterionGroupAssigned = true;
                        }
                    }
                }

            }
            if (hasDefultCriterionGroupAssigned) {
                try {
                    InitialContext jndi = fina2.Main.getJndiContext();
                    Object ref = jndi.lookup("fina2/bank/Bank");
                    BankHome home = (BankHome) PortableRemoteObject.narrow(ref,
                            BankHome.class);

                    Bank bank = home.findByPrimaryKey(pk);
                    bank.setBankGroupPKs(selectedBankGroups);
                } catch (Exception e) {
                    Main.generalErrorHandler(e);
                }
                setResult(OK);
                setVisible(false);
                dispose();
            } else {
                ui.showMessageBox(null, ui.getString("fina2.title"), ui
                        .getString("fina2.bank.noDefaultGroup"),
                        javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }

    }

    private void setResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

}
