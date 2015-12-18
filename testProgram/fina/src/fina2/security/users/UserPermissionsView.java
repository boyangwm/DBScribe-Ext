package fina2.security.users;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fina2.Main;
import fina2.security.SecurityItem;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;

/**
 * User permissions view
 */
public class UserPermissionsView extends AbstractTableView<UserPK> {

    /** User permissions list */
    private List<SecurityItem> permissionsList = null;

    /** Creates an instance of the view */
    UserPermissionsView(ModeType modeType, UserPK userPK) throws Exception {
        super(modeType, userPK);

        permissionsList = loadPermissionsList();
        initTable(permissionsList);
    }

    /** Loads the permissions list */
    private List<SecurityItem> loadPermissionsList() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading list of all permissions */
            return SecurityGate.getAllPermissions();
        } else {
            /* Edit mode. Loading current role permissions data */
            return SecurityGate.getUserPermissions(getKey());
        }
    }

    /** Inits the permissions table */
    private void initTable(List<SecurityItem> modelData) {

        String textColumn = Main.getString("fina2.security.permissions");

        /* Data model for the table */
        TableViewModel model = new TableViewModel(modelData, textColumn, false);
        super.initTable(model);
    }

    /** Checks the view data */
    public void check() {
        /* Nothing to do */
    }

    /** Saves the view data */
    public void save() throws Exception {

        UserPK userPK = getKey();
        Set<Integer> permissions = getSelectedPermissions();

        SecurityGate.setUserPermissions(userPK, permissions);
    }

    /** Returns a set of selected permissions */
    private Set<Integer> getSelectedPermissions() {

        Set<Integer> permissions = new HashSet<Integer>();

        /* Looping and retrieving the selected permissions */
        for (SecurityItem item : permissionsList) {
            if (item.getReview() == SecurityItem.Status.YES) {
                permissions.add(item.getId());
            }
        }

        return permissions;
    }
}
