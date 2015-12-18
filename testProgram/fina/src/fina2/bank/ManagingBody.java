/*
 * Branch.java
 *
 * Created on March 17, 2002, 3:22 AM
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
public interface ManagingBody extends EJBObject {

    String getManagingBody(Handle langHandle) throws RemoteException,
            EJBException;

    void setManagingBody(Handle langHandle, String managingBodyString)
            throws RemoteException, EJBException;

}
