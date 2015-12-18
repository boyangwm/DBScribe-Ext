/*
 * Iterator.java
 *
 * Created on 19 Сентябрь 2002 г., 13:21
 */

package fina2.ui.sheet;

import fina2.returns.ReturnDefinitionTablePK;

public interface Iterator {

    public static final int ROW_ITERATOR = 1;
    public static final int COL_ITERATOR = 2;

    public static final int BANK_ITERATOR = 1;
    public static final int PEER_ITERATOR = 2;
    public static final int NODE_ITERATOR = 3;
    public static final int PERIOD_ITERATOR = 4;
    public static final int OFFSET_ITERATOR = 5;
    public static final int VCT_ITERATOR = 6;

    void create(Spreadsheet sheet, String name, int type, int orientation,
            int start, int end);

    void createInMemory(Spreadsheet sheet, String name, int type,
            int orientation, int start, int end);

    void setCoordinate(Spreadsheet sheet, int start, int end);

    String getName();

    void setName(String name);

    void remove();

    int getOrientation();

    void setOrientation(int orientation);

    int getType();

    void setType(int type);

    int getStart();

    int getEnd();

    void setValues(java.util.Collection values);

    java.util.Collection getValues();

    boolean isInIterator(int row, int col);

    Object getValue(int row, int col);

    void setSheet(Spreadsheet sheet);

    void setParameter(String parameterName);

    String getParameter();

    void setTable(ReturnDefinitionTablePK tablePK);

    ReturnDefinitionTablePK getTable();

    void setGroupCode(String groupCode);

    String getGroupCode();

    void setAggregateType(int aggregateType);

    int getAggregateType();

    void setAggregateValues(java.util.Collection aggregateValues);

    java.util.Collection getAggregateValues();

    void setAggregateParameter(String aggregateParameterName);

    String getAggergateParameter();

    void setPeriodValues(java.util.Collection periodValues);

    java.util.Collection getPeriodValues();

    void setPeriodParameter(String periodParameterName);

    String getPeriodParameter();

    void setVersionCode(String versionCode);

    String getVersionCode();
}
