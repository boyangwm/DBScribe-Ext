/*
 * PeriodSession.java
 *
 * Created on October 30, 2001, 3:15 AM
 */
package fina2.period;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

/**
 *
 * @author  vasop
 */
public interface PeriodSession extends EJBObject {

    Collection getPeriodRows(Handle userHandle, Handle languageHandle)
            throws RemoteException, EJBException, FinaTypeException;

    Collection getPeriodRows(Handle userHandle, Handle languageHandle,
            String type, Date fromDate, Date toDate) throws RemoteException,
            EJBException, FinaTypeException;

    Collection getPeriodTypeRows(Handle userHandle, Handle languageHandle)
            throws RemoteException, EJBException, FinaTypeException;

    Collection getPeriodInsertRows(Handle languageHandle, String type,
            int frequencyType, java.util.Date fromDate, int startPeriodNumber,
            int numberOfPeriods) throws RemoteException, EJBException,
            FinaTypeException;

    Collection getPeriodInsertRows(Handle languageHandle, String type,
            java.util.Date fromDate, int startPeriodNumber,
            int numberOfPeriods, int daysInPeriods, int daysBetweenPeriods)
            throws RemoteException, EJBException, FinaTypeException;

    Collection savePeriods(Handle langHandle, PeriodTypePK typePK,
            Collection rows) throws RemoteException, EJBException,
            FinaTypeException;
}
