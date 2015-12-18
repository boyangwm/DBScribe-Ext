package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ReturnVersionSessionHome extends EJBHome {
    public ReturnVersionSession create() throws CreateException,
            RemoteException;
}
