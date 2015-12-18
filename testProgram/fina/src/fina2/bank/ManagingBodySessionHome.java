/*
 * ManagingBodySessionHome.java
 *
 * Created on 1 јпрель 2002 г., 10:04
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

/**
 *
 * @author  Vasop
 * @version 
 */
public interface ManagingBodySessionHome extends EJBHome {

    ManagingBodySession create() throws CreateException, EJBException,
            RemoteException;

}
