/*
 * UserHome.java
 *
 * Created on October 16, 2001, 11:41 AM
 */

package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface UserHome extends EJBHome {

    User create() throws EJBException, CreateException, RemoteException;

    User findByPrimaryKey(UserPK param0) throws EJBException, FinderException,
            RemoteException;

    User findByLoginPassword(String login, String password)
            throws EJBException, RemoteException, FinderException;

    User findByLogin(String login)
     		throws EJBException,RemoteException,FinderException;
   
  
           
}
