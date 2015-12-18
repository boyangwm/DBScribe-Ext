/*
 * UserSessionHome.java
 *
 * Created on 28 ќкт€брь 2001 г., 11:01
 */

package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface UserSessionHome extends EJBHome {

    UserSession create() throws CreateException, EJBException, RemoteException;

}
