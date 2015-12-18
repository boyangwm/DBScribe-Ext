

package fina2.reportoo;

import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ReportInfo implements java.io.Serializable {

    private static final long serialVersionUID = -4251585888122638860L;

    public int header = 0;

    public int footer = 5000;

    public Hashtable iterators = new Hashtable();

    public Hashtable parameters = new Hashtable();

    public Object getIteratorValue(int r, int c) {
        for (java.util.Iterator iter = iterators.values().iterator(); iter
                .hasNext();) {
            fina2.ui.sheet.Iterator it = (fina2.ui.sheet.Iterator) iter.next();
            if (it.isInIterator(r, c)) {
                return it.getValue(r, c);
            }
        }
        return null;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ReportInfo))
            return false;
        ReportInfo info = (ReportInfo) o;
        if (iterators.size() != info.iterators.size())
            return false;
        if (parameters.size() != info.parameters.size())
            return false;
        for (Iterator iter = iterators.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            Object value = iterators.get(key);
            Object infoValue = info.iterators.get(key);
            if (infoValue == null)
                return false;
            if (!value.equals(infoValue))
                return false;
        }
        for (Iterator iter = parameters.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            Object value = parameters.get(key);
            Object infoValue = info.parameters.get(key);
            if (infoValue == null)
                return false;
            if (!value.equals(infoValue))
                return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = 0;
        for (Iterator iter = iterators.values().iterator(); iter.hasNext();) {
            fina2.ui.sheet.Iterator it = (fina2.ui.sheet.Iterator) iter.next();
            int type = it.getType();
            for (Iterator _iter = it.getValues().iterator(); _iter.hasNext();) {
                Object value = _iter.next();
                hashCode += type * 10000 + value.hashCode();
            }
        }
        for (Iterator iter = parameters.values().iterator(); iter.hasNext();) {
            fina2.ui.sheet.Parameter param = (fina2.ui.sheet.Parameter) iter
                    .next();
            int type = param.getType();
            for (Iterator _iter = param.getValues().iterator(); _iter.hasNext();) {
                Object value = _iter.next();
                hashCode += type * 10000 + value.hashCode();
            }
        }
        return hashCode;
    }
}
