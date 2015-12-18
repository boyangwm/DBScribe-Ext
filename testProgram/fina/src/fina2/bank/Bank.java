/*
 * Bank.java
 *
 * Created on October 22, 2001, 8:07 PM
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
 * @author David Shalamberidze
 * @version
 */
public interface Bank extends EJBObject {

	String getCode() throws RemoteException, EJBException;

	void setCode(String param) throws RemoteException, EJBException,
			FinaTypeException;

	BankTypePK getTypePK() throws RemoteException, EJBException;

	void setTypePK(BankTypePK pk) throws RemoteException, EJBException;

	String getShortName(Handle langHandle) throws RemoteException, EJBException;

	void setShortName(Handle langHandle, String shortName)
			throws RemoteException, EJBException;

	String getName(Handle langHandle) throws RemoteException, EJBException;

	void setName(Handle langHandle, String name) throws RemoteException,
			EJBException;

	String getAddress(Handle langHandle) throws RemoteException, EJBException;

	void setAddress(Handle langHandle, String address) throws RemoteException,
			EJBException;

	String getPhone() throws RemoteException, EJBException;

	void setPhone(String phone) throws RemoteException, EJBException;

	String getFax() throws RemoteException, EJBException;

	void setFax(String fax) throws RemoteException, EJBException;

	String getEmail() throws RemoteException, EJBException;

	void setEmail(String email) throws RemoteException, EJBException;

	String getTelex() throws RemoteException, EJBException;

	void setTelex(String telex) throws RemoteException, EJBException;

	long getRegionId() throws RemoteException, EJBException;

	void setRegionId(long regionId) throws RemoteException, EJBException;

	String getSwiftCode() throws RemoteException, EJBException;

	void setSwiftCode(String swiftCode) throws RemoteException, EJBException;

	public Collection getBankGroupPKs() throws RemoteException, EJBException;

	public void setBankGroupPKs(Collection bankGroupPKs)
			throws RemoteException, EJBException;

}
