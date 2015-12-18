package fina2.update.gui;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import fina2.FinaGuiUpdateException;

public interface GuiUpdateSession extends EJBObject {
	public boolean canSynchronize(String oldGuiVersionDate)
			throws EJBException, RemoteException, FinaGuiUpdateException;

	public byte[] getGuiFile() throws EJBException, RemoteException,
			FinaGuiUpdateException;

	public Map<String, byte[]> getOtherUpdateFiles(String... files)
			throws EJBException, RemoteException, FinaGuiUpdateException;
}
