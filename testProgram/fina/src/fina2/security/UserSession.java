package fina2.security;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.ui.tree.Node;

public interface UserSession extends EJBObject {

	//
	// General user management methods
	//
	UserPK findByLogin(String login) throws RemoteException;

	void blockUserByLogin(String login, UserPK pk) throws RemoteException;

	List<SecurityItem> getAllPermissions(Handle languageHandle) throws RemoteException;

	Map<Integer, TreeSecurityItem> getAllReturns(Handle languageHandle) throws RemoteException;

	Map<Integer, TreeSecurityItem> getAllReports(Handle languageHandle) throws RemoteException;

	List<SecurityItem> getAllReturnVersions(Handle languageHandle) throws RemoteException;

	List<SecurityItem> getAllRoles(Handle languageHandle) throws RemoteException;

	List<SecurityItem> getAllUsers(Handle languageHandle) throws RemoteException;

	Map<Integer, TreeSecurityItem> getAllBanks(Handle languageHandle) throws RemoteException;

	//
	// User methods
	//

	List<SecurityItem> getUserRoles(UserPK userPK, Handle languageHandle) throws RemoteException;

	void setUserRoles(UserPK userPK, Set<Integer> roles) throws RemoteException;

	Map<Integer, TreeSecurityItem> getUserBanks(UserPK userPK, Handle languageHandle) throws RemoteException;

	List<Integer> getUserBanksId(UserPK userPK) throws RemoteException;

	Map<Integer, TreeSecurityItem> getUserBanksOnly(UserPK userPK, Handle languageHandle) throws RemoteException;

	void setUserBanks(UserPK userPK, Set<Integer> banks) throws RemoteException;

	List<SecurityItem> getUserPermissions(UserPK userPK, Handle languageHandle) throws RemoteException;

	void setUserPermissions(UserPK userPK, Set<Integer> permissions) throws RemoteException;

	Map<Integer, TreeSecurityItem> getUserReturns(UserPK userPK, Handle languageHandle) throws RemoteException;

	void setUserReturns(UserPK userPK, Set<Integer> returns) throws RemoteException;

	Map<Integer, TreeSecurityItem> getUserReports(UserPK userPK, Handle languageHandle) throws RemoteException;

	void setUserReports(UserPK userPK, Set<Integer> reports) throws RemoteException;

	List<SecurityItem> getUserReturnVersions(UserPK userPK, Handle languageHandle) throws RemoteException;

	void setUserReturnVersions(UserPK userPK, List<SecurityItem> versions) throws RemoteException;

	//
	// Other
	//
	Node getTreeNodes(Handle userHandle, Handle languageHandle) throws RemoteException, EJBException, fina2.FinaTypeException;

	Hashtable getUserCanAmendBanks(UserPK userPK) throws RemoteException, EJBException;

	public Hashtable getUserCanAmendBankCodes(UserPK userPK) throws RemoteException, EJBException;

	Hashtable getUserCanReviewBanks(UserPK userPK) throws RemoteException, EJBException;

	void setUserBank(UserPK user, fina2.bank.BankPK bank) throws RemoteException, EJBException;

}
