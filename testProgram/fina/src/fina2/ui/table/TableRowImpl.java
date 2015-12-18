package fina2.ui.table;

import java.util.Hashtable;

public class TableRowImpl implements TableRow {

    private static final long serialVersionUID = 2425844507244004114L;

    private Object pk;
    private String[] cols;
    private int defaultCol = 0;
    private boolean blank;

    private Hashtable properties;

    /** Creates new TableRowImpl */
    public TableRowImpl(Object pk, int numOfCols) {
        this.pk = pk;
        cols = new String[numOfCols];
        properties = new Hashtable();
    }

    public void setDefaultCol(int defaultCol) {
        this.defaultCol = defaultCol;
    }

    public Object getPrimaryKey() {
        return pk;
    }

    public void setValue(int column, String value) {
        cols[column] = value;
    }

    public String getValue(int column) {
        return cols[column];
    }

    public int getColumnCount() {
        return cols.length;
    }

    public String toString() {
        return cols[defaultCol];
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof TableRowImpl) {
            return toString().equals(o.toString());
        }
        return false;
    }

    public void putProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public boolean isBlank() {
        return this.blank;
    }

    public void setBlank(boolean blank) {
        this.blank = blank;
    }
}
