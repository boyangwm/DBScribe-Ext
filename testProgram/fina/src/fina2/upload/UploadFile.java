package fina2.upload;

import java.rmi.RemoteException;
import java.sql.Date;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

public interface UploadFile extends EJBObject{
	
	void setPk(UploadFilePK pk)throws RemoteException,EJBException;
	UploadFilePK getPk()throws RemoteException,EJBException;
	
	void setFile(byte [] file)throws RemoteException,EJBException;
	byte [] getFile()throws RemoteException,EJBException;
	
	void setBankCode(String bankCode)throws RemoteException,EJBException;
	String getBankCode()throws RemoteException,EJBException;
	
	void setFileName(String fileName)throws RemoteException,EJBException;
	String getFileName()throws RemoteException,EJBException;
	
	void setUploadTime(Date uploadTime)throws RemoteException,EJBException;
	Date getUploadTime()throws RemoteException,EJBException;
	
	void setUsername(String user)throws RemoteException,EJBException;
	String getUsername()throws RemoteException,EJBException;
	
	void setStatus(int status)throws RemoteException,EJBException;
	int getStatus()throws RemoteException,EJBException;
	
}
