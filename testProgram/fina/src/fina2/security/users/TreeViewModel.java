package fina2.security.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fina2.Main;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.ui.treetable.AbstractTreeTableModel;

/**
 * Defines the model for the view with a tree representation
 * 
 * @author Askhat Asanaliev, Jan 2007
 */
public class TreeViewModel extends AbstractTreeTableModel {

	/** Id of the root node */
	private final int ROOT_ID = 0;

	/** The names of the columns */
	private String[] columnNames = null;

	/** The types of the columns */
	private Class[] columnTypes = null;

	/** The data of this model: a map which contains id's and items for tree */
	private Map<Integer, TreeSecurityItem> nodes = null;

	/** Contains a list to the child items for the tree nodes */
	private Map<Integer, ArrayList<Integer>> childNodes = null;

	/** Creates an instance of the class */
	public TreeViewModel(Map<Integer, TreeSecurityItem> modelData,
			String textColumn, boolean hasAmendColumn) {
		super(modelData);

		initColumns(textColumn, hasAmendColumn);

		nodes = modelData;
		childNodes = initChildNodes(nodes);

		/* Setting the branch items selection beginning from the root */
		setBranchItemSelection(ROOT_ID);
	}

	/**
	 * Sets the branch items selection according to the selection of the
	 * children. If all children of branch are selected then this branch is also
	 * selected.
	 */
	private void setBranchItemSelection(int itemId) {

		ArrayList<Integer> childList = childNodes.get(itemId);

		if (childList == null) {
			/* The given item doesn't have children. Nothing to do. */
			return;
		}

		/* Looping through the children */
		for (int childId : childList) {
			/* Updating the children */
			setBranchItemSelection(childId);
		}

		/* Updating the item itself */
		TreeSecurityItem item = nodes.get(itemId);

		if ((itemId == ROOT_ID) || (item == null)) {
			/* This is the root or there is no such item. Nothing to do. */
			return;
		}

		SecurityItem.Status review = getChildrenSelection(itemId, 1);
		SecurityItem.Status amend = getChildrenSelection(itemId, 2);

		item.setReview(review);
		item.setAmend(amend);
	}

	/** Updates the selection of branch items */
	public void updateBranchNodesSelection() {

		/* Set of items, which have child items */
		Set<Integer> branchItems = childNodes.keySet();

		for (Integer itemId : branchItems) {
			/* Updating current item state */
			setBranchItemSelection(itemId);
		}
	}

	/** Checks whether the children of item (parentId) are selected */
	private SecurityItem.Status getChildrenSelection(int parentId, int column) {

		ArrayList<Integer> childList = childNodes.get(parentId);

		if (childList == null) {
			/* No children. Nothing to do */
			return SecurityItem.Status.NO;
		}

		int roleSelectedCount = 0;
		int selectedCount = 0;

		/* Looping through the children */
		for (int childId : childList) {

			/* Retrieving the current child */
			TreeSecurityItem item = nodes.get(childId);
			
			/*
			 * Getting the status of the child. If 1-st column, the review
			 * status should be read; 2-nd column - amend.
			 */
			SecurityItem.Status status = (column == 1) ? item.getReview() : item.getAmend();

			boolean roleSelected = (status == SecurityItem.Status.YES_READONLY) ? true : false;
			boolean selected = (status == SecurityItem.Status.YES) ? true : false;

			if (roleSelected) {
				roleSelectedCount++;
			}
			if (selected) {
				selectedCount++;
			}
		}

		/* ------------------------------------------------------------------ */
		/* Evaluting the result */

		SecurityItem.Status result = SecurityItem.Status.NO;

		if (roleSelectedCount == childList.size()) {
			/* All children are selected / provided by role(s) */
			result = SecurityItem.Status.YES_READONLY;
		} else if (selectedCount == childList.size()) {
			/* All children are selected */
			result = SecurityItem.Status.YES;
		} else if ((selectedCount + roleSelectedCount) == childList.size()) { 
			/* All children are selected (part by role, part by user) */
			result = SecurityItem.Status.YES;
		} else	if ((selectedCount > 0 || roleSelectedCount > 0) && ((selectedCount+roleSelectedCount) < childList.size())) {
			/* The children are selected partially */
			result = SecurityItem.Status.PARTIAL;
		} else if ((selectedCount+roleSelectedCount) == 0) {
			/* No child is selected */
			result = SecurityItem.Status.NO;
		}

		return result;
	}

	/** Inits columns data */
	private void initColumns(String textColumn, boolean hasAmendColumn) {

		if (hasAmendColumn) {
			/* There is "amend" column */

			columnNames = new String[] { textColumn,
					Main.getString("fina2.review"),
					Main.getString("fina2.amend") };

			columnTypes = new Class[] {
					fina2.ui.treetable.TreeTableModel.class, Boolean.class,
					Boolean.class };

		} else {
			/* There is no "amend" column */

			columnNames = new String[] { textColumn, "" };

			columnTypes = new Class[] {
					fina2.ui.treetable.TreeTableModel.class, Boolean.class };
		}
	}

	/** Determines whether there is Amend column */
	boolean hasAmendColumn() {
		return (columnNames.length == 3);
	}

	/**
	 * Returns a map, which contains a list of child items for each parent item
	 */
	static Map<Integer, ArrayList<Integer>> initChildNodes(
			Map<Integer, TreeSecurityItem> allItems) {

		/* The result map */
		Map<Integer, ArrayList<Integer>> childrenMap = new HashMap<Integer, ArrayList<Integer>>();

		/* All items in the returns map */
		Collection<TreeSecurityItem> items = allItems.values();

		/* Looping through items */
		ArrayList<Integer> childrenList;
		for (TreeSecurityItem item : items) {

			int parentId = item.getParentId();

			if (childrenMap.containsKey(parentId)) {
				/* This parentId already processed */
				childrenList = childrenMap.get(parentId);
			} else {
				/* This parentId is retrieved a first time */
				childrenList = new ArrayList<Integer>();
				childrenMap.put(parentId, childrenList);
			}

			/* The id of item is added to its parent list */
			childrenList.add(item.getId());
		}

		/* The result map */
		return childrenMap;
	}

	/** Returns a child of parent at index in the parent's child array */
	public Object getChild(Object parent, int index) {

		/* Retrieving the list of children */
		ArrayList<Integer> childList = null;
		if (parent == nodes) {
			/* The root node */
			childList = childNodes.get(ROOT_ID);
		} else {
			/* Ordinary node */
			TreeSecurityItem item = (TreeSecurityItem) parent;
			Integer parentId = item.getId();
			childList = childNodes.get(parentId);
		}

		/* Result */
		if (childList == null) {
			/* The given parent doesn't have such child */
			return null;
		} else {
			/* A child found */
			int childId = childList.get(index);
			return nodes.get(childId);
		}

	}

	/** Returns the number of children for a given parent */
	public int getChildCount(Object parent) {

		/* Retrieving the list of children */
		ArrayList<Integer> childList = null;
		if (parent == nodes) {
			/* The root node */
			childList = childNodes.get(ROOT_ID);
		} else {
			/* Ordinary node */
			TreeSecurityItem item = (TreeSecurityItem) parent;
			Integer parentId = item.getId();
			childList = childNodes.get(parentId);
		}

		/* Result */
		if (childList == null) {
			/* The given parent doesn't have children */
			return 0;
		} else {
			/* Children found */
			return childList.size();
		}
	}

	/** Returns the value to be displayed for node, at column number */
	public Object getValueAt(Object node, int column) {

		Object result = "";

		if (!(node instanceof TreeSecurityItem)) {
			/* Nothing to do */
			return result;
		}

		TreeSecurityItem item = (TreeSecurityItem) node;

		switch (column) {
		case 0: /* Text column */
			result = item.toString();
			break;
		case 1: /* Review column */
		case 2: /* Amend column */
			result = getCheckCellValue(item, column);
			break;
		}

		return result;
	}

	/** Sets column value of given node */
	public void setValueAt(Object value, Object node, int column) {

		if (!(node instanceof TreeSecurityItem) || !(value instanceof Boolean)
				|| (column < 1)) {
			/* Nothing to do */
			return;
		}

		TreeSecurityItem item = (TreeSecurityItem) node;

		if ((column == 1)) {
			/* Review */
			item.setReview((Boolean) value);
		} else if (column == 2) {
			/* Amend */
			item.setAmend((Boolean) value);
		}

		/*
		 * Updating tree nodes states. Changes of review and amend values can
		 * affect each other, that is why this should be done.
		 */
		setChildrenSelection(item.getId(), column, (Boolean) value);
		setParentSelection(item.getId(), column);
		refreshTreeNodes();

		/*
		 * Updating current node selection according to the selection of the children.
		 */
		setBranchItemSelection(item.getId());
	}

	/**
	 * Sets a parent selection of given item on a given column. The parent
	 * selection status is defined from the children status.
	 */
	private void setParentSelection(int itemId, int column) {

		TreeSecurityItem item = nodes.get(itemId);
		int parentId = item.getParentId();

		if (parentId == ROOT_ID) {
			return;
		}

		/* Children selection of a parent of a given item */
		SecurityItem.Status childrenSelection = getChildrenSelection(parentId, column);

		/* Setting selection */
		TreeSecurityItem parentItem = nodes.get(parentId);

		if (column == 1) {
			/* Review */
			parentItem.setReview(childrenSelection);
		} else if (column == 2) {
			/* Amend */
			parentItem.setAmend(childrenSelection);
		}

		/* Recursion: updating parent of parent */
		setParentSelection(parentItem.getId(), column);
	}

	/** Sets the children selection to a value selected on a given column */
	private void setChildrenSelection(int parentId, int column, boolean selected) {

		ArrayList<Integer> childList = childNodes.get(parentId);

		if (childList == null) {
			/* No children. Nothing to do */
			return;
		}

		/* Looping through the children */
		for (int childId : childList) {
			/* Updating the children of current child */
			setChildrenSelection(childId, column, selected);

			/* Updating the current child itself */
			TreeSecurityItem item = nodes.get(childId);

			if (column == 1
					&& (item.getReview() != SecurityItem.Status.YES_READONLY)) {
				/* Review column */
				item.setReview(selected);
			} else if (column == 2
					&& (item.getAmend() != SecurityItem.Status.YES_READONLY)) {
				/* Amend column */
				item.setAmend(selected);
			}
		}
	}

	/** Refreshes the tree nodes according to their values */
	private void refreshTreeNodes() {
		fireTreeNodesChanged(nodes, new Object[] { nodes }, null, null);
	}

	/**
	 * Returns a value of check cell for given node. The value can be true,
	 * false, YES_READONLY and etc. See SecurityItem.Status.
	 */
	private Object getCheckCellValue(TreeSecurityItem item, int column) {

		SecurityItem.Status status = SecurityItem.Status.NO;

		if (column == 1) {
			/* Review column */
			status = item.getReview();
		} else if (column == 2) {
			/* Amend column */
			status = item.getAmend();
		}

		/* The result */
		Object value = status;

		/*
		 * Boolean type value is necessary for CheckBoxRenderer. Without it will
		 * not work properly.
		 */
		if (status == SecurityItem.Status.YES) {
			value = true;
		} else if (status == SecurityItem.Status.NO) {
			value = false;
		}

		return value;
	}

	/** Determines whether a column of given node is editable */
	public boolean isCellEditable(Object node, int column) {

		if (!(node instanceof TreeSecurityItem)) {
			/* Nothing to do */
			return false;
		}

		TreeSecurityItem item = (TreeSecurityItem) node;
		SecurityItem.Status status = null;

		if (column == 1) {
			/* Review column */
			status = item.getReview();
		} else if (column == 2) {
			/* Amend column */
			status = item.getAmend();
		}

		/* Result */
		if (status == SecurityItem.Status.YES_READONLY) {
			/* Readonly */
			return false;
		} else {
			/* Editable */
			return true;
		}
	}

	/** Checks whether a given node is a leaf */
	public boolean isLeaf(Object node) {

		if (!(node instanceof TreeSecurityItem)) {
			/* Nothing to do */
			return false;
		}

		TreeSecurityItem item = (TreeSecurityItem) node;
		return item.isLeaf();
	}

	/** Returns the number of available columns */
	public int getColumnCount() {
		return columnNames.length;
	}

	/** Returns the given column name */
	public String getColumnName(int index) {
		return columnNames[index];
	}

	/** Returns the given column class */
	public Class getColumnClass(int index) {
		return columnTypes[index];
	}
}