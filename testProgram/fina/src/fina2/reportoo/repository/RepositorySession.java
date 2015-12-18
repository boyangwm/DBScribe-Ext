/*
 * RepositorySession.java
 *
 * Created on 10 Сентябрь 2002 г., 0:03
 */

package fina2.reportoo.repository;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 *
 * @author  Shota Shalamberidze
 * @version
 */

public interface RepositorySession extends EJBObject {
    int createFormula(Formula formula, int parentID) throws RemoteException,
            EJBException;

    int createFolder(String name, int parentID) throws RemoteException,
            EJBException;

    String getFolderName(int id) throws RemoteException, EJBException;

    void setFolderName(int id, String name) throws RemoteException,
            EJBException;

    Formula findFormula(int id) throws RemoteException, EJBException;

    Collection getFormulas() throws RemoteException, EJBException;

    void deleteFormula(int id) throws RemoteException, EJBException;

    void deleteFolder(int id) throws RemoteException, EJBException;

    void updateFormula(int id, Formula formula) throws RemoteException,
            EJBException;

    Folder getRepositoryTree() throws RemoteException, EJBException;

    Collection getPeerValues(Collection values, Handle userHandle,
            Handle languageHandle) throws EJBException, RemoteException;

    Collection getBankValues(Collection values, Handle userHandle,
            Handle languageHandle) throws EJBException, RemoteException;

    Collection getPeriodValues(Collection values, Handle userHandle,
            Handle languageHandle) throws EJBException, RemoteException;

    Collection getNodeValues(Collection values, Handle userHandle,
            Handle languageHandle) throws EJBException, RemoteException;
}
