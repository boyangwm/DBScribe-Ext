package fina2.returns;

import java.io.Serializable;

import fina2.ui.table.TableRow;

public class ValuesTableRow implements TableRow, Serializable {

	
	
	private int id;
	private String[] values;
	private String[] codes;
	private int[] dataTypes;
	private int[] types;
	private long[] nodes;
	private int size;
	private boolean blank;
	private int length = 256;

	public ValuesTableRow(int id) {
		this.id = id;
		size = 0;
		values = new String[this.length];
		codes = new String[this.length];
		dataTypes = new int[this.length];
		types = new int[this.length];
		nodes = new long[this.length];
	}

	public ValuesTableRow(int id, int length) {
		this.id = id;
		size = 0;
		this.length = (length != 0) ? length : 256;
		values = new String[this.length];
		codes = new String[this.length];
		dataTypes = new int[this.length];
		types = new int[this.length];
		nodes = new long[this.length];
	}

	public Object getPrimaryKey() {
		return null;
	}

	public int getId() {
		return id;
	}

	public void setDefaultCol(int defaultCol) {
	}

	public void setValue(int column, String value) {
		if (column < size) {
			values[column] = value;
		}
	}

	public String getValue(int column) {
		return values[column];
	}

	public void setCode(int column, String code) {
		if (column < size) {
			codes[column] = code;
		}
	}

	public String getCode(int column) {
		return codes[column];
	}

	public int getColumnCount() {
		return size;
	}

	public long getNodeID(int column) {
		return nodes[column];
	}

	public int getType(int column) {
		return types[column];
	}

	public int getDataType(int column) {
		return dataTypes[column];
	}

	public void addColumn(String value, int type, int dataType, long nodeID, String code) {
		if (size < this.length) {
			values[size] = value;
			types[size] = type;
			dataTypes[size] = dataType;
			nodes[size] = nodeID;
			codes[size] = code;
			size++;
		}
	}

	public void addColumn(String value, int type, int dataType, long nodeID) {
		if (size < this.length) {
			values[size] = value;
			types[size] = type;
			dataTypes[size] = dataType;
			nodes[size] = nodeID;
			codes[size] = "code";
			size++;
		}
	}

	public boolean equals(Object o) {
		if (o instanceof ValuesTableRow) {
			ValuesTableRow other = (ValuesTableRow) o;
			return id == other.getId();
		} else
			return false;
	}

	public Object getProperty(String key) {
		return null;
	}

	public void putProperty(String key, Object value) {
	}

	public boolean isBlank() {
		return this.blank;
	}

	public void setBlank(boolean blank) {
		this.blank = blank;
	}
}
