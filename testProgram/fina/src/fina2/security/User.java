/*
 * User.java
 *
 * Created on October 16, 2001, 11:41 AM
 */
package fina2.security;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.bank.BankPK;

public interface User extends EJBObject {

    String getLogin() throws RemoteException, EJBException;

    void setLogin(String login) throws RemoteException, EJBException,
            FinaTypeException;

    void setPassword(String password) throws RemoteException, EJBException,
            FinaTypeException;

    String getName(Handle languageHandle) throws RemoteException, EJBException;

    void setName(Handle languageHandle, String name) throws RemoteException,
            EJBException;

    String getTitle(Handle languageHandle) throws RemoteException, EJBException;

    void setTitle(Handle languageHandle, String title) throws RemoteException,
            EJBException;

    String getPhone() throws RemoteException, EJBException;

    void setPhone(String phone) throws RemoteException, EJBException;

    String getEmail() throws RemoteException, EJBException;

    void setEmail(String email) throws RemoteException, EJBException;

    void setChangePassword(boolean changePassword) throws RemoteException,
            EJBException;

    boolean getChangePassword() throws RemoteException, EJBException;

    void setBlocked(boolean blocked) throws RemoteException, EJBException;

    boolean getBlocked() throws RemoteException, EJBException;

    boolean hasPermission(String permissionCode) throws RemoteException,
            EJBException;

    boolean canAccessBank(BankPK bankPK) throws RemoteException, EJBException;

    boolean hasPermissions(String[] permissions) throws RemoteException,
            EJBException;

    Collection getPermissions() throws RemoteException, EJBException;
}
