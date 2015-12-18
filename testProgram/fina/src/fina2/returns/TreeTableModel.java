package fina2.returns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import fina2.i18n.Language;
import fina2.ui.table.TableRow;
import fina2.ui.treetable.AbstractTreeTableModel;

public class TreeTableModel extends AbstractTreeTableModel {

	private Logger log = Logger.getLogger(TreeTableModel.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private fina2.Main main = fina2.Main.main;

	private PackageInfo root = new PackageInfo();

	private HashMap packages = new HashMap();

	// Names of the columns.
	protected String[] cNames = { ui.getString("fina2.code")+"|"+ui.getString("fina2.sname")+"|"+ui.getString("fina2.period.toDate")+"|"+ui.getString("fina2.rtype")+"|"+ui.getString("fina2.version"), ui.getString("fina2.from"), ui.getString("fina2.to"), ui.getString("fina2.returns.returnDefinition"), ui.getString("fina2.bank.bank"), ui.getString("fina2.version"), ui.getString("fina2.status"), ui.getString("fina2.type") };

	// Types of the columns.
	protected Class[] cTypes = { fina2.ui.treetable.TreeTableModel.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class };

	private Collection returnRows;

	public TreeTableModel() {
		super(null);
		this.returnRows = returnRows;
	}

	public void setReturns(Collection newRows) {

		// Preserve TableRow class references as Return Manager and Return
		// Amend \ Review frames UI are heavily dependent on them.
		if (returnRows != null) {
			HashMap<Object, TableRow> oldRows = new HashMap<Object, TableRow>();
			for (TableRow oldRow : (Collection<TableRow>) returnRows) {
				oldRows.put(oldRow.getPrimaryKey() + ":" + oldRow.getValue(5), oldRow);
			}
			ArrayList<TableRow> rows = new ArrayList<TableRow>();
			for (TableRow newRow : (Collection<TableRow>) newRows) {
				TableRow oldRow = oldRows.get(newRow.getPrimaryKey() + ":" + newRow.getValue(5));
				if (oldRow != null) {
					for (int i = 0; i < newRow.getColumnCount(); i++) {
						oldRow.setValue(i, newRow.getValue(i));
					}
					newRow = oldRow;
				}
				rows.add(newRow);
			}
			newRows = rows;
		}

		super.root = init(newRows);
		returnRows = newRows;
	}

	public Collection getReturns() {
		return this.returnRows;
	}

	public void removeRow(TableRow row) {
		removeTableRow(row);
		super.root = init(returnRows);
	}

	public TreePath addRow(TableRow row) {
		this.returnRows.add(row);
		super.root = init(returnRows);
		;

		return new TreePath(new Object[] { super.root, getPackageInfo(row), row });
	}

	public void removeRows(Collection rows) {
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			removeTableRow((TableRow) iter.next());
		}

		super.root = init(returnRows);
	}

	private void removeTableRow(TableRow row) {
		for (Iterator iter = returnRows.iterator(); iter.hasNext();) {
			TableRow tableRow = (TableRow) iter.next();
			if (tableRow == row) {
				iter.remove();
			}
		}
	}

	private PackageInfo init(Collection returnRows) {
		root.getItems().clear();
		for (Iterator iter = packages.keySet().iterator(); iter.hasNext();) {
			PackageInfo packageInfo = (PackageInfo) iter.next();
			packageInfo.getItems().clear();
		}

		if (returnRows != null) {
			for (Iterator iter = returnRows.iterator(); iter.hasNext();) {
				TableRow row = (TableRow) iter.next();
				getPackageInfo(row).getItems().add(row);
			}
		}

		ArrayList sortedPackages = new ArrayList(packages.keySet());
		Collections.sort(sortedPackages, new PackageComparator(getDateFormat()));

		for (Iterator iter = sortedPackages.iterator(); iter.hasNext();) {

			PackageInfo packageInfo = (PackageInfo) iter.next();
			if (packageInfo.getItems().size() > 0) {
				Collections.sort(packageInfo.getItems(), new ReturnComparator());
				root.getItems().add(packageInfo);
			}
		}
		return root;
	}

	private String getDateFormat() {
		try {
			return ((Language) main.getLanguageHandle().getEJBObject()).getDateFormat();
		} catch (Exception ex) {
			log.error("Error retrieving date format", ex);
			throw new RuntimeException(ex);
		}
	}

	private PackageInfo getPackageInfo(TableRow returnRow) {

		PackageInfo packageInfo = new PackageInfo(returnRow.getValue(4), returnRow.getValue(1), returnRow.getValue(2), returnRow.getValue(7), returnRow.getValue(5),"");

		if (packages.get(packageInfo) == null) {
			packages.put(packageInfo, packageInfo);
		} else {
			packageInfo = (PackageInfo) packages.get(packageInfo);
		}

		return packageInfo;
	}

	/**
	 * Returns the child of <code>parent</code> at index <code>index</code> in
	 * the parent's child array.
	 * 
	 * @param parent
	 *            a node in the tree, obtained from this data source
	 * @param index
	 *            int
	 * @return the child of <code>parent</code> at index <code>index</code>
	 * @todo Implement this javax.swing.tree.TreeModel method
	 */
	public Object getChild(Object parent, int index) {

		if (parent instanceof PackageInfo) {
			PackageInfo returnPackage = (PackageInfo) parent;
			return returnPackage.getItems().get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns the number of children of <code>parent</code>.
	 * 
	 * @param parent
	 *            a node in the tree, obtained from this data source
	 * @return the number of children of the node <code>parent</code>
	 * @todo Implement this javax.swing.tree.TreeModel method
	 */
	public int getChildCount(Object parent) {

		if (parent instanceof PackageInfo) {
			PackageInfo returnPackage = (PackageInfo) parent;
			return returnPackage.getItems().size();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the value to be displayed for node <code>node</code>, at column
	 * number <code>column</code>.
	 * 
	 * @param node
	 *            Object
	 * @param column
	 *            int
	 * @return Object
	 * @todo Implement this test.TreeTableModel method
	 */
	public Object getValueAt(Object node, int column) {

		Object result = "";

		if (node instanceof TableRow) {
			result = ((TableRow) node).getValue(column);
		}

		return result;
	}

	/**
	 * Returns the number ofs availible column.
	 * 
	 * @return int
	 * @todo Implement this test.TreeTableModel method
	 */
	public int getColumnCount() {
		return cNames.length;
	}

	/**
	 * Returns the name for column number <code>column</code>.
	 * 
	 * @param column
	 *            int
	 * @return String
	 * @todo Implement this test.TreeTableModel method
	 */
	public String getColumnName(int column) {
		return cNames[column];
	}

	public Class getColumnClass(int column) {
		return cTypes[column];
	}
}

class ReturnComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		TableRow row1 = (TableRow) o1;
		TableRow row2 = (TableRow) o2;

		return row1.getValue(0).compareTo(row2.getValue(0));
	}
}

class PackageComparator implements Comparator {

	private Logger log = Logger.getLogger(PackageComparator.class);

	SimpleDateFormat format;

	public PackageComparator(String dateFormat) {
		format = new SimpleDateFormat(dateFormat);
	}

	public int compare(Object o1, Object o2) {

		PackageInfo p1 = (PackageInfo) o1;
		PackageInfo p2 = (PackageInfo) o2;

		int result = p1.getBank().compareTo(p2.getBank());

		if (result == 0) {
			try {
				Date d1 = format.parse(p1.getStartDate());
				Date d2 = format.parse(p2.getStartDate());
				result = d1.compareTo(d2);
			} catch (ParseException ex) {
				log.error("Error parsing date string", ex);
				throw new RuntimeException(ex);
			}
		}

		if (result == 0) {
			result = p1.getReturnType().compareTo(p2.getReturnType());
		}

		if (result == 0) {
			result = p1.getReturnVersion().compareTo(p2.getReturnVersion());
		}

		return result;
	}
}
