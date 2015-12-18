/*
 * BankSession.java
 *
 * Created on October 19, 2001, 7:47 PM
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.ui.tree.Node;

public interface BankSession extends EJBObject {

	Collection getBanksRows(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException, EJBException;

	Collection getBankListNodes(Handle userHandle, Handle languageHandle)
			throws RemoteException, EJBException, FinaTypeException;

	Collection getRegionRows(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException, EJBException;

	Collection getBankTypesRows(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException, EJBException;

	Collection getBankGroupsRows(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException, EJBException;

	Collection getBankCriterionRows(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException, EJBException;

	Collection getLicenceTypesRows(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException, EJBException;

	Collection getLicencesNodes(Handle userHandle, Handle languageHandle,
			BankPK bankPK) throws FinaTypeException, RemoteException,
			EJBException;

	Collection getBranchNodes(Handle userHandle, Handle languageHandle,
			BankPK bankPK) throws FinaTypeException, RemoteException,
			EJBException;

	Collection getBankManagNodes(Handle userHandle, Handle languageHandle,
			BankPK bankPK) throws FinaTypeException, RemoteException,
			EJBException;

	Collection getBranchManagNodes(Handle userHandle, Handle languageHandle,
			BranchPK branchPK) throws FinaTypeException, RemoteException,
			EJBException;

	Collection getBankGroupNodes(Handle userHandle, Handle languageHandle,
			BankPK bankPK) throws RemoteException, FinaTypeException,
			EJBException;

	Collection getNotAssignedBankRows(Handle userHandle, Handle languageHandle,
			BankCriterionPK criterionPK) throws FinaTypeException,
			EJBException, RemoteException;

	Collection getBanks(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, EJBException, RemoteException;

	Collection loadBanks(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException;

	List<Node> getBankTypes(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, RemoteException;

	Integer getBankId(String bankCode) throws RemoteException;
}
