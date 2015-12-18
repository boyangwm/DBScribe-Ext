package fina2.security.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import fina2.Main;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;

public class UserReturnsView extends AbstractTreeView<UserPK> {

    /** User returns map */
    private Map<Integer, TreeSecurityItem> returnsMap;

    /** Creates an instance of the class */
    UserReturnsView(ModeType modeType, UserPK key) throws Exception {
        super(modeType, key);

        returnsMap = loadReturnsMap();
        initTree(returnsMap);
    }

    /** Loads a returns map */
    private Map<Integer, TreeSecurityItem> loadReturnsMap() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading a list of all returns */
            return SecurityGate.getAllReturns();
        } else {
            /* Not create mode. Loading the current role returns data */
            return SecurityGate.getUserReturns(getKey());
        }
    }

    /** Inits the returns tree */
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

        UserPK userPK = getKey();
        Set<Integer> returns = getSelectedReturns();

        SecurityGate.setUserReturns(userPK, returns);
    }

    /** Returns a set of selected returns */
    private Set<Integer> getSelectedReturns() {

        Set<Integer> returns = new HashSet<Integer>();
        Collection<TreeSecurityItem> items = returnsMap.values();

        /* Looping through items */
        for (TreeSecurityItem item : items) {
            if (item.isLeaf() && (item.getReview() == SecurityItem.Status.YES)) {
                /* This return is selected */
                returns.add(item.getId());
            }
        }

        /* The result set. Contains only the selected returns */
        return returns;
    }
}
