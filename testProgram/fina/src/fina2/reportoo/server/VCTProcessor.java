package fina2.reportoo.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import fina2.metadata.MDTConstants;
import fina2.period.PeriodPK;
import fina2.reportoo.ReportConstants;
import fina2.ui.sheet.openoffice.OOIterator;

public class VCTProcessor {

    private static Logger log = Logger.getLogger(VCTProcessor.class);

    private List vctDataTable = null;
    private List vctColumnCodes = null;
    private int[] vctColumnTypes = null;
    private OOIterator vctIterator = null;

    private VCTProcessor() {
    }

    public static VCTProcessor newInstance(OOIterator vctIterator,
            Connection con) throws Exception {
        VCTProcessor vp = new VCTProcessor();
        ResultSet rs = vp.getRawVctData(vctIterator, con);
        vp.prepareVctDataTable(rs);
        if (vp.vctDataTable.size() > 0) {
            vp.vctColumnTypes = vp.getColTypes((List) vp.vctDataTable.get(0));
        }
        vp.vctIterator = vctIterator;
        return vp;
    }

    public double getVctFunctionResult(String node, String agregate,
            String function) {

        double retVal = Double.NaN;

        int func = AgregateFunction.getCode(function);
        int nodeColPos = vctColumnCodes.indexOf(node);
        if ((nodeColPos >= 0) && // the node code exists in VCT Data table and
                (func != AgregateFunction.UNK) && // the function is known and
                ((vctColumnTypes[nodeColPos] == MDTConstants.DATATYPE_NUMERIC) || // (type is int or
                (func == AgregateFunction.COUNT)) // funct is count)
        ) {
            agregate = agregate.trim();
            switch (func) {
            case AgregateFunction.AVG:
                retVal = calcVctAverage(agregate, nodeColPos);
                break;
            case AgregateFunction.SUM:
                retVal = calcVctSum(agregate, nodeColPos);
                break;
            case AgregateFunction.COUNT:
                retVal = calcVctCount(agregate);
                break;
            case AgregateFunction.MIN:
                retVal = calcVctMin(agregate, nodeColPos);
                break;
            case AgregateFunction.MAX:
                retVal = calcVctMax(agregate, nodeColPos);
                break;
            }
        }
        return retVal;
    }

    public List getVctIteratorValues() {

        Set set = new LinkedHashSet();
        int pos = vctColumnCodes.indexOf(vctIterator.getGroupCode().trim());
        if (pos >= 0) {
            for (Iterator iter = vctDataTable.iterator(); iter.hasNext();) {
                List row = (List) iter.next();
                set.add(row.get(pos));
            }
        }
        return new LinkedList(set);
    }

    private int[] getColTypes(List vctDataRow) {

        int[] retVal = new int[vctDataRow.size()];
        int curPos = 0;
        for (Iterator iter = vctDataRow.iterator(); iter.hasNext();) {
            Object item = (Object) iter.next();
            if (item instanceof String) {
                retVal[curPos++] = MDTConstants.DATATYPE_TEXT;
            } else {
                retVal[curPos++] = MDTConstants.DATATYPE_NUMERIC;
            }
        }
        return retVal;
    }

    private double calcVctAverage(String agregate, int nodeColPos) {

        int groupColPos = vctColumnCodes.indexOf(vctIterator.getGroupCode()
                .trim());
        int count = 0;
        double sum = 0;
        for (Iterator iter = vctDataTable.iterator(); iter.hasNext();) {
            List row = (List) iter.next();
            String groupFileldValue = row.get(groupColPos).toString().trim();
            if (agregate.equals(groupFileldValue)) {
                double field = ((Double) row.get(nodeColPos)).doubleValue();
                if (!Double.isNaN(field)) {
                    sum += field;
                    count++;
                }
            }
        }
        return (count == 0) ? 0 : sum / count;
    }

    private double calcVctSum(String agregate, int nodeColPos) {

        int groupColPos = vctColumnCodes.indexOf(vctIterator.getGroupCode()
                .trim());
        double sum = 0;
        for (Iterator iter = vctDataTable.iterator(); iter.hasNext();) {
            List row = (List) iter.next();
            String groupFileldValue = row.get(groupColPos).toString().trim();
            if (agregate.equals(groupFileldValue)) {
                double field = ((Double) row.get(nodeColPos)).doubleValue();
                if (!Double.isNaN(field)) {
                    sum += field;
                }
            }
        }
        return sum;
    }

    private double calcVctCount(String agregate) {

        int groupColPos = vctColumnCodes.indexOf(vctIterator.getGroupCode()
                .trim());
        int count = 0;
        for (Iterator iter = vctDataTable.iterator(); iter.hasNext();) {
            List row = (List) iter.next();
            String groupFileldValue = row.get(groupColPos).toString().trim();
            if (agregate.equals(groupFileldValue)) {
                count++;
            }
        }
        return count;
    }

    private double calcVctMax(String agregate, int nodeColPos) {

        int groupColPos = vctColumnCodes.indexOf(vctIterator.getGroupCode()
                .trim());
        double max = Double.MIN_VALUE;
        for (Iterator iter = vctDataTable.iterator(); iter.hasNext();) {
            List row = (List) iter.next();
            String groupFileldValue = row.get(groupColPos).toString().trim();
            if (agregate.equals(groupFileldValue)) {
                double field = ((Double) row.get(nodeColPos)).doubleValue();
                if (Double.isNaN(field) && max < field) {
                    max = field;
                }
            }
        }
        return max;
    }

    private double calcVctMin(String agregate, int nodeColPos) {

        int groupColPos = vctColumnCodes.indexOf(vctIterator.getGroupCode()
                .trim());
        double min = Double.MAX_VALUE;
        for (Iterator iter = vctDataTable.iterator(); iter.hasNext();) {
            List row = (List) iter.next();
            String groupFileldValue = row.get(groupColPos).toString().trim();
            if (agregate.equals(groupFileldValue)) {
                double field = ((Double) row.get(nodeColPos)).doubleValue();
                if (Double.isNaN(field) && min > field) {
                    min = field;
                }
            }
        }
        return min;
    }

    private ResultSet getRawVctData(OOIterator vctIterator, Connection con)
            throws SQLException {
        ResultSet retVal = null;

        // form banks sql list
        StringBuffer strBuf = new StringBuffer();
        for (Iterator iter = vctIterator.getAggregateValues().iterator(); iter
                .hasNext();) {
            strBuf.append('\'').append(iter.next()).append('\'');
            if (iter.hasNext()) {
                strBuf.append(", ");
            }
        }
        String bankSql = strBuf.toString();
        if (vctIterator.getAggregateType() != fina2.ui.sheet.Iterator.BANK_ITERATOR) { // is a list of bank groups
            bankSql = "select b.code from in_banks b where b.id in "
                    + "(select m.bankid from mm_bank_group m, in_bank_groups g "
                    + "where m.bankgroupid=g.id and rtrim(g.code) in ("
                    + bankSql + "))";
        }

        // form periods sql list
        strBuf = new StringBuffer();
        for (Iterator iter = vctIterator.getPeriodValues().iterator(); iter
                .hasNext();) {
            PeriodPK periodPk = (PeriodPK) iter.next();
            strBuf.append(periodPk.getId());
            if (iter.hasNext()) {
                strBuf.append(", ");
            }
        }
        String periodsSql = strBuf.toString();

        // form sql string
        StringBuffer sql = new StringBuffer();

        sql
                .append("select ri.value, ri.rowNumber, n.code, n.datatype ")
                .append(
                        "  from in_return_items ri, in_mdt_nodes n, in_returns r, in_schedules s, in_banks b ")
                .append("  where n.parentid=")
                .append(vctIterator.getTable().getNodeID())
                .append("        and ri.nodeid = n.id and ri.tableid = ")
                .append(vctIterator.getTable().getTableID())
                .append(
                        "        and ri.nodeid=n.id and ri.returnid=r.id and r.scheduleid=s.id ")
                .append("        and s.periodid in (")
                .append(periodsSql)
                .append(") ")
                .append("        and s.bankid=b.id and rtrim(b.code) in (")
                .append(bankSql)
                .append(") ")
                .append(versionFilter(vctIterator.getVersionCode()))
                .append(
                        "        order by ri.returnID, ri.rowNumber, n.sequence");
        // execute SQL - retrieve VCT data
        retVal = con.createStatement().executeQuery(sql.toString());
        return retVal;
    }

    private void prepareVctDataTable(ResultSet rs) throws SQLException {

        List table = new LinkedList();
        List row = null;
        List columnCodes = new ArrayList();

        int oldRowNum = Integer.MIN_VALUE;
        boolean isFirstRecord = true;
        while (rs.next()) {
            // read row number
            int rowNum = rs.getInt("ROWNUMBER");

            // if old row number is not equal to new one, this is new record element
            if (rowNum != oldRowNum) { // new Row Start
                if (row != null) { // this is not first row in virtual table
                    table.add(row); // add row to virtual table
                    isFirstRecord = false;
                }
                row = new ArrayList();
            }
            oldRowNum = rowNum;

            // add element to row
            row.add(convertToVctObject(rs.getString("VALUE"), rs
                    .getInt("DATATYPE")));

            // store column code information
            if (isFirstRecord) {
                columnCodes.add(rs.getString("CODE").trim());
            }
        }
        if (row != null) { //add last row to table
            table.add(row);
        }
        // Store Results
        vctDataTable = table;
        vctColumnCodes = columnCodes;

        // Log Results (debug mode only)
        logTableInfo(columnCodes, table);
    }

    private void logTableInfo(List columnCodes, List table) {
        if (log.isDebugEnabled()) {
            log.debug("Columns:");
            StringBuffer tmp = new StringBuffer();
            for (Iterator iter = columnCodes.iterator(); iter.hasNext();) {
                Object item = (Object) iter.next();
                tmp.append(item).append('\t');
            }
            log.debug(tmp.toString());
            for (Iterator rowIter = table.iterator(); rowIter.hasNext();) {
                tmp = new StringBuffer();
                List tableRow = (List) rowIter.next();
                for (Iterator iter = tableRow.iterator(); iter.hasNext();) {
                    Object item = (Object) iter.next();
                    tmp.append(item).append('\t');
                }
                log.debug(tmp.toString());
            }
        }
    }

    private Object convertToVctObject(String value, int type) {
        if (type == MDTConstants.DATATYPE_TEXT) {
            return value.replace('"', '\'');
        } else {
            double val = Double.NaN;
            try {
                val = Double.parseDouble(value);
            } catch (Exception ex) {
            }
            return new Double(val);
        }
    }

    private String versionFilter(String versionCode) {

        StringBuffer sql = new StringBuffer();

        sql.append(" AND ");

        if (versionCode == null
                || versionCode.equalsIgnoreCase(ReportConstants.LATEST_VERSION)) {
            sql
                    .append("ri.versionId = (select MAX(ri2.versionId) from IN_RETURNS r2, IN_RETURN_ITEMS ri2 where r2.id=r.id and r2.id=ri2.returnID) ");
        } else {
            sql
                    .append("ri.versionId = (select id from IN_RETURN_VERSIONS where code='"
                            + versionCode + "') ");
        }

        return sql.toString();
    }
}
