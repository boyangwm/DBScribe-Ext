/*
 * ReturnDefinition.java
 *
 * Created on October 31, 2001, 11:26 AM
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
public interface ReturnDefinition extends EJBObject {

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;

    String getDescription(Handle languageHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle languageHandle, String desc)
            throws RemoteException, EJBException;

    ReturnTypePK getType() throws RemoteException, EJBException;

    void setType(ReturnTypePK param) throws RemoteException, EJBException,FinaTypeException;
}
