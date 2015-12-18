package fina2.upload;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import fina2.FinaTypeException;
import fina2.i18n.Language;
import fina2.security.User;

public interface UploadFileSession extends EJBObject{
	
	public void upload(Map<String, byte[]> files) throws RemoteException, EJBException;
	
	public List<UploadedFileInfo> getUploadedFiles(String username, String bankCode, int status, Date from, Date to) throws RemoteException, EJBException;
	
	public void reject(int id) throws RemoteException, EJBException,FinaTypeException;
	
	public void remove(int id) throws RemoteException, EJBException;		
	
	public void importReturns(LinkedList<byte[]> xmls, User user, Language lang, int fileId) throws RemoteException, EJBException, FinaTypeException;
	
	public List<ImportedReturnInfo> getImportedReturns(int fileId) throws RemoteException, EJBException;
	
	public List<Language> getLanguages() throws RemoteException, EJBException;
	
	public Map<String, String> getMessageBundle(Language lang) throws RemoteException, EJBException;
	
	public List<String> getUsers() throws RemoteException,EJBException;
	
	public List<String> getBanks() throws RemoteException,EJBException;
	
	public void setStatus(int id,int status)throws RemoteException,EJBException;
}
