package fina2.update.gui;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface GuiUpdateSessionHome extends EJBHome {

	GuiUpdateSession create() throws CreateException, EJBException,
			RemoteException;
}
