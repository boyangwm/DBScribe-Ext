package fina2.regions;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface RegionStructureNodeHome extends EJBHome {

	RegionStructureNode create() throws EJBException, CreateException,
			RemoteException;

	RegionStructureNode findByPrimaryKey(RegionStructureNodePK pk)
			throws EJBException, FinderException, RemoteException;
}
