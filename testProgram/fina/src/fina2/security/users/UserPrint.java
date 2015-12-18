package fina2.security.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fina2.Main;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

public class UserPrint {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private String INDENT = "    ";

	private Spreadsheet sheet = null;
	private HashMap<String, String> listTreeItem = null;

	private Object[][] data;

	private int rows;

	private int allColumn;

	private int userCount = 0;

	private String title;

	private IndeterminateLoading loading = ui.createIndeterminateLoading(main.getMainFrame());

	public UserPrint(String title) {
		this.title = title;
	}

	private void initSheet() {
		sheet = SpreadsheetsManager.getInstance().createSpreadsheet(title);
		sheet.showGrid(true);
		sheet.setFontName(0, 0, allColumn, rows, ui.getFont().getName());
		sheet.setHorizontalAlign(1, 1, rows, allColumn, Spreadsheet.CENTER);
		sheet.setViewMode(Spreadsheet.VIEW_SIMPLE);
	}

	void print(final UserPK userPK) throws Exception {
		Thread t = new Thread() {
			public void run() {
				loading.start();
				print(userPK, 1);
				loading.stop();
			}
		};
		t.start();
	}

	void printAllUsers() throws Exception {
		List<SecurityItem> tempUsers = null;

		try {
			tempUsers = SecurityGate.getAllUsers();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}

		final List<SecurityItem> users = tempUsers;
		userCount = users.size();
		Thread t = new Thread() {
			public void run() {
				loading.start();
				for (int i = 0; i < users.size(); i++) {
					int userId = users.get(i).getId();
					UserPK userPK = new UserPK(userId);
					print(userPK, i + 1);
				}

				initSheet();

				sheet.setDataArray(0, 0, rows - 1, userCount, data);
				sheet.setOptimalColWidth(0, 100);
				loading.stop();
			}
		};
		t.start();
	}

	/* Print users list(run as role print) */
	void printAllUsers(final List<SecurityItem> users) throws Exception {
		userCount = users.size();
		Thread t = new Thread() {
			public void run() {
				loading.start();
				for (int i = 0; i < users.size(); i++) {
					int userId = users.get(i).getId();
					UserPK userPK = new UserPK(userId);
					print(userPK, i + 1);
				}

				initSheet();

				sheet.setDataArray(0, 0, rows - 1, userCount, data);
				sheet.setOptimalColWidth(0, 100);
				loading.stop();
			}
		};
		t.start();
	}

	private void print(UserPK userPK, int column) {
		try {
			printGeneralData(userPK, column);
			printRoles(userPK, column);
			printBanks(userPK, column);
			printPermissions(userPK, column);
			printReturns(userPK, column);
			printReports(userPK, column);
			printReturnVersions(userPK, column);

			if ((column == 1) && (userCount == 0)) {
				initSheet();
				sheet.setDataArray(0, 0, rows - 1, column, data);
				sheet.setOptimalColWidth(0, 100);
			}
			allColumn = rows;
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private Object[][] copyMatrix(Object[][] srcMatrix, Object[][] dest, int endX, int endY) {
		for (int i = 0; i < endY; i++) {
			for (int j = 0; j < endX; j++) {
				srcMatrix[i][j] = dest[i][j];
			}
		}
		return srcMatrix;
	}

	/** Prints a given user general data */
	private void printGeneralData(UserPK userPK, int column) throws Exception {
		User user = SecurityGate.getUser(userPK);
		rows = 8;
		if (column == 1) {
			data = new Object[rows][column + 1];
		} else {
			Object[][] tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column, allColumn);
		}

		String text = user.getLogin();
		data[0][column] = text;
		if (column == 1) {
			data[1][0] = Main.getString("fina2.security.general");
			data[2][0] = INDENT + Main.getString("fina2.login.userName");
			data[3][0] = INDENT + Main.getString("fina2.security.title");
			data[4][0] = INDENT + Main.getString("fina2.security.blocked");
			data[5][0] = INDENT + Main.getString("fina2.security.changePassword");
			data[6][0] = INDENT + Main.getString("fina2.security.phone");
			data[7][0] = INDENT + Main.getString("fina2.security.email");
		}
		data[2][column] = user.getName(Main.getCurrentLanguage());
		data[3][column] = user.getTitle(Main.getCurrentLanguage());
		String value = user.getBlocked() ? "+" : "";
		data[4][column] = value;
		value = user.getChangePassword() ? "+" : "";
		data[5][column] = value;
		data[6][column] = user.getPhone();
		data[7][column] = user.getEmail();
	}

	private void printRoles(UserPK userPK, int column) throws Exception {
		List<SecurityItem> roles = SecurityGate.getUserRoles(userPK);

		int rowsTmp = this.rows + roles.size() + 1;
		Object[][] tmp = null;

		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, this.rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, this.allColumn);
		}

		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.security.roles");
		}

		int count = 1;
		for (SecurityItem item : roles) {
			if (column == 1) {
				data[rows + count][0] = INDENT + item.getText();
			}
			if (item.getReview() == SecurityItem.Status.YES) {
				data[rows + count][column] = "+";
			}
			count++;
		}
		rows = rowsTmp;
	}

	private void printBanks(UserPK userPK, int column) throws Exception {
		Map<Integer, TreeSecurityItem> banks = SecurityGate.getUserBanks(userPK);
		Map<Integer, ArrayList<Integer>> childItems = TreeViewModel.initChildNodes(banks);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		Collection<Integer> idList = banks.keySet();
		for (int id : idList) {
			if (childItems.containsKey(id)) {
				listTreeItem = new HashMap<String, String>();
				printTreeItem(id, banks, childItems, column, INDENT);
				list.add(listTreeItem);
			}
		}
		int countSize = 0;
		for (HashMap<String, String> m : list) {
			countSize += m.size();
		}

		int rowsTmp = countSize + rows + 1;
		Object[][] tmp = null;

		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, allColumn);
		}
		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.security.fi");
		}
		int count = 1;
		for (HashMap<String, String> m : list) {
			Iterator<Entry<String, String>> iter = m.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				data[rows + count][0] = entry.getKey();
				data[rows + count][column] = entry.getValue();
				count++;
			}
		}
		rows = rowsTmp;
	}

	private void printPermissions(UserPK userPK, int column) throws Exception {
		List<SecurityItem> permissions = SecurityGate.getUserPermissions(userPK);
		int rowsTmp = permissions.size() + rows + 1;
		Object[][] tmp = null;

		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, allColumn);
		}
		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.security.permissions");
		}
		int count = 1;
		for (SecurityItem item : permissions) {
			if (column == 1) {
				data[rows + count][0] = INDENT + item.getText();
			}
			if (item.getReview() == SecurityItem.Status.YES) {
				data[rows + count][column] = "+";
			} else if (item.getReview() == SecurityItem.Status.YES_READONLY) {
				data[rows + count][column] = "(+)";
			}
			count++;
		}
		rows = rowsTmp;
	}

	private void printReturns(UserPK userPK, int column) throws Exception {
		Map<Integer, TreeSecurityItem> returns = SecurityGate.getUserReturns(userPK);
		Map<Integer, ArrayList<Integer>> childItems = TreeViewModel.initChildNodes(returns);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		Collection<Integer> idList = returns.keySet();
		for (int id : idList) {
			if (childItems.containsKey(id)) {
				listTreeItem = new HashMap<String, String>();
				printTreeItem(id, returns, childItems, column, INDENT);
				list.add(listTreeItem);
				listTreeItem = null;
			}
		}

		int countSize = 0;
		for (HashMap<String, String> m : list) {
			countSize += m.size();
		}

		int rowsTmp = countSize + rows + 1;
		Object[][] tmp = null;

		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, allColumn);
		}

		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.returns");
		}

		int count = 1;
		for (HashMap<String, String> m : list) {
			Iterator<Entry<String, String>> iter = m.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				data[rows + count][0] = entry.getKey();
				data[rows + count][column] = entry.getValue();
				count++;
			}
		}
		rows = rowsTmp;
	}

	private void printReports(UserPK userPK, int column) throws Exception {
		Map<Integer, TreeSecurityItem> reports = SecurityGate.getUserReports(userPK);
		Map<Integer, ArrayList<Integer>> childItems = TreeViewModel.initChildNodes(reports);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		Collection<Integer> idList = reports.keySet();
		for (int id : idList) {
			if (childItems.containsKey(id)) {
				listTreeItem = new HashMap<String, String>();
				printTreeItem(id, reports, childItems, column, INDENT);
				list.add(listTreeItem);
				listTreeItem = null;
			}
		}

		int countSize = 0;
		for (HashMap<String, String> m : list) {
			countSize += m.size();
		}

		int rowsTmp = countSize + rows + 1;
		Object[][] tmp = null;

		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, allColumn);
		}

		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.security.reports");
		}

		int count = 1;
		for (HashMap<String, String> m : list) {
			Iterator<Entry<String, String>> iter = m.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				data[rows + count][0] = entry.getKey();
				data[rows + count][column] = entry.getValue();
				count++;
			}
		}
		rows = rowsTmp;
	}

	private void printReturnVersions(UserPK userPK, int column) throws Exception {
		List<SecurityItem> versions = SecurityGate.getUserReturnVersions(userPK);

		int rowsTmp = versions.size() + rows + 1;
		Object[][] tmp = null;
		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, allColumn);
		}

		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.versions");
		}
		int count = 1;
		for (SecurityItem item : versions) {
			if (column == 1) {
				data[rows + count][0] = INDENT + item.getText();
			}
			String value = " ";
			if (item.getReview() == SecurityItem.Status.YES || item.getReview() == SecurityItem.Status.YES_READONLY) {
				value = "R";
			}
			if (item.getAmend() == SecurityItem.Status.YES || item.getAmend() == SecurityItem.Status.YES_READONLY) {
				value = value + "/A";
			}
			data[rows + count][column] = value;
			count++;
		}
		rows = rowsTmp;
	}

	private void printTreeItem(int id, Map<Integer, TreeSecurityItem> items, Map<Integer, ArrayList<Integer>> childItems, int column, String indent) {
		TreeSecurityItem item = items.get(id);

		String key = null;
		String value = null;

		key = indent + item.getText();

		if (item.getReview() == SecurityItem.Status.YES) {
			value = "+";
		} else if (item.getReview() == SecurityItem.Status.YES_READONLY) {
			value = "(+)";
		} else {
			value = " ";
		}
		listTreeItem.put(key, value);

		if (childItems.containsKey(id)) {
			ArrayList<Integer> children = childItems.get(id);
			for (int childId : children) {
				printTreeItem(childId, items, childItems, column, indent + INDENT);
			}
		}
	}
}
