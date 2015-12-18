/*
 * PeriodType.java
 *
 * Created on October 28, 2001, 11:43 PM
 */

package fina2.period;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

/**
 *
 * @author  vasop
 * @version 
 */
public interface PeriodType extends EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException;

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;

}
