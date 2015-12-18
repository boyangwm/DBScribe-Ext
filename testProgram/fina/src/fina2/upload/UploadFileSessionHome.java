package fina2.upload;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;


public interface UploadFileSessionHome extends EJBHome{

	UploadFileSession create() throws CreateException, EJBException,RemoteException;
}
