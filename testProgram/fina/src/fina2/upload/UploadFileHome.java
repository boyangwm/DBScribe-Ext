package fina2.upload;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface UploadFileHome extends EJBHome{
	
	public UploadFile create(byte [] file, String filename) throws RemoteException,EJBException, CreateException;
	public UploadFile findByPrimaryKey(UploadFilePK pk)throws RemoteException,EJBException,FinderException;
}
