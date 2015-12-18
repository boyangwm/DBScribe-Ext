/*
 * RoleHome.java
 *
 * Created on October 30, 2001, 2:49 PM
 */

package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 * Role home
 *
 * @author  David Shalamberidze
 */
public interface RoleHome extends EJBHome {

    Role create() throws EJBException, CreateException, RemoteException;

    Role findByPrimaryKey(RolePK param0) throws EJBException, FinderException,
            RemoteException;

}
