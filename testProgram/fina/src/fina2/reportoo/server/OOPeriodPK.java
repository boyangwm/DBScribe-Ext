package fina2.reportoo.server;

import fina2.period.PeriodPK;

public class OOPeriodPK extends PeriodPK {

    private java.util.Date fromDate;
    private java.util.Date toDate;

    private int number;
    private String typeCode;
    private String typeName;

    OOPeriodPK(int id) {
        super(id);
    }

    public void setFromDate(java.util.Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(java.util.Date toDate) {
        this.toDate = toDate;
    }

    public java.util.Date getFromDate() {
        return fromDate;
    }

    public java.util.Date getToDate() {
        return toDate;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
