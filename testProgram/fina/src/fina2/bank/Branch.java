/*
 * Branch.java
 *
 * Created on March 17, 2002, 3:22 AM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.regions.RegionStructureNodePK;

/**
 * 
 * @author vasop
 * @version
 */
public interface Branch extends EJBObject {

	RegionStructureNodePK getBankRegionPK() throws RemoteException,
			EJBException;

	void setBankRegionPK(RegionStructureNodePK pk) throws RemoteException, EJBException;

	String getName(Handle langHandle) throws RemoteException, EJBException;

	void setName(Handle langHandle, String paramName) throws RemoteException,
			EJBException;

	String getShortName(Handle langHandle) throws RemoteException, EJBException;

	void setShortName(Handle langHandle, String paramShortName)
			throws RemoteException, EJBException;

	String getAddress(Handle langHandle) throws RemoteException, EJBException;

	void setAddress(Handle langHandle, String paramAddress)
			throws RemoteException, EJBException;

	String getComments(Handle langHandle) throws RemoteException, EJBException;

	void setComments(Handle langHandle, String paramComments)
			throws RemoteException, EJBException;

	String getDate(Handle langHandle) throws RemoteException, EJBException;

	void setDate(Handle langHandle, String paramDate) throws RemoteException,
			EJBException, java.text.ParseException;

	String getDateOfChange(Handle langHandle) throws RemoteException,
			EJBException;

	void setDateOfChange(Handle langHandle, String paramDateOfChange)
			throws RemoteException, EJBException, java.text.ParseException;

	BankPK getBankPK() throws RemoteException, EJBException;

	void setBankPK(BankPK bankPK) throws RemoteException, EJBException;

}
