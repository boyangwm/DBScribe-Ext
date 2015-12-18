/*
 * Period.java
 *
 * Created on October 31, 2001, 4:12 AM
 */

package fina2.period;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface Period extends EJBObject {

    PeriodTypePK getType() throws RemoteException, EJBException;

    void setType(PeriodTypePK typePK) throws RemoteException, EJBException;

    String getPeriodNumber() throws RemoteException, EJBException;

    void setPeriodNumber(String periodNumber) throws RemoteException,
            EJBException;

    String getFromDate(Handle languageHandle) throws RemoteException,
            EJBException;

    void setFromDate(Handle languageHandle, String fromDate)
            throws RemoteException, EJBException, java.text.ParseException;

    String getToDate(Handle languageHandle) throws RemoteException,
            EJBException;

    void setToDate(Handle languageHandle, String toDate)
            throws RemoteException, EJBException, java.text.ParseException,
            FinaTypeException;
}
