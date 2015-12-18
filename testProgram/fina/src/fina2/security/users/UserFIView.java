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

/**
 * User banks view
 */
public class UserFIView extends AbstractTreeView<UserPK> {

    /** Banks data map */
    private Map<Integer, TreeSecurityItem> banksMap;

    /** Creates an instance of the class */
    UserFIView(ModeType modeType, UserPK key) throws Exception {
        super(modeType, key);

        banksMap = loadBanksMap();
        initTree(banksMap);
    }

    /** Loads the banks map */
    private Map<Integer, TreeSecurityItem> loadBanksMap() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading a map of all banks */
            return SecurityGate.getAllBanks();
        } else {
            /* Not create mode. Loading the current role returns data */
            return SecurityGate.getUserBanks(getKey());
        }
    }

    /** Inits the banks tree */
    private void initTree(Map<Integer, TreeSecurityItem> modelData) {

        String textColumn = Main.getString("fina2.security.fi");
        Icon branchIcon = Main.getIcon("folder.gif");
        Icon leafIcon = Main.getIcon("banks.gif");

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
        Set<Integer> banks = getSelectedBanks();

        SecurityGate.setUserBanks(userPK, banks);
    }

    /** Returns a set of selected banks */
    private Set<Integer> getSelectedBanks() {

        /* The result set */
        Set<Integer> banks = new HashSet<Integer>();

        /* All items in the banks map */
        Collection<TreeSecurityItem> items = banksMap.values();

        /* Looping and retrieving selected banks */
        for (TreeSecurityItem item : items) {
            if (item.isLeaf() && (item.getReview() == SecurityItem.Status.YES)) {
                /* This is a selected bank */
                banks.add(item.getId());
            }
        }

        /* The result set. Contains only the selected banks */
        return banks;
    }

}
