package fina2.security.users;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import fina2.Main;
import fina2.security.SecurityItem;

/**
 * AbstractTableView
 */
@SuppressWarnings("serial")
public abstract class AbstractTableView<K> extends AbstractView<K> {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	/** Creates the instance of the class */
	protected AbstractTableView(ModeType modeType, K key) {
		super(modeType, key);
	}

	/** Inits the components */
	protected final void initTable(TableViewModel model) {

		JTable table = createTable(model);
		table.setFont(ui.getFont());
		table.getTableHeader().setFont(ui.getFont());

		/* For scrolling the table is contained inside of scroll pane */
		JScrollPane scrollPane = initScrollPane(table);
		scrollPane.setFont(ui.getFont());
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	/** Creates the table */
	private JTable createTable(TableViewModel model) {

		JTable table = new JTable(model);
		table.setFont(ui.getFont());
		table.getTableHeader().setFont(ui.getFont());

		/* For single row selection */
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(fina2.Main.main.ui.getFont());

		/* Review column */
		CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer();
		table.getColumnModel().getColumn(1).setCellRenderer(checkBoxRenderer);

		if (model.hasAmendColumn()) {
			/* There is Amend column. Setting Text and Amend columns */
			table.getColumnModel().getColumn(0).setPreferredWidth(300);
			table.getColumnModel().getColumn(2)
					.setCellRenderer(checkBoxRenderer);
		} else {
			/* There is no Amend column. Setting Text column */
			table.getColumnModel().getColumn(0).setPreferredWidth(400);
		}

		/* The created table */
		return table;
	}

	/** Creates the scroll pane */
	private JScrollPane initScrollPane(JTable table) {

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setFont(ui.getFont());
		/* Making the space around */
		Border border = scrollPane.getBorder();
		Border margin = new EmptyBorder(12, 12, 12, 12);
		scrollPane.setBorder(new CompoundBorder(margin, border));

		return scrollPane;
	}
}

/**
 * The check box renderer with disabled option
 */
class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

	/** Constructor */
	CheckBoxRenderer() {
		setHorizontalAlignment(CENTER);
	}

	/** Returns check box for rendering */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// Setting background color
		Color color = (isSelected) ? table.getSelectionBackground() : table
				.getBackground();
		setBackground(color);

		// Default state
		setSelected(false);
		setEnabled(true);

		// Setting simple state
		if (value instanceof Boolean) {
			boolean checked = (Boolean) value;
			setSelected(checked);
			setEnabled(true);
		}

		// Setting extended state
		if (value instanceof SecurityItem.Status) {
			// Extended check box status. See SecurityItem.Status.
			SecurityItem.Status status = (SecurityItem.Status) value;

			switch (status) {
			case YES:

				// true, checked
				setSelected(true);
				break;
			case NO:

				// false, unchecked
				// Nothing to do: default state is enough
				break;
			case YES_READONLY:

				// checked, read only
				setSelected(true);
				setEnabled(false);
				break;
			case PARTIAL:

				// checked partially
				setEnabled(false);
				break;
			}
		}

		/* The result: instance of this checkbox */
		return this;
	}
}

/**
 * Table model for view
 */
class TableViewModel extends AbstractTableModel {

	/** Column names: id, permissions, checked */
	private String[] columnNames = null;

	/** Column types */
	private Class[] columnTypes = null;

	/** The data of this model */
	private List<SecurityItem> modelData = null;

	/** Creates the instance of the class */
	TableViewModel(List<SecurityItem> modelData, String textColumn,
			boolean hasAmendColumn) {

		this.modelData = modelData;

		initColumns(textColumn, hasAmendColumn);
	}

	/** Inits columns data */
	private void initColumns(String textColumn, boolean hasAmendColumn) {

		if (hasAmendColumn) {
			/* There is "amend" column */

			columnNames = new String[] { textColumn,
					Main.getString("fina2.review"),
					Main.getString("fina2.amend") };

			columnTypes = new Class[] { String.class, Boolean.class,
					Boolean.class };

		} else {
			/* There is no "amend" column */
			columnNames = new String[] { textColumn, "" };
			columnTypes = new Class[] { String.class, Boolean.class };
		}
	}

	/** Returns the class of column type */
	public Class getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

	/** Returns column name */
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	/** Returns column count */
	public int getColumnCount() {
		return columnNames.length;
	}

	/** Returns row count */
	public int getRowCount() {
		return modelData.size();
	}

	/** Determines whether there is Amend column */
	boolean hasAmendColumn() {
		return (columnNames.length == 3);
	}

	/** Returns true, if a cell is editable */
	public boolean isCellEditable(int row, int column) {

		/* Only the "review" and "amend" columns are editable */
		if (column == 0) {
			return false;
		}

		SecurityItem item = modelData.get(row);
		SecurityItem.Status status = null;

		if (column == 1) {
			/* Review column */
			status = item.getReview();
		} else if (column == 2) {
			/* Amend column */
			status = item.getAmend();
		}

		if (status == SecurityItem.Status.YES_READONLY) {
			/* Readonly */
			return false;
		} else {
			/* Editable */
			return true;
		}
	}

	/** Returns a value of cell */
	public Object getValueAt(int row, int column) {

		SecurityItem item = modelData.get(row);
		Object value = null;

		switch (column) {
		case 0: /* Text column */
			value = item.getText();
			break;
		case 1: /* Review column */
		case 2: /* Amend column */
			value = getCheckCellValue(row, column);
			break;
		}

		return value;
	}

	/**
	 * Returns a value of check cell. The value can be true, false, YES_READONLY
	 * and etc.
	 * 
	 * @see SecurityItem.Status
	 */
	private Object getCheckCellValue(int row, int column) {

		SecurityItem item = modelData.get(row);
		SecurityItem.Status status = null;

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

	/** Sets a cell value */
	public void setValueAt(Object value, int row, int column) {

		/* Only the "review" and "amend" columns are editable */
		if (column == 0) {
			return;
		}

		/* Setting item value */
		SecurityItem item = modelData.get(row);
		boolean state = (Boolean) value;

		if ((column == 1)) {
			item.setReview(state);
		} else if (column == 2) {
			item.setAmend(state);
		}

		/*
		 * Updating table current row, because changes of review and amend
		 * values can affect each other.
		 */
		fireTableRowsUpdated(row, row);
	}
}
