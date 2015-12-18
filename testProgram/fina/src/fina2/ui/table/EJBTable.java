/*
 * EJBTable.java
 *
 * Created on October 19, 2001, 1:43 PM
 */

package fina2.ui.table;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import fina2.Main;
import fina2.i18n.Language;
import fina2.metadata.MDTConstants;
import fina2.returns.SchedulePK;
import fina2.returns.ValuesTableRow;

/**
 * 
 * @author David Shalamberidze
 * @version
 */

@SuppressWarnings("serial")
public class EJBTable extends JTable implements java.awt.event.MouseListener, javax.swing.event.ListSelectionListener, TableColumnModelListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	@SuppressWarnings("rawtypes")
	private Vector selectionListeners;

	// private Vector columnsNames;
	// private Vector pks;
	@SuppressWarnings("unused")
	private JPopupMenu multiSelectMenu;
	private JPopupMenu popupMenu;

	private EJBTableModel model;
	private EJBTableCellEditor editor;

	@SuppressWarnings("unused")
	private int editable;

	private boolean allowSort = true;

	@SuppressWarnings("rawtypes")
	public EJBTable() {
		super();
		// tableRows = new Vector();
		// columnsNames = new Vector();
		// pks = new Vector();

		selectionListeners = new Vector();

		getSelectionModel().addListSelectionListener(this);

		setFont(ui.getFont());
		getTableHeader().setDefaultRenderer(new EJBTableHeaderRenderer());
		setDefaultRenderer(TableColumn.class, new EJBTableCellRenderer());
		getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
				// TableColumn col = getModel().get
				int colIndex = ((JTableHeader) mouseEvent.getSource()).columnAtPoint(mouseEvent.getPoint());
				String name = model.getColumnName(colIndex);
				model.sort(colIndex, name);
			}
		});

		addMouseListener(this);
		model = new EJBTableModel(this, new Vector(), new Vector(), null);
		model.setAllowSort(allowSort);
		setModel(model);

		getColumnModel().addColumnModelListener(this);

		javax.swing.JTextField textField = new javax.swing.JTextField() {
			public void setText(String text) {
				super.setText(text);
				if (text.length() > 0) {
					setSelectionStart(0);
					setSelectionEnd(text.length());
				}
			}
		};
		// textField.setFont(ui.getFont());
		editor = new EJBTableCellEditor(textField);
		editor.setClickCountToStart(1);

		setCellEditor(editor);
		setDefaultEditor(String.class, editor);
	}

	@SuppressWarnings("rawtypes")
	public TableRow findRow(Object pk) {
		for (Iterator iter = getRows().iterator(); iter.hasNext();) {
			TableRow r = (TableRow) iter.next();
			if (r.getPrimaryKey().equals(pk))
				return r;
		}
		return null;
	}

	public void sort(int colIndex) {
		model.sort(colIndex, null);
	}

	public void setValueAt(Object value, int r, int c) {
		model.setValueAt(value, r, c);
	}

	public void resizeAndRepaint() {
		super.resizeAndRepaint();
	}

	public void initTable(Collection columnsNames, Collection tableRows) {
		initTable(columnsNames, tableRows, null);
	}

	public void initTable(Collection columnsNames, Collection tableRows, Collection cellFormats) {

		clearSelection();
		model = new EJBTableModel(this, tableRows, columnsNames, cellFormats);
		model.setAllowSort(allowSort);
		setModel(model);

		if (getRowCount() > 0)
			setRowSelectionInterval(0, 0);

		if (columnsNames.size() > 0 && allowSort)
			model.sort(0, null);

		for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
			getColumnModel().getColumn(i).setCellEditor(editor);
		}

		if (getRowCount() > 0) {
			setRowSelectionInterval(0, 0);
		} else {
			clearSelection();
		}

		fireSelectionChangedEvent();

	}

	public void setAllowSort(boolean allowSort) {
		this.allowSort = allowSort;
		model.setAllowSort(allowSort);
	}

	public Collection getRows() {
		return model.getRows();
	}

	public TableRow getTableRow(int index) {
		return model.getTableRow(index);
	}

	public Hashtable getHashRows() {
		return model.getHashRows();
	}

	public void addRow(TableRow row) {
		model.addRow(row);

		clearSelection();
		changeSelection(model.getRowCount() - 1, 0, false, false);
		fireSelectionChangedEvent();
	}

	public void setEditable(int editable) {
		this.editable = editable;
		model.setEditable(editable);
	}

	public TableRow getSelectedTableRow() {
		if (getSelectedRow() == -1)
			return null;

		return model.getTableRow(getSelectedRow());
	}

	public Collection getSelectedTableRows() {
		Vector v = null;
		if (getSelectedRowCount() > 0) {
			v = new Vector();
			int[] sel = getSelectedRows();
			for (int i = 0; i < sel.length; i++) {

				v.add(model.getTableRow(sel[i]));
			}
		}
		return v;
	}

	public void removeRow(int index) {
		model.removeRow(index);
	}

	public void removeRows(int indexes[]) {
		model.removeRows(indexes);
	}

	public void updateRow(int index, TableRow row) {
		model.updateRow(index, row);
	}

	public void setMultiSelectMenu(JPopupMenu multiSelectMenu) {
		this.multiSelectMenu = multiSelectMenu;
	}

	public Collection getMultiSelectedPKs() {
		return null;
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	public Object getSelectedPK() {
		if (getSelectedRow() == -1)
			return null;

		return model.getPrimaryKey(getSelectedRow());
	}

	public Object[] getSelectedPks() {
		return model.getPrimaryKeys(getSelectedRows());
	}

	public void addEmptyRow() {
		model.addEmptyRow();
	}

	public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
	}

	public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
	}

	public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
	}

	public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
		if (mouseEvent.getModifiers() != mouseEvent.BUTTON3_MASK)
			return;
		if (popupMenu == null)
			return;

		int row = rowAtPoint(mouseEvent.getPoint());
		if (row != -1)
			setRowSelectionInterval(row, row);
		popupMenu.show(this, mouseEvent.getX(), mouseEvent.getY());
	}

	public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
	}

	public void addSelectionListener(ListSelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(ListSelectionListener listener) {
		selectionListeners.remove(listener);
	}

	public synchronized void fireSelectionChangedEvent() {
		if ((selectionListeners == null) || (selectionListeners.size() == 0))
			return;

		for (Iterator iter = selectionListeners.iterator(); iter.hasNext();) {
			ListSelectionListener listener = (ListSelectionListener) iter.next();
			listener.valueChanged(null);
		}
	}

	public synchronized void valueChanged(javax.swing.event.ListSelectionEvent evt) {
		super.valueChanged(evt);
		// //// if(1==1) return;
		fireSelectionChangedEvent();
		/*
		 * if ((getSelectedRow() != -1) && (getSelectedColumn() != -1)) { if
		 * (model.isCellEditable(getSelectedRow(), getSelectedColumn()))
		 * editCellAt(getSelectedRow(), getSelectedColumn()); else
		 * changeSelection(getSelectedRow(), getSelectedColumn(), true, true); }
		 */
	}

	public void columnSelectionChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
		super.columnSelectionChanged(listSelectionEvent);
		fireSelectionChangedEvent();
		if ((getSelectedRow() != -1) && (getSelectedColumn() != -1)) {
			if (model.isCellEditable(getSelectedRow(), getSelectedColumn()))
				editCellAt(getSelectedRow(), getSelectedColumn());
			else
				changeSelection(getSelectedRow(), getSelectedColumn(), true, true);
		}
	}

	public void columnRemoved(javax.swing.event.TableColumnModelEvent tableColumnModelEvent) {
		super.columnRemoved(tableColumnModelEvent);
	}

	public void columnMoved(javax.swing.event.TableColumnModelEvent tableColumnModelEvent) {
		super.columnMoved(tableColumnModelEvent);
	}

	public void columnMarginChanged(javax.swing.event.ChangeEvent changeEvent) {
		super.columnMarginChanged(changeEvent);
	}

	public void columnAdded(javax.swing.event.TableColumnModelEvent tableColumnModelEvent) {
		super.columnAdded(tableColumnModelEvent);
	}

	public String getColumnName(int c) {
		return model.getColumnName(c);
	}

}

class EJBTableCellEditor extends javax.swing.DefaultCellEditor {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	EJBTableCellEditor(javax.swing.JTextField textField) {
		super(textField);
	}

	public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {

		java.awt.Component editor = super.getTableCellEditorComponent(table, value, isSelected, r, c);

		editor.setFont(table.getFont());
		// editor.requestFocus();

		return editor;
	}
}

class EJBTableCellRenderer extends DefaultTableCellRenderer {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	EJBTableCellRenderer() {
		super();
	}

	public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		java.awt.Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		renderer.setFont(ui.getFont());
		if (row == -1) {
			((DefaultTableCellRenderer) renderer).setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
			((DefaultTableCellRenderer) renderer).setText(((EJBTableModel) table.getModel()).getColumnName(column));
		}
		if (isSelected) {
			renderer.setForeground(java.awt.Color.white);
			renderer.setBackground(java.awt.Color.blue.darker());
		}
		return renderer;
	}
}

class EJBTableHeaderRenderer extends DefaultTableCellRenderer {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	EJBTableHeaderRenderer() {
		super();
	}

	public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		java.awt.Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		renderer.setFont(ui.getFont());
		// if(row == -1) {
		// ((DefaultTableCellRenderer)renderer).setBorder(
		// new
		// javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED)
		// );
		// ((DefaultTableCellRenderer)renderer).setText(
		// ((EJBTableModel)table.getModel()).getColumnName(column)
		// );
		// }
		renderer.setForeground(java.awt.Color.white);
		renderer.setBackground(java.awt.Color.blue.darker());
		return renderer;
	}

}

class EJBTableModel extends AbstractTableModel {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private Vector rows;

	private Vector colNames;
	private EJBTable table;
	private Vector cellFormats;

	private int editable = 0;

	private boolean allowSort = true;

	@SuppressWarnings("rawtypes")
	EJBTableModel(EJBTable table, Collection rows, Collection colNames, Collection cellFormats) {
		super();
		this.table = table;

		this.rows = new Vector(rows);

		this.colNames = new Vector(colNames);

		if (cellFormats == null) {
			this.cellFormats = null;
		} else {
			this.cellFormats = new Vector(cellFormats);
		}
		addTableModelListener(this.table);
		return;

	}

	public void setAllowSort(boolean allowSort) {
		this.allowSort = allowSort;
	}

	public Collection getRows() {

		if (rows == null) {
			return new Vector();
		}

		return rows;
	}

	public Hashtable getHashRows() {
		Hashtable h = new Hashtable();
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			h.put(row.getPrimaryKey(), row);
		}
		return h;
	}

	public String getColumnName(int c) {
		return (String) colNames.elementAt(c);
	}

	private boolean reformat = true;

	private String defaultFormat = null;

	public java.lang.Object getValueAt(int r, int c) {

		if (defaultFormat == null) {
			try {
				Language lang = (Language) main.getLanguageHandle().getEJBObject();
				defaultFormat = lang.getNumberFormat();
			} catch (Exception e) {
			}
		}
		if (editable == 0) {
			TableRow row = (TableRow) rows.elementAt(r);
			if (row == null)
				return "";
			return row.getValue(c);
		}
		String format = defaultFormat;

		try {
			Vector rows = null;

			int cellFormatRowIndex = 0;
			if (editable == 1) {
				cellFormatRowIndex = r;
			}

			if (cellFormats.size() > cellFormatRowIndex) {
				rows = (Vector) cellFormats.get(cellFormatRowIndex);
			}

			if (rows != null && rows.size() > c) {
				format = (String) rows.get(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (r >= rows.size())
			return "";

		if (rows.get(r) instanceof fina2.returns.ValuesTableRow) {
			fina2.returns.ValuesTableRow row = (fina2.returns.ValuesTableRow) rows.get(r);
			if (row.getType(c) != MDTConstants.NODETYPE_INPUT)
				return "-";
		}

		TableRow row = (TableRow) rows.elementAt(r);
		if (row == null)
			return "";
		String value = row.getValue(c);
		if (reformat) {
			if (row instanceof fina2.returns.ValuesTableRow) {
				fina2.returns.ValuesTableRow v = (fina2.returns.ValuesTableRow) row;
				if (v.getDataType(c) == MDTConstants.DATATYPE_NUMERIC) {
					try {
						if (!value.trim().equals("") && !format.equals("")) {
							// String _format =
							DecimalFormat f = new DecimalFormat(format); // .replace(',','.'));

							double d = 0;
							try {
								d = f.parse(value.trim()).doubleValue();
							} catch (Exception ex) {
								try {
									DecimalFormat df = new DecimalFormat(defaultFormat); // .replace(',','.'));
									d = df.parse(value.trim()).doubleValue();
									/*
									 * d = LocaleUtil.string2number(
									 * main.getLanguageHandle(),
									 * ((String)value).trim() );
									 */
								} catch (Exception exx) {
								}
							}
							return f.format(d);
						}
					} catch (Exception e) {
						return "NaN";
					}
				}
			}
		}
		reformat = true;
		return value;
	}

	private java.awt.Frame getParentFrame(java.awt.Component c) {
		java.awt.Component parent = c.getParent();
		if (parent == null)
			return null;
		if (parent instanceof java.awt.Frame)
			return (java.awt.Frame) parent;
		return getParentFrame(parent);
	}

	public void setValueAt(Object value, int r, int c) {

		reformat = false;
		String format = "";
		try {
			Language lang = (Language) main.getLanguageHandle().getEJBObject();
			format = lang.getNumberFormat();
		} catch (Exception e) {
		}

		try {
			Vector row = null;
			if (editable == 1)
				row = (Vector) cellFormats.get(r);
			if (editable == 2)
				row = (Vector) cellFormats.get(0);
			format = (String) row.get(c);
		} catch (Exception e) {
		}

		if (rows.size() <= r)
			return;

		if (c >= colNames.size())
			return;
		double d = 0.0;
		TableRow row = (TableRow) rows.elementAt(r);
		if (row instanceof fina2.returns.ValuesTableRow) {
			fina2.returns.ValuesTableRow v = (fina2.returns.ValuesTableRow) row;
			if (v.getDataType(c) == MDTConstants.DATATYPE_NUMERIC) {
				try {
					if (!((String) value).trim().equals("") && !format.equals("")) {
						DecimalFormat f = new DecimalFormat(defaultFormat); // .replace(',','.'));

						d = f.parse(((String) value).trim()).doubleValue(); /*
																			 * LocaleUtil
																			 * .
																			 * string2number
																			 * (
																			 * main
																			 * .
																			 * getLanguageHandle
																			 * (
																			 * )
																			 * ,
																			 * (
																			 * (
																			 * String
																			 * )
																			 * value
																			 * )
																			 * .
																			 * trim
																			 * (
																			 * )
																			 * )
																			 * ;
																			 */
						value = f.format(d); /*
											 * LocaleUtil.number2string(
											 * main.getLanguageHandle(), d );
											 */
					}
				} catch (java.text.ParseException e) {
					Main.errorHandler(getParentFrame(table), Main.getString("fina2.title"), Main.getString("fina2.invalidNumberFormat"));
					return;
				} catch (Exception e) {
					Main.generalErrorHandler(e);
					return;
				}
			}
		}
		String oldValue = row.getValue(c);
		row.setValue(c, value.toString());

		fireTableChanged(new TableModelEvent(this, r, r, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));

		if ((row instanceof ValuesTableRow) && (r == rows.size() - 1) && (editable == 2)) {
			if (!oldValue.equals(value.toString())) {
				addEmptyRow();
			}
		}
	}

	public void addEmptyRow() {
		ValuesTableRow thisRow = (ValuesTableRow) rows.get(rows.size() - 1);
		ValuesTableRow newRow = new ValuesTableRow(0);
		for (int i = 0; i < thisRow.getColumnCount(); i++) {
			newRow.addColumn("", thisRow.getType(i), thisRow.getDataType(i), thisRow.getNodeID(i));
		}
		addRow(newRow);
	}

	public int getRowCount() {
		return rows.size();
	}

	public int getColumnCount() {
		return colNames.size();
	}

	public void addRow(TableRow row) {
		int index = rows.size();

		rows.add(row);

		// fireTableDataChanged();
		table.tableChanged(new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	public void removeRow(int index) {
		rows.removeElementAt(index);

		fireTableChanged(new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
		table.clearSelection();
		table.changeSelection(-1, 0, false, false);
		table.fireSelectionChangedEvent();
	}

	public void removeRows(int[] indexes) {
		for (int i = 0; i < indexes.length; i++) {
			rows.removeElementAt(indexes[i]);

			fireTableChanged(new TableModelEvent(this, indexes[i], indexes[i], TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
			table.clearSelection();
			table.changeSelection(-1, 0, false, false);
			table.fireSelectionChangedEvent();
		}
	}

	public void updateRow(int index, TableRow row) {
		rows.remove(index);
		rows.insertElementAt(row, index);

		fireTableChanged(new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

	public Object getPrimaryKey(int index) {
		TableRow row = (TableRow) rows.elementAt(index);
		return row.getPrimaryKey();
	}

	public Object[] getPrimaryKeys(int[] indexes) {
		Object[] rowsData = new Object[indexes.length];
		for (int i = 0; i < rowsData.length; i++) {
			rowsData[i] = (SchedulePK) getPrimaryKey(indexes[i]);
		}
		return rowsData;
	}

	public TableRow getTableRow(int index) {
		return (TableRow) rows.elementAt(index);
	}

	public void setEditable(int editable) {
		this.editable = editable;
	}

	public boolean isCellEditable(int row, int col) {
		if (rows.get(row) instanceof fina2.returns.ValuesTableRow) {
			fina2.returns.ValuesTableRow r = (fina2.returns.ValuesTableRow) rows.get(row);
			if (r.getType(col) != MDTConstants.NODETYPE_INPUT)
				return false;
		}
		switch (editable) {
		case 1:
			if (col == 0)
				return false;
			else
				return true;
		case 2:
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sort(int colIndex, String name) {
		if (!allowSort)
			return;

		if (rows == null)
			return;
		if (rows.size() <= 1)
			return;

		TableRow selRow = table.getSelectedTableRow();
		boolean isnumbercolumn = true;
		TreeMap set = null;
		String date = null;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {

			TableRow row = (TableRow) iter.next();
			row.setDefaultCol(colIndex);
			date = row.getValue(colIndex);
			break;
		}
		if (name != null && isHederDateName(name)) {

			if (isCorrectDate(date)) {
				set = new TreeMap(new StartEndDateComparator());
				isnumbercolumn = false;
			}
		} else if (name != null && isHederNumberName(name) && isnumbercolumn && isCorrectInteger(date)) {
			set = new TreeMap(new IntegerComparator());
		} else {
			set = new TreeMap();
		}
		for (Iterator iter = rows.iterator(); iter.hasNext();) {

			TableRow row = (TableRow) iter.next();
			row.setDefaultCol(colIndex);
			set.put(row.getValue(colIndex), row);
		}
		Vector oldRows = rows;
		rows = new Vector();
		int selIndex = 0;
		int i = 0;
		for (Iterator iter = set.values().iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			for (Iterator oldIter = oldRows.iterator(); oldIter.hasNext();) {
				TableRow r = (TableRow) oldIter.next();
				if (row.getValue(colIndex).equals(r.getValue(colIndex))) {
					if (r.getPrimaryKey().equals(selRow.getPrimaryKey()))
						selIndex = i;
					rows.add(r);
					i++;
				}
			}
		}
		// rows = new Vector(set.values());

		fireTableChanged(new TableModelEvent(this, 0, rows.size() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
		table.clearSelection();
		table.changeSelection(selIndex, 0, false, false);
		table.fireSelectionChangedEvent();

		/*
		 * for(int i=1; i<rows.size(); i++) { for(int j=rows.size()-1; j>=i;
		 * j--) { Object row = rows.elementAt(j); rows.remove(j); } }
		 */
	}

	private boolean isHederDateName(String name) {
		if (ui.getString("fina2.period.toDate").equals(name) || ui.getString("fina2.period.fromDate").equals(name)) {
			return true;
		}
		return false;
	}

	private boolean isHederNumberName(String name) {
		if (ui.getString("fina2.period.number").equals(name) || ui.getString("fina2.period.numberOfPeriods").equals(name) || ui.getString("fina2.returns.acceptableDelay").equals(name)) {
			return true;
		}
		return false;
	}

	private boolean isCorrectDate(String date) {
		boolean isdate = true;
		if (date == null)
			return false;
		SimpleDateFormat formatter = (SimpleDateFormat) ui.getDateFromat();
		try {
			formatter.parse(date.trim());
		} catch (ParseException pe) {
			isdate = false;
		}
		return isdate;
	}

	private boolean isCorrectInteger(String integer) {
		boolean isinteger = true;
		try {
			Integer.parseInt(integer);
		} catch (Exception ex) {
			isinteger = false;
		}
		return isinteger;
	}
}

class StartEndDateComparator implements Comparator<String> {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private SimpleDateFormat formatter;

	StartEndDateComparator() {
		formatter = (SimpleDateFormat) ui.getDateFromat();
	}

	@Override
	public int compare(String first, String second) {
		Date firstDate = null;
		Date secondDate = null;

		try {
			firstDate = (Date) formatter.parse(first);
			secondDate = (Date) formatter.parse(second);
		} catch (ParseException e) {
			Main.generalErrorHandler(e);
		}
		return firstDate.compareTo(secondDate);
	}

}

class IntegerComparator implements Comparator<String> {
	@Override
	public int compare(String first, String second) {
		Integer firstresult = Integer.parseInt(first);
		Integer secondresult = Integer.parseInt(second);
		return firstresult.compareTo(secondresult);
	}
}
