package fina2.security.users;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fina2.Main;
import fina2.security.SecurityItem;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;

/**
 * User roles view
 */
public class UserRolesView extends AbstractTableView<UserPK> {

	/** Role permissions data */
	private List<SecurityItem> rolesList = null;

	/** Creates an instance of the view */
	UserRolesView(ModeType modeType, UserPK userPK) throws Exception {
		super(modeType, userPK);

		rolesList = loadRolesList();
		initTable(rolesList);
	}

	/** Loads the roles list */
	private List<SecurityItem> loadRolesList() throws Exception {

		if (getModeType() == ModeType.CREATE) {
			/* Create mode. Loading list of all roles */
			return SecurityGate.getAllRoles();
		} else {
			/* Edit mode. Loading the current user roles */
			return SecurityGate.getUserRoles(getKey());
		}
	}

	/** Inits the view table */
	private void initTable(List<SecurityItem> modelData) {

		String textColumn = Main.getString("fina2.security.roles");

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

		UserPK userPK = getKey();
		Set<Integer> roles = getSelectedRoles();

		SecurityGate.setUserRoles(userPK, roles);
	}

	/** Returns the set of selected roles */
	private Set<Integer> getSelectedRoles() {

		Set<Integer> roles = new HashSet<Integer>();

		/* Looping and retrieving selected roles */
		for (SecurityItem item : rolesList) {
			if (item.getReview() == SecurityItem.Status.YES) {
				roles.add(item.getId());
			}
		}

		return roles;
	}
}
