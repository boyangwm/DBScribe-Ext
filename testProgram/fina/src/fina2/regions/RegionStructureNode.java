package fina2.regions;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface RegionStructureNode extends EJBObject {

	public String getCode() throws RemoteException, EJBException;

	public void setCode(String param, boolean isAmend) throws RemoteException,
			EJBException, FinaTypeException;

	public String getDescription(Handle langHandleF) throws RemoteException,
			EJBException, FinaTypeException;

	public void setDescription(Handle langHandle, String description)
			throws RemoteException, EJBException, FinaTypeException;

	public long getParentId() throws RemoteException, EJBException,
			FinaTypeException;

	public void setParentId(long parentId) throws RemoteException,
			EJBException, FinaTypeException;

	public int getSequence() throws RemoteException, EJBException;

	public boolean isInUsed() throws RemoteException, EJBException;
}
