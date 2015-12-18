/*
 * LicenceType.java
 *
 * Created on November 7, 2001, 5:43 PM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 *
 * @author  Administrator
 * @version 
 */
public interface LicenceType extends EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException;

}
