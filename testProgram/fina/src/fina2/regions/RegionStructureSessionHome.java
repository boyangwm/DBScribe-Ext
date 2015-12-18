package fina2.regions;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface RegionStructureSessionHome extends EJBHome {

	RegionStructureSession create() throws CreateException, EJBException,
			RemoteException;
}
