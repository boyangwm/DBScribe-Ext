package fina2.security.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import fina2.Main;
import fina2.security.Role;
import fina2.security.RolePK;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.servergate.SecurityGate;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

/**
 * Implements role printing
 */
class RolePrint {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	/** Indent for printing */
	private String INDENT = "    ";

	private Spreadsheet sheet = null;
	private HashMap<String, String> listTreeItem = null;

	private Object[][] data;

	private int rows;

	private int allColumn;

	private int roleCount = 0;

	private String title;

	private IndeterminateLoading loading = ui.createIndeterminateLoading(main.getMainFrame());

	public RolePrint(String title) {
		this.title = title;
	}

	void printAllRoles() throws Exception {

		List<SecurityItem> tempRoles = null;

		try {
			tempRoles = SecurityGate.getAllRoles();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return; // No sense to continue
		}

		final List<SecurityItem> roles = tempRoles;
		roleCount = roles.size();

		// Without a new thread ProcessDialog works improperly
		Thread t = new Thread() {
			public void run() {

				// Process dialog. sheetView is parent frame.
				loading.start();

				// Printing the roles
				for (int i = 0; i < roles.size(); i++) {
					int roleId = roles.get(i).getId();
					RolePK rolePK = new RolePK(roleId);
					print(rolePK, i + 1);
				}

				initSheet();

				sheet.setDataArray(0, 0, rows - 1, roleCount, data);
				sheet.setOptimalColWidth(0, 100);

				loading.stop();

			} // run()
		};

		t.start();
	}

	/** Prints a given role data */
	void print(final RolePK rolePK) throws Exception {

		// Without a new thread ProcessDialog works improperly
		Thread t = new Thread() {
			public void run() {

				// Process dialog. sheetView is parent frame.
				loading.start();

				// Printing role on the 1-st column
				print(rolePK, 1);

				// Closing progress dialog
				loading.stop();
			} // run()
		};

		t.start();
	}

	/** Prints a given role data */
	private void print(RolePK rolePK, int column) {

		try {

			printGeneralData(rolePK, column);
			printUsers(rolePK, column);
			printPermissions(rolePK, column);
			printReturns(rolePK, column);
			printReports(rolePK, column);
			printReturnVersions(rolePK, column);

			if ((column == 1) && (roleCount == 0)) {
				initSheet();
				sheet.setDataArray(0, 0, rows - 1, column, data);
				sheet.setOptimalColWidth(0, 100);
			}
			allColumn = rows;

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void initSheet() {
		sheet = SpreadsheetsManager.getInstance().createSpreadsheet(title);
		sheet.showGrid(true);
		sheet.setFontName(0, 0, allColumn, rows, ui.getFont().getName());
		sheet.setHorizontalAlign(1, 1, rows, allColumn, Spreadsheet.CENTER);
		sheet.setViewMode(Spreadsheet.VIEW_SIMPLE);
	}

	private Object[][] copyMatrix(Object[][] srcMatrix, Object[][] dest, int endX, int endY) {
		for (int i = 0; i < endY; i++) {
			for (int j = 0; j < endX; j++) {
				srcMatrix[i][j] = dest[i][j];
			}
		}
		return srcMatrix;
	}

	/** Prints a given role general data */
	private void printGeneralData(RolePK rolePK, int column) throws Exception {

		Role role = SecurityGate.getRole(rolePK);

		String text = role.getDescription(Main.main.getLanguageHandle()) + " [" + role.getCode() + "]";

		rows = 1;
		if (column == 1) {
			data = new Object[rows][column + 1];
		} else {
			Object[][] tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column, allColumn);
		}
		data[0][column] = text;
	}

	private void printUsers(RolePK rolePK, int column) throws Exception {
		List<SecurityItem> users = SecurityGate.getRoleUsers(rolePK);

		int rowsTmp = this.rows + users.size() + 1;
		Object[][] tmp = null;

		if (column == 1) {
			tmp = new Object[rowsTmp][column + 1];
			data = copyMatrix(tmp, data, column + 1, rows);
		} else {
			tmp = new Object[allColumn][column + 1];
			data = copyMatrix(tmp, data, column + 1, allColumn);
		}

		if (column == 1) {
			data[rows][0] = fina2.Main.getString("fina2.security.users");
		}

		int count = 1;
		for (SecurityItem item : users) {
			if (item.getReview() == SecurityItem.Status.YES) {
				data[rows + count][column] = "+";
			}

			if (column == 1) {
				data[rows + count][0] = INDENT + item.getText();
			}
			count++;
		}
		rows = rowsTmp;
	}

	private void printPermissions(RolePK rolePK, int column) throws Exception {
		List<SecurityItem> permissions = SecurityGate.getRolePermissions(rolePK);

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

	private void printReturns(RolePK rolePK, int column) throws Exception {

		Map<Integer, TreeSecurityItem> returns = SecurityGate.getRoleReturns(rolePK);
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

	private void printReports(RolePK rolePK, int column) throws Exception {

		Map<Integer, TreeSecurityItem> reports = SecurityGate.getRoleReports(rolePK);
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

	private void printReturnVersions(RolePK rolePK, int column) throws Exception {
		List<SecurityItem> versions = SecurityGate.getRoleReturnVersions(rolePK);

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

	private void printTreeItem(int id, Map<Integer, TreeSecurityItem> returns, Map<Integer, ArrayList<Integer>> childItems, int column, String indent) {
		TreeSecurityItem item = returns.get(id);

		String key = null;
		String value = null;

		key = indent + item.getText();

		if (item.getReview() == SecurityItem.Status.YES) {
			value = "+";
		} else {
			value = " ";
		}
		listTreeItem.put(key, value);

		if (childItems.containsKey(id)) {
			ArrayList<Integer> children = childItems.get(id);
			for (int childId : children) {
				printTreeItem(childId, returns, childItems, column, indent + INDENT);
			}
		}
	}
}
