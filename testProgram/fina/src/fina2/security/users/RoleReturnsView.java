package fina2.security.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import fina2.Main;
import fina2.security.RolePK;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.servergate.SecurityGate;

/**
 * The view for role returns
 */
class RoleReturnsView extends AbstractTreeView<RolePK> {

    /** Role returns data */
    private Map<Integer, TreeSecurityItem> returnsMap;

    /** Creates the instance of the class */
    RoleReturnsView(ModeType modeType, RolePK key) throws Exception {
        super(modeType, key);

        returnsMap = loadData();
        initTree(returnsMap);
    }

    /** Loads the view data */
    private Map<Integer, TreeSecurityItem> loadData() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading a list of all returns */
            return SecurityGate.getAllReturns();
        } else {
            /* Not create mode. Loading the current role returns data */
            return SecurityGate.getRoleReturns(getKey());
        }
    }

    /** Inits the view tree */
    private void initTree(Map<Integer, TreeSecurityItem> modelData) {

        String textColumn = Main.getString("fina2.returns");
        Icon branchIcon = Main.getIcon("folder.gif");
        Icon leafIcon = Main.getIcon("return_table.gif");

        /* Data model for tree */
        TreeViewModel model = new TreeViewModel(modelData, textColumn, false);
        super.initTree(model, branchIcon, leafIcon);
    }

    /** Checks the view data */
    public void check() {
        /* Nothing to do */
    }

    /** Saves the view data */
    public void save() throws Exception {

        RolePK rolePK = getKey();
        Set<Integer> returns = getSelectedReturns();

        SecurityGate.setRoleReturns(rolePK, returns);
    }

    /** Returns a set of selected returns id */
    private Set<Integer> getSelectedReturns() {

        /* The result set */
        Set<Integer> returns = new HashSet<Integer>();

        /* All items in the returns map */
        Collection<TreeSecurityItem> items = returnsMap.values();

        /* Looping through items */
        for (TreeSecurityItem item : items) {
            if (item.isLeaf() && (item.getReview() == SecurityItem.Status.YES)) {
                /* Item is a return and selected. Adding to the result set */
                returns.add(item.getId());
            }
        }

        /* The result set. Contains only the selected returns */
        return returns;
    }
}
