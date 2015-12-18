/*
 * MenuSession.java
 *
 * Created on October 16, 2001, 2:31 PM
 */

package fina2.ui.menu;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.ui.tree.Node;

public interface MenuSession extends EJBObject {

    Node getUserMenuTree(Handle userHandle, Handle languageHandle)
            throws RemoteException, EJBException, FinaTypeException;

    Node getMenuTree(Handle userHandle, Handle languageHandle)
            throws RemoteException, EJBException, FinaTypeException;

    void moveUp(MenuPK pk) throws RemoteException, EJBException;

    void moveDown(MenuPK pk) throws RemoteException, EJBException;

    void sort(MenuPK pk) throws RemoteException, EJBException;

    void copyPaste(Collection pks, MenuPK newParent, boolean cut)
            throws RemoteException, EJBException;

}
