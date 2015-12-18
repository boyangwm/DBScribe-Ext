/*
 * MenuSessionHome.java
 *
 * Created on October 16, 2001, 2:31 PM
 */

package fina2.ui.menu;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface MenuSessionHome extends EJBHome {

    MenuSession create() throws CreateException, EJBException, RemoteException;

}
