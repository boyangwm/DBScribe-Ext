package fina2.regions;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.ui.tree.Node;

public interface RegionStructureSession extends EJBObject {
	public Node getTreeNodes(Handle userHandle, Handle languageHandle)
			throws EJBException, RemoteException, FinaTypeException;

	public boolean moveUp(RegionStructureNodePK pk) throws RemoteException,
			EJBException;

	public boolean moveDown(RegionStructureNodePK pk) throws RemoteException,
			EJBException;

	public String getNodePathLabel(RegionStructureNodePK pk,
			Handle languageHandle, StringBuffer sb) throws RemoteException,
			EJBException;

	public void setProperties(Map<Integer, String> map, Handle languageHandle)
			throws RemoteException, EJBException;

	public Map<Integer, String> getProperties(Handle languageHandle)
			throws RemoteException, EJBException;

}
