package fina2.security.users;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fina2.Main;
import fina2.security.RolePK;
import fina2.security.SecurityItem;
import fina2.servergate.SecurityGate;

/**
 * View which represents role permissions data
 */
class RolePermissionsView extends AbstractTableView<RolePK> {

    /** Role permissions data */
    private List<SecurityItem> permissionData = null;

    /** Creates the instance of the view */
    RolePermissionsView(ModeType modeType, RolePK rolePK) throws Exception {
        super(modeType, rolePK);

        permissionData = loadData();
        initTable(permissionData);
    }

    /** Loads the view data */
    private List<SecurityItem> loadData() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading list of all permissions */
            return SecurityGate.getAllPermissions();
        } else {
            /* Edit mode. Loading current role permissions data */
            return SecurityGate.getRolePermissions(getKey());
        }
    }

    /** Inits the view table */
    private void initTable(List<SecurityItem> modelData) {

        String textColumn = Main.getString("fina2.security.permissions");

        /* Data model for table */
        TableViewModel model = new TableViewModel(modelData, textColumn, false);
        super.initTable(model);
    }

    /** Checks the view data */
    public void check() {
        /* Nothing to do */
    }

    /** Saves the view data to DB */
    public void save() throws Exception {

        RolePK rolePK = getKey();
        Set<Integer> permissions = getSelectedPermissions();

        SecurityGate.setRolePermissions(rolePK, permissions);
    }

    /** Returns the set of selected permissions id */
    private Set<Integer> getSelectedPermissions() {

        Set<Integer> permissions = new HashSet<Integer>();

        /*
         * Looping the permissions list. If a permissions is selected, then
         * adding it to result set.
         */
        for (SecurityItem item : permissionData) {
            if (item.getReview() == SecurityItem.Status.YES) {
                permissions.add(item.getId());
            }
        }

        return permissions;
    }
}
