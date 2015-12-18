/*
 * Menu.java
 *
 * Created on October 16, 2001, 1:54 PM
 */

package fina2.ui.menu;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

public interface Menu extends EJBObject {
    //EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String desc) throws RemoteException,
            EJBException;

    int getType() throws RemoteException, EJBException;

    void setType(int param) throws RemoteException, EJBException;

    String getActionKey() throws RemoteException, EJBException;

    void setActionKey(String param) throws RemoteException, EJBException;

    String getApplication() throws RemoteException, EJBException;

    void setApplication(String param) throws RemoteException, EJBException;

}
