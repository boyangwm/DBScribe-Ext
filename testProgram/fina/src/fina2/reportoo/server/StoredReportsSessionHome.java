package fina2.reportoo.server;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface StoredReportsSessionHome extends EJBHome {

    public StoredReportsSession create() throws CreateException,
            RemoteException;
}
