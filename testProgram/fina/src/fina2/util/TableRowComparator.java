package fina2.util;

import java.util.Comparator;

import fina2.ui.table.TableRow;

/**
 * Comparator class allowing to compare two TableRow objects based on field set
 * passed to constructor.
 */
public class TableRowComparator implements Comparator {
    private int[] fieldOrder;

    /**
     * Creates comparator object.
     * @param fieldOrder int[] - The integer array containing indexes of TableRow's
     * fields. The field number 0 compares primary keys of two TableRow objects,
     * 1 will compare getValue(0), 2 - getValue(1) and so on.
     * For example passing array with values (2, 0, 1) will make following comparations:
     * compare obj1.getValue(1) with obj2.getValue(1) and return result, if they are equal then
     * compare obj1.getPrimaryKey() with obj2.getPrimaryKey() and return result, if they are equal then
     * compare obj1.getValue(0) with obj2.getValue(0) and return result, if they are equal then
     * return 0 - this objects are equal.
     * NOTE: In case 0 index is used (primary key comparation), obj.getPrimaryKey() should return
     * object that implements Comparable interface, otherwise exceptoion will be thrown,
     * i.e. in order to compare objects by primary keys, primary key object should
     * implement Comparable interface.
     *
     */
    public TableRowComparator(int[] fieldOrder) {
        this.fieldOrder = fieldOrder;
    }

    public int compare(Object o1, Object o2) {
        int retVal = 0;
        TableRow tr1 = (TableRow) o1;
        TableRow tr2 = (TableRow) o2;
        for (int i = 0; i < fieldOrder.length; i++) {
            int fieldNumber = fieldOrder[i];
            if (fieldNumber == 0) {
                retVal = ((Comparable) tr1.getPrimaryKey())
                        .compareTo((Comparable) tr2.getPrimaryKey());

            } else {
                retVal = tr1.getValue(fieldNumber - 1).compareTo(
                        tr2.getValue(fieldNumber - 1));
            }
            if (retVal != 0) {
                break;
            }
        }
        return retVal;
    }
}
