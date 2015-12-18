package fina2.ui.sheet.openoffice;

import java.util.Vector;

import fina2.reportoo.ReportConstants;
import fina2.returns.ReturnDefinitionTablePK;
import fina2.ui.sheet.Iterator;
import fina2.ui.sheet.Spreadsheet;

public class OOIterator implements Iterator, java.io.Serializable {

    private static final long serialVersionUID = 7119724937848337454L;

    private String name;

    private int type = 0;

    private int orientation;

    private String parameterName = null;

    private ReturnDefinitionTablePK tablePK = null;

    private String groupCode = null;

    private int aggregateType = 0;

    private Vector aggregateValues = new Vector();

    private String aggregateParameterName = null;

    private Vector periodValues = new Vector();

    private String periodParameterName = null;

    private String versionCode = ReportConstants.LATEST_VERSION;

    private transient OOSheet sheet;

    private Vector values = new Vector();

    /** Creates new OOIterator */
    public OOIterator() {
    }

    public int getStart() {
        if (orientation == ROW_ITERATOR) {
            return sheet.getDatabaseRangeValue(name, sheet.START_ROW);
        } else {
            return sheet.getDatabaseRangeValue(name, sheet.START_COL);
        }
    }

    public String getName() {
        return name;
    }

    public void remove() {
        if (orientation == ROW_ITERATOR)
            sheet.ungroup(orientation, getStart(), 0, getEnd(), 0);
        else
            sheet.ungroup(orientation, 0, getStart(), 0, getEnd());

        sheet.removeDatabaseRange(name);
    }

    public int getEnd() {
        if (orientation == ROW_ITERATOR) {
            return sheet.getDatabaseRangeValue(name, sheet.END_ROW);
        } else {
            return sheet.getDatabaseRangeValue(name, sheet.END_COL);
        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void create(Spreadsheet sheet, String name, int type,
            int orientation, int start, int end) {
        createInMemory(sheet, name, type, orientation, start, end);

        if (orientation == ROW_ITERATOR) {
            this.sheet.addDatabaseRange(name, start, 0, end, 0);
            this.sheet.group(sheet.ROWS, start, 0, end, 0);
        } else {
            this.sheet.addDatabaseRange(name, 0, start, 0, end);
            this.sheet.group(sheet.COLUMNS, 0, start, 0, end);
        }
    }

    public void setValues(java.util.Collection values) {
        this.values = new Vector(values);
    }

    public java.util.Collection getValues() {
        return values;
    }

    public int getType() {
        return type;
    }

    public void setName(String name) {
        if (sheet != null) {
            int start = getStart();
            int end = getEnd();
            sheet.removeDatabaseRange(this.name);
            if (orientation == ROW_ITERATOR)
                sheet.addDatabaseRange(name, start, 0, end, 0);
            else
                sheet.addDatabaseRange(name, 0, start, 0, end);
        }
        this.name = name;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setCoordinate(Spreadsheet sheet, int start, int end) {
        this.sheet = (OOSheet) sheet;

        if (orientation == ROW_ITERATOR) {
            this.sheet.addDatabaseRange(name, start, 0, end, 0);
            this.sheet.group(sheet.ROWS, start, 0, end, 0);
        } else {
            this.sheet.addDatabaseRange(name, 0, start, 0, end);
            this.sheet.group(sheet.COLUMNS, 0, start, 0, end);
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getValue(int row, int col) {
        if (!isInIterator(row, col))
            return null;

        int size = getEnd() - getStart() + 1; // number of cells in iterator
        int vsize = values.size(); // number of items in values
        int isize = size / vsize; // number of cells for each item

        int position = 0; // position inside iterator;
        if (orientation == ROW_ITERATOR)
            position = getStart() - row + isize;
        else
            position = getStart() - col + isize;

        int index = position / isize - 1; // index of value in Vector

        return values.get(index);
    }

    public void createInMemory(Spreadsheet sheet, String name, int type,
            int orientation, int start, int end) {
        this.sheet = (OOSheet) sheet;
        this.type = type;
        this.orientation = orientation;
        this.name = name;
    }

    public void setSheet(Spreadsheet sheet) {
        this.sheet = (OOSheet) sheet;
    }

    public boolean isInIterator(int row, int col) {
        if (orientation == ROW_ITERATOR) {
            if ((row >= getStart()) && (row <= getEnd()))
                return true;
            else
                return false;
        } else {
            if ((col >= getStart()) && (col <= getEnd()))
                return true;
            else
                return false;
        }
    }

    public void setParameter(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameter() {
        return parameterName;
    }

    public ReturnDefinitionTablePK getTable() {
        return tablePK;
    }

    public void setTable(ReturnDefinitionTablePK tablePK) {
        this.tablePK = tablePK;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public void setAggregateType(int aggregateType) {
        this.aggregateType = aggregateType;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public int getAggregateType() {
        return aggregateType;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setAggregateValues(java.util.Collection aggregateValues) {
        this.aggregateValues = new Vector(aggregateValues);
    }

    public void setAggregateParameter(String aggregateParameterName) {
        this.aggregateParameterName = aggregateParameterName;
    }

    public void setPeriodParameter(String periodParameterName) {
        this.periodParameterName = periodParameterName;
    }

    public void setPeriodValues(java.util.Collection periodValues) {
        this.periodValues = new Vector(periodValues);
    }

    public String getAggergateParameter() {
        return aggregateParameterName;
    }

    public String getPeriodParameter() {
        return periodParameterName;
    }

    public java.util.Collection getAggregateValues() {
        return aggregateValues;
    }

    public java.util.Collection getPeriodValues() {
        return periodValues;
    }
}
