package fina2.security.users;

import java.util.ArrayList;
import java.util.List;

import fina2.Main;
import fina2.security.RolePK;
import fina2.security.SecurityItem;
import fina2.servergate.SecurityGate;

/**
 * View which represents role return versions data
 */
public class RoleReturnVersionsView extends AbstractTableView<RolePK> {

    /** Role versions data */
    private List<SecurityItem> versionData = null;

    /** Creates the instance of the view */
    RoleReturnVersionsView(ModeType modeType, RolePK rolePK) throws Exception {
        super(modeType, rolePK);

        versionData = loadData();
        initTable(versionData);
    }

    /** Loads the view data */
    private List<SecurityItem> loadData() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading list of all versions */
            return SecurityGate.getAllReturnVersions();
        } else {
            /* Edit mode. Loading current role permissions data */
            return SecurityGate.getRoleReturnVersions(getKey());
        }
    }

    /** Inits the view table */
    private void initTable(List<SecurityItem> modelData) {

        String textColumn = Main.getString("fina2.versions");
        TableViewModel model = new TableViewModel(modelData, textColumn, true);

        super.initTable(model);
    }

    /** Checks the view data */
    public void check() {
        /* Nothing to do */
    }

    /** Saves the view data to DB */
    public void save() throws Exception {

        RolePK rolePK = getKey();
        List<SecurityItem> versions = getSelectedVersions();

        SecurityGate.setRoleReturnVersions(rolePK, versions);
    }

    /** Returns the set of selected permissions id */
    private List<SecurityItem> getSelectedVersions() {

        List<SecurityItem> versions = new ArrayList<SecurityItem>();

        /* Looping through the versions list */
        for (SecurityItem item : versionData) {
            if (item.getReview() == SecurityItem.Status.YES) {
                versions.add(item);
            }
        }

        return versions;
    }

}
