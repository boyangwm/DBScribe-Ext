package fina2.ui.table;

public interface TableRow extends java.io.Serializable {

    String getValue(int column);

    void setValue(int column, String value);

    Object getPrimaryKey();

    int getColumnCount();

    void setDefaultCol(int defaultCol);

    void putProperty(String key, Object value);

    Object getProperty(String key);

    boolean isBlank();

    void setBlank(boolean blank);
}
