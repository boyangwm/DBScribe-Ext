package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface ImportManagerSessionHome extends EJBHome {

    ImportManagerSession create() throws CreateException, EJBException,
            RemoteException;
}
