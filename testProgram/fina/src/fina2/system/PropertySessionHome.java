package fina2.system;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface PropertySessionHome extends EJBHome {

    PropertySession create() throws CreateException, EJBException,
            RemoteException;
}
