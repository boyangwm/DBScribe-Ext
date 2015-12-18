/*
 * ScheduleComparator.java
 *
 * Created on 26 январь 2004 г., 6:24
 */

package fina2.web;
import fina2.ui.table.TableRowImpl;
import java.util.Date;
/**
 *
 * @author  zuka
 */
public class ScheduleComparator implements java.util.Comparator {
    
    /** Creates a new instance of ScheduleComparator */
    public ScheduleComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        TableRowImpl row1=(TableRowImpl)o1;
        TableRowImpl row2=(TableRowImpl)o2;
        Date StartDate1=new Date(row1.getValue(1)); //Start date
        Date EndDate1=new Date(row1.getValue(2));   //End date
        String bank1=row1.getValue(0);              //Bank
        String code1=row1.getValue(4);              //Code
        Date StartDate2=new Date(row2.getValue(1)); //Start date
        Date EndDate2=new Date(row2.getValue(2));   //End date
        String bank2=row2.getValue(0);              //Bank
        String code2=row2.getValue(4);              //Code
        int result=StartDate1.compareTo(StartDate2);
        if(result==0){  //if start dates are equal
            result=EndDate1.compareTo(EndDate2);
            if(result==0){  //if end dates are equal
                result=bank1.compareToIgnoreCase(bank2);
                if(result==0){   //if banks are equal
                    result=code1.compareTo(code2);
                }
            }
        }
        return result;
    }
    
}
