/*
 * Role.java
 *
 * Created on October 30, 2001, 2:49 PM
 */

package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

/**
 * Role data interface
 *
 * @author  David Shalamberidze
 */
public interface Role extends EJBObject {

    /** Returns the role description */
    String getDescription(Handle languageHandle) throws RemoteException,
            EJBException;

    /** Sets the role description */
    void setDescription(Handle languageHandle, String desc)
            throws FinaTypeException, RemoteException, EJBException;

    /** Returns the role code */
    public String getCode() throws RemoteException;

    /** Sets the role code */
    public void setCode(String code) throws FinaTypeException, RemoteException;
}
