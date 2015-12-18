/*
 * BankRegion.java
 *
 * Created on March 25, 2002, 4:57 AM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 *
 * @author  vasop
 * @version 
 */
@Deprecated
public interface BankRegion extends EJBObject {

    String getCity(Handle langHandle) throws RemoteException, EJBException;

    void setCity(Handle langHandle, String cityStr) throws RemoteException,
            EJBException;

    String getRegion(Handle langHandle) throws RemoteException, EJBException;

    void setRegion(Handle langHandle, String regionStr) throws RemoteException,
            EJBException;

}
