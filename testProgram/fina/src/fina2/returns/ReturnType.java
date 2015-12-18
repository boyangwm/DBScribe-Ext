/*
 * ReturnType.java
 *
 * Created on October 31, 2001, 11:59 AM
 */

package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface ReturnType extends EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException;

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;
}
