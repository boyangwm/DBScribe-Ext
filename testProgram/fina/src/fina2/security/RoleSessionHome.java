package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * RoleSessionHome interface
 */
public interface RoleSessionHome extends EJBHome {

    RoleSession create() throws CreateException, RemoteException;

}
