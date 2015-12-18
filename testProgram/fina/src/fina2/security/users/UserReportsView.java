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
 * User reports view
 */
public class UserReportsView extends AbstractTreeView<UserPK> {

	/** User reports data */
	private Map<Integer, TreeSecurityItem> reportsMap;

	/** Creates an instance of the class */
	UserReportsView(ModeType modeType, UserPK key) throws Exception {
		super(modeType, key);

		reportsMap = loadReportsMap();
		initTree(reportsMap);
	}

	/** Loads the reports map */
	private Map<Integer, TreeSecurityItem> loadReportsMap() throws Exception {

		if (getModeType() == ModeType.CREATE) {
			/* Create mode. Loading all reports */
			return SecurityGate.getAllReports();
		} else {
			/* Edit mode. Loading the current role reports data */
			return SecurityGate.getUserReports(getKey());
		}
	}

	/** Inits the view tree */
	private void initTree(Map<Integer, TreeSecurityItem> modelData) {

		String textColumn = Main.getString("fina2.security.reports");
		Icon branchIcon = Main.getIcon("folder.gif");
		Icon leafIcon = Main.getIcon("amend.gif");

		/* Data model for tree */
		TreeViewModel model = new TreeViewModel(modelData, textColumn, false);
		super.initTree(model, branchIcon, leafIcon);
	}

	/** Checks the view data. */
	public void check() {
		/* Nothing to do */
	}

	/** Saves the view data */
	public void save() throws Exception {

		UserPK UserPK = getKey();
		Set<Integer> reports = getSelectedReports();

		SecurityGate.setUserReports(UserPK, reports);
	}

	/** Returns a set of selected reports */
	private Set<Integer> getSelectedReports() {

		Set<Integer> reports = new HashSet<Integer>();
		Collection<TreeSecurityItem> items = reportsMap.values();

		/* Looping and retrieving the selected reports */
		for (TreeSecurityItem item : items) {
			if (item.isLeaf() && (item.getReview() == SecurityItem.Status.YES)) {
				/* Item is a report and it is selected */
				reports.add(item.getId());
			}
		}

		/* The result set. Contains only the selected reports */
		return reports;
	}
}
