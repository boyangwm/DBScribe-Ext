/*
 * OOIterator.java
 *
 * Created on 19 Сентябрь 2002 г., 13:43
 */

package fina2.ui.sheet.openoffice;

import java.util.Vector;

import fina2.ui.sheet.Parameter;
import fina2.ui.sheet.Spreadsheet;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class OOParameter implements Parameter, java.io.Serializable {

    //private static final long serialVersionUID = 7119724937848337454L;
    private String name;
    private int type;
    //private int orientation;

    private transient OOSheet sheet;

    private Vector values = null;

    /** Creates new OOIterator */
    public OOParameter() {
    }

    /*public int getStart() {
        if(orientation == ROW_ITERATOR) {
            return sheet.getDatabaseRangeValue(name, sheet.START_ROW);
        } else {
            return sheet.getDatabaseRangeValue(name, sheet.START_COL);
        }
    }*/

    public String getName() {
        return name;
    }

    public void remove() {
    }

    /*public int getEnd() {
        if(orientation == ROW_ITERATOR) {
            return sheet.getDatabaseRangeValue(name, sheet.END_ROW);
        } else {
            return sheet.getDatabaseRangeValue(name, sheet.END_COL);
        }
    }*/

    /*public int getOrientation() {
        return orientation;
    }*/

    /*public void create(Spreadsheet sheet, String name, int type, int orientation, int start, int end) {
        createInMemory(sheet, name, type, orientation, start, end);
        
        if(orientation == ROW_ITERATOR) {
            this.sheet.addDatabaseRange(name, start, 0, end, 0);
            this.sheet.group(sheet.ROWS, start, 0, end , 0);
        } else {
            this.sheet.addDatabaseRange(name, 0, start, 0, end);
            this.sheet.group(sheet.COLUMNS, 0, start, 0, end);
        }
    }*/

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
        this.name = name;
    }

    /*public void setOrientation(int orientation) {
        this.orientation = orientation;
    }*/

    /*public void setCoordinate(Spreadsheet sheet, int start, int end) {
        this.sheet = (OOSheet)sheet;
        
        if(orientation == ROW_ITERATOR) {
            this.sheet.addDatabaseRange(name, start, 0, end, 0);
            this.sheet.group(sheet.ROWS, start, 0, end , 0);
        } else {
            this.sheet.addDatabaseRange(name, 0, start, 0, end);
            this.sheet.group(sheet.COLUMNS, 0, start, 0, end);
        }
    }*/

    public void setType(int type) {
        this.type = type;
    }

    /*
        public Object getValue(int row, int col) {
            if(!isInIterator(row, col))
                return null;
            
            int size = getEnd() - getStart() + 1; // number of cells in iterator
            int vsize = values.size(); // number of items in values
            int isize = size / vsize; // number of cells for each item
            
            int position = 0; // position inside iterator;
            if(orientation == ROW_ITERATOR)
                position = getStart() - row + isize;
            else
                position = getStart() - col + isize;
            
            int index = position / isize - 1; // index of value in Vector
            
            return values.get(index);
        }
    */
    /*    public void createInMemory(Spreadsheet sheet, String name, int type, int orientation, int start, int end) {
            this.sheet = (OOSheet)sheet;
            this.type = type;
            this.orientation = orientation;
            this.name = name;
        }    
    */
    public void setSheet(Spreadsheet sheet) {
        this.sheet = (OOSheet) sheet;
    }
    /*
        public boolean isInIterator(int row, int col) {
            if(orientation == ROW_ITERATOR) {
                if( (row >= getStart()) && (row <= getEnd()) )
                    return true;
                else
                    return false;
            } else {
                if( (col >= getStart()) && (col <= getEnd()) )
                    return true;
                else
                    return false;
            }
        }    
       
      */
}
