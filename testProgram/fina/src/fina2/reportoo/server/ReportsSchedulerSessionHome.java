package fina2.reportoo.server;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ReportsSchedulerSessionHome extends EJBHome {
    public ReportsSchedulerSession create() throws CreateException,
            RemoteException;
}
