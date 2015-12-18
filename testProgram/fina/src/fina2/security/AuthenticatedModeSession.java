package fina2.security;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.ejb.EJBObject;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;

public interface AuthenticatedModeSession extends EJBObject {
	public TreeMap<String,String> loadProperties()throws RemoteException;
	
	public ArrayList<String> loadUsers() throws RemoteException;
	
	public boolean authenticateLdap(String username, String password) throws RemoteException,AuthenticationException,NamingException;
}
