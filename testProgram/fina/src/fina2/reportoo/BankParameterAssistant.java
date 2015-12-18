package fina2.reportoo;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import fina2.Main;
import fina2.bank.BankPK;
import fina2.bank.BanksConstants;
import fina2.security.TreeSecurityItem;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;
import fina2.ui.UIManager;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

/**
 * Bank tree assistant for the SelectParameterValuesDialog class.
 */
class BankParameterAssistant {

    /** Source bank tree */
    private EJBTree sourceBankTree = null;

    /** Target bank tree */
    private EJBTree targetBankTree = null;

    /** The root node of the source tree */
    private DefaultMutableTreeNode sourceRootNode = null;

    /** The root node of the target tree */
    private DefaultMutableTreeNode targetRootNode = null;

    /** Constructor */
    BankParameterAssistant() {
        initTargetBankTree();
        initSourceBankTree();
    }

    /** Return source bank tree */
    EJBTree getSourceTree() {
        return sourceBankTree;
    }

    /** Return target bank tree */
    EJBTree getTargetTree() {
        return targetBankTree;
    }

    /** Inits the source bank tree */
    private void initTargetBankTree() {

        UIManager ui = Main.main.ui;

        // ---------------------------------------------------------------------
        // Creating the tree

        targetBankTree = new EJBTree();

        Node rootNode = new Node(new BankPK(0), ui
                .getString("fina2.bank.bankList"), new Integer(0));
        targetBankTree.initTree(rootNode);

        DefaultTreeModel model = (DefaultTreeModel) targetBankTree.getModel();
        targetRootNode = new DefaultMutableTreeNode(rootNode);
        model.setRoot(targetRootNode);

        // ---------------------------------------------------------------------
        // Setting visual side

        targetBankTree.setRootVisible(true);

        targetBankTree.addIcon(BanksConstants.NODETYPE_ROOT, ui
                .getIcon("fina2.node"));
        targetBankTree.addIcon(BanksConstants.NODETYPE_BANK_TYPE, ui
                .getIcon("fina2.node"));
        targetBankTree.addIcon(BanksConstants.NODETYPE_BANK, ui
                .getIcon("fina2.node"));

    }

    /** Inits the source bank tree */
    private void initSourceBankTree() {

        UIManager ui = Main.main.ui;

        // ---------------------------------------------------------------------
        // Creating the tree

        sourceBankTree = new EJBTree();

        Node rootNode = new Node(new BankPK(0), ui
                .getString("fina2.bank.bankList"), new Integer(0));
        sourceBankTree.initTree(rootNode);

        DefaultTreeModel model = (DefaultTreeModel) sourceBankTree.getModel();
        sourceRootNode = new DefaultMutableTreeNode(rootNode);
        model.setRoot(sourceRootNode);

        // ---------------------------------------------------------------------
        // Setting visual side

        sourceBankTree.setRootVisible(true);

        sourceBankTree.addIcon(BanksConstants.NODETYPE_ROOT, ui
                .getIcon("fina2.node"));
        sourceBankTree.addIcon(BanksConstants.NODETYPE_BANK_TYPE, ui
                .getIcon("fina2.node"));
        sourceBankTree.addIcon(BanksConstants.NODETYPE_BANK, ui
                .getIcon("fina2.node"));

        // ---------------------------------------------------------------------
        // Loading the data
        // Loading the banks
        loadBanks(sourceBankTree, model, sourceRootNode);
        expandAllRows(sourceBankTree);

    }

    /** Expands all nodes of given tree */
    private void expandAllRows(EJBTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /** Checks whether any bank is selected */
    boolean hasSelectedBanks() {
        return (targetBankTree.getRowCount() > 1);
    }

    /** Loads bank list */
    private void loadBanks(EJBTree tree, DefaultTreeModel model,
            final DefaultMutableTreeNode rootNode) {

        // ---------------------------------------------------------------------
        // Getting user bank list from server
        Map<Integer, TreeSecurityItem> banksMap = null;
        try {
            User user = (User) Main.main.getUserHandle().getEJBObject();
            UserPK userPK = (UserPK) user.getPrimaryKey();

            banksMap = SecurityGate.getUserBanksOnly(userPK);
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return;
        }
        Collection<TreeSecurityItem> items = banksMap.values();

        // ---------------------------------------------------------------------
        // Adding the bank list to the tree

        for (TreeSecurityItem item : items) {

            BankPK bankPK = new BankPK(item.getId());
            String text = item.getText();

            Integer nodeType = item.isLeaf() ? BanksConstants.NODETYPE_BANK
                    : BanksConstants.NODETYPE_BANK_TYPE;

            Node node = new Node(bankPK, text, nodeType);
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
            treeNode.setUserObject(node);

            // Setting FI code
            String bankCode = (String) item.getProperty("code");
            node.putProperty("bankCode", bankCode);

            DefaultMutableTreeNode parentNode = null;

            if (item.isLeaf()) {
                // Bank. Adding to a bank type node.

                BankPK parentBankPK = new BankPK(item.getParentId());

                // Setting parent's PK
                node.setParentPK(parentBankPK);

                // Looking for parent
                parentNode = tree.findTreeNode(rootNode, parentBankPK);

                if (parentNode == null) {
                    // Not found. Adding to the root.
                    parentNode = rootNode;
                }

            } else {
                // Bank type. Adding to the root node.
                parentNode = rootNode;
            }

            model.insertNodeInto(treeNode, parentNode, parentNode
                    .getChildCount());

        }
    }

    /** Removes current selected node from the target tree */
    void removeTargetNode() {

        // Selected node
        Node node = targetBankTree.getSelectedNode();

        if (!isValidNode(node)) {
            // Not valid node. Nothing to do.
            return;
        }

        // Finding the node from the target tree
        BankPK bankPK = (BankPK) node.getPrimaryKey();
        DefaultMutableTreeNode mutableNode = targetBankTree.findTreeNode(
                targetRootNode, bankPK);

        // Removing
        DefaultTreeModel model = (DefaultTreeModel) targetBankTree.getModel();
        model.removeNodeFromParent(mutableNode);

        // Removing the parent node if it has no children
        BankPK parentPK = (BankPK) node.getParentPK();
        DefaultMutableTreeNode parentNode = targetBankTree.findTreeNode(
                targetRootNode, parentPK);

        if (parentNode.getChildCount() == 0) {
            // No children. Remove.
            model.removeNodeFromParent(parentNode);
        }
    }

    /** Moves the current selected bank node from the source tree to right */
    void moveSourceNodeToRight() {

        // Iterating through all selected nodes, and adding them to the target tree
        Collection<Node> nodes = sourceBankTree.getSelectedNodes();

        for (Node node : nodes) {

            if (!isValidNode(node)) {
                /* Not valid node. Nothing to do. */
                return;
            }

            /* Selected node type: bank, bank type */
            Integer type = (Integer) node.getType();

            if (type == BanksConstants.NODETYPE_BANK) {
                /* Bank node */
                moveBankNodeToRight(node);
            } else if (type == BanksConstants.NODETYPE_BANK_TYPE) {
                /* Bank type node */
                moveBankTypeNodeToRight(node);
            }
        }
    }

    /** Moves the current target node up */
    void moveTargetNodeUp() {

        DefaultMutableTreeNode currentNode = targetBankTree
                .getSelectedTreeNode();

        if (currentNode == null) {
            // No selected node
            return;
        }

        MutableTreeNode parent = (MutableTreeNode) (targetBankTree
                .getSelectedTreeNode()).getParent();
        int nodeIndex = parent.getIndex((MutableTreeNode) currentNode);

        if (nodeIndex == 0) {
            // Can't move selected node upper
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) targetBankTree.getModel();
        model.removeNodeFromParent(currentNode);
        model.insertNodeInto(currentNode, parent, nodeIndex - 1);
        TreePath selectionPath = new TreePath(currentNode.getPath());
        targetBankTree.setSelectionPath(selectionPath);
    }

    /** Moves the current target node down */
    void moveTargetNodeDown() {

        DefaultMutableTreeNode currentNode = targetBankTree
                .getSelectedTreeNode();

        if (currentNode == null) {
            // No selected node
            return;
        }

        MutableTreeNode parent = (MutableTreeNode) (targetBankTree
                .getSelectedTreeNode()).getParent();
        int nodeIndex = parent.getIndex((MutableTreeNode) currentNode);
        int childCount = parent.getChildCount();

        if (nodeIndex == (childCount - 1)) {
            // Can't move selected node below
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) targetBankTree.getModel();
        model.removeNodeFromParent(currentNode);
        model.insertNodeInto(currentNode, parent, nodeIndex + 1);
        TreePath selectionPath = new TreePath(currentNode.getPath());
        targetBankTree.setSelectionPath(selectionPath);
    }

    /** Moves a given bank type node to right */
    private void moveBankTypeNodeToRight(Node node) {

        if (!targetTreeContainsNode(node)) {
            // This node is absent in target tree. Adding.
            addBankTypeNodeToTargetTree(node);
        }

        // Adding all children to the target tree
        addChildrenToTargetTree(node);
    }

    /**
     * Adds all children of given bank type node to the target tree. A child
     * which already in the target tree will not be added.
     */
    private void addChildrenToTargetTree(Node node) {

        BankPK bankPK = (BankPK) node.getPrimaryKey();
        DefaultMutableTreeNode mutableNode = sourceBankTree.findTreeNode(
                sourceRootNode, bankPK);

        // Looping through children
        for (java.util.Enumeration enu = mutableNode.children(); enu
                .hasMoreElements();) {

            DefaultMutableTreeNode childMutableNode = (DefaultMutableTreeNode) enu
                    .nextElement();
            Node childNode = (Node) childMutableNode.getUserObject();

            if (!targetTreeContainsNode(childNode)) {
                // Child isn't in target tree. Adding it to the target tree.
                addBankNodeToTargetTree(childNode);
            }
        }
    }

    /** Returns a vector with selected banks */
    Vector getSelectedBanks() {

        // The result vector
        Vector<String> banksVector = new Vector<String>();

        // Looping through all nodes in the target tree
        for (Enumeration types = targetRootNode.children(); types
                .hasMoreElements();) {

            DefaultMutableTreeNode typeMutableNode = (DefaultMutableTreeNode) types
                    .nextElement();

            for (Enumeration banks = typeMutableNode.children(); banks
                    .hasMoreElements();) {

                DefaultMutableTreeNode bankMutableNode = (DefaultMutableTreeNode) banks
                        .nextElement();
                Node childNode = (Node) bankMutableNode.getUserObject();

                // Adding bank code (not id) to the result vector
                String bankCode = (String) childNode.getProperty("bankCode");
                banksVector.add(bankCode);
            }
        }

        // The result vector
        return banksVector;
    }

    /** Adds a given bank type node to the target tree */
    private void addBankTypeNodeToTargetTree(Node node) {

        /* Creating new bank node for the target tree */
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);

        /* Adding the created bank node to the target tree */
        DefaultTreeModel model = (DefaultTreeModel) targetBankTree.getModel();
        model.insertNodeInto(newNode, targetRootNode, targetRootNode
                .getChildCount());

        /* The added node can be collapsed - just expanding all rows */
        expandAllRows(targetBankTree);

    }

    /** Moves a given bank node to right */
    private void moveBankNodeToRight(Node node) {

        if (targetTreeContainsNode(node)) {
            // This bank node is already in target tree
            return;
        }

        if (!targetTreeContainsParent(node)) {
            // Node parent isn't in target tree. Adding the parent.
            addParentToTargetTree(node);
        }

        // Adding the node to target tree
        addBankNodeToTargetTree(node);

    }

    /** Adds a given node to the target tree */
    private void addBankNodeToTargetTree(Node node) {

        /* Finding the parent from the target tree */
        BankPK parentPK = (BankPK) node.getParentPK();
        DefaultMutableTreeNode parentNode = targetBankTree.findTreeNode(
                targetRootNode, parentPK);

        /* Creating new bank node for the target tree */
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);

        /* Adding the created bank node to the target tree */
        DefaultTreeModel model = (DefaultTreeModel) targetBankTree.getModel();
        model.insertNodeInto(newNode, parentNode, parentNode.getChildCount());

        /* The added node can be collapsed - just expanding all rows */
        expandAllRows(targetBankTree);

    }

    /** Adds the parent of given node to the target tree */
    private void addParentToTargetTree(Node node) {

        // Finding the parent from source tree
        BankPK parentPK = (BankPK) node.getParentPK();
        DefaultMutableTreeNode parentNode = sourceBankTree.findTreeNode(
                sourceRootNode, parentPK);
        Node sourceParentNode = (Node) parentNode.getUserObject();

        // Adding the parent to the target tree
        Node targetParentNode = new Node(parentPK, sourceParentNode.getLabel(),
                BanksConstants.NODETYPE_BANK_TYPE);

        addBankTypeNodeToTargetTree(targetParentNode);

    }

    /** Checks whether the parent of given node in target tree */
    private boolean targetTreeContainsParent(Node node) {

        BankPK parentPK = (BankPK) node.getParentPK();
        DefaultMutableTreeNode parentNode = targetBankTree.findTreeNode(
                targetRootNode, parentPK);

        return (parentNode != null);
    }

    /** Checks whether the target tree contains given node */
    private boolean targetTreeContainsNode(Node node) {

        BankPK bankPK = (BankPK) node.getPrimaryKey();
        DefaultMutableTreeNode foundNode = targetBankTree.findTreeNode(
                targetRootNode, bankPK);

        return (foundNode != null);
    }

    /** Checks whether a given node is valid: not null, not root. */
    private boolean isValidNode(Node node) {

        if (node == null) {
            /* Not valid node */
            return false;
        }

        Integer type = (Integer) node.getType();

        if (type != BanksConstants.NODETYPE_BANK
                && type != BanksConstants.NODETYPE_BANK_TYPE) {
            /* Not bank and bank type node. Not valid node. */
            return false;
        }

        /* Valid node */
        return true;
    }

    /** Selects the given banks */
    public void selectBanks(Vector bankRows) {

        for (int i = 0; i < bankRows.size(); i++) {

            // Find a node with a given bank code
            TableRowImpl row = (TableRowImpl) bankRows.get(i);
            String bankCode = (String) row.getValue(0);
            Node node = getNodeByCode(bankCode);

            // If the node is found, add it to the target tree
            if (node != null) {
                moveBankNodeToRight(node);
            }
        }
    }

    /** Returns a node with given code */
    private Node getNodeByCode(String code) {

        // Loop type nodes
        for (Enumeration types = sourceRootNode.children(); types
                .hasMoreElements();) {

            DefaultMutableTreeNode typeMutableNode = (DefaultMutableTreeNode) types
                    .nextElement();

            // Loop the banks with the current type
            for (Enumeration banks = typeMutableNode.children(); banks
                    .hasMoreElements();) {

                // Get bank code
                DefaultMutableTreeNode bankMutableNode = (DefaultMutableTreeNode) banks
                        .nextElement();
                Node childNode = (Node) bankMutableNode.getUserObject();
                String bankCode = (String) childNode.getProperty("bankCode");

                // Compare the bank code to the given code
                if (bankCode.equals(code)) {
                    // Found. Exit.
                    return childNode;
                }
            }
        }

        // Not found
        return null;
    }
}
