/*
 * MenuHome.java
 *
 * Created on October 16, 2001, 1:54 PM
 */

package fina2.ui.menu;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface MenuHome extends EJBHome {

    Menu create(MenuPK parentPK) throws EJBException, CreateException,
            RemoteException;

    Menu findByPrimaryKey(MenuPK param0) throws EJBException, FinderException,
            RemoteException;

    Collection findByParent(MenuPK param0) throws EJBException,
            FinderException, RemoteException;
}
