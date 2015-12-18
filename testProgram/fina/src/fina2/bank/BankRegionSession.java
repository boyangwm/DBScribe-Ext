/*
 * BankRegionSession.java
 *
 * Created on March 25, 2002, 12:39 AM
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

/**
 * 
 * @author vasop
 * @version
 */
@Deprecated
public interface BankRegionSession extends EJBObject {

	/** Creates new BankRegionSession */

	Collection getRegionRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException, EJBException;

	boolean cityExists(String cityName) throws RemoteException, EJBException;

	void addCityRegion(String city, String region, Handle languageHandle) throws RemoteException, EJBException;

	void updateCityRegion(int id, String city, String region) throws RemoteException, EJBException;

	int getId(String city,Handle languageHandle) throws RemoteException, EJBException;

	boolean removeCity(int id,Handle languageHandle) throws RemoteException,EJBException;
	
	boolean regionCityExists(String city,String region)throws RemoteException,EJBException;
}
