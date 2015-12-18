package fina2.security.users;

import java.util.ArrayList;
import java.util.List;

import fina2.Main;
import fina2.security.SecurityItem;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;

/**
 * User return versions view
 */
class UserReturnVersionsView extends AbstractTableView<UserPK> {

    /** User return versions list */
    private List<SecurityItem> versionsList = null;

    /** Creates the instance of the view */
    UserReturnVersionsView(ModeType modeType, UserPK UserPK) throws Exception {
        super(modeType, UserPK);

        versionsList = loadData();
        initTable(versionsList);
    }

    /** Loads the view data */
    private List<SecurityItem> loadData() throws Exception {

        if (getModeType() == ModeType.CREATE) {
            /* Create mode. Loading list of all versions */
            return SecurityGate.getAllReturnVersions();
        } else {
            /* Edit mode. Loading current role permissions data */
            return SecurityGate.getUserReturnVersions(getKey());
        }
    }

    /** Inits the return verions table */
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

        UserPK UserPK = getKey();
        List<SecurityItem> versions = getSelectedVersions();

        SecurityGate.setUserReturnVersions(UserPK, versions);
    }

    /** Returns the set of selected permissions id */
    private List<SecurityItem> getSelectedVersions() {

        List<SecurityItem> versions = new ArrayList<SecurityItem>();

        /* Looping and retrieving the selected versions */
        for (SecurityItem item : versionsList) {
            if ((item.getReview() == SecurityItem.Status.YES)
                    || (item.getAmend() == SecurityItem.Status.YES)) {
                versions.add(item);
            }
        }

        /* The result list. Contains only selected versions */
        return versions;
    }

}
