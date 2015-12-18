package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface AuthenticatedModeSessionHome extends EJBHome {

	AuthenticatedModeSession create() throws CreateException, EJBException,
			RemoteException;
}
