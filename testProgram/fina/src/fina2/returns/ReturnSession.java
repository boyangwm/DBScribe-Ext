package fina2.returns;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.metadata.MDTNodePK;
import fina2.ui.table.TableRow;

public interface ReturnSession extends EJBObject {

	void toAuditLog(String str, Handle userHandle, Handle languageHandle) throws RemoteException;

	Collection getReturnTypesRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException, EJBException;

	Collection getReturnDefinitionsRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException, EJBException;

	Collection getAllDefinitionTables(Handle languageHandle) throws RemoteException, EJBException;

	Collection getDefinitionTables(Handle languageHandle, ReturnDefinitionPK pk) throws RemoteException, EJBException;

	Collection getDefinitionTablesFormat(Handle languageHandle, ReturnDefinitionPK pk) throws RemoteException, EJBException;

	Collection getReturnTables(Handle languageHandle, ReturnPK pk) throws RemoteException, EJBException;

	byte[] getReturnDefinitionReviewFormat(ReturnDefinitionPK pk) throws FinaTypeException, RemoteException, EJBException;

	byte[] getReturnReviewFormat(ReturnPK pk) throws FinaTypeException, RemoteException, EJBException;

	void setReturnDefinitionReviewFormat(ReturnDefinitionPK pk, byte[] fv) throws FinaTypeException, RemoteException, EJBException;

	void setDefinitionTables(Handle languageHandle, ReturnDefinitionPK pk, Collection tables) throws FinaTypeException, RemoteException, EJBException;

	Collection getSchedulesRows(Handle userHandle, Handle languageHandle, String bankCode, String code, Date fromDate, Date toDate) throws fina2.FinaTypeException, RemoteException, EJBException;
	
	Collection getSchedulesRows(Handle userHandle, Handle languageHandle, String bankCode, String code, String type, String dodate, String returnDef, Date fromDate, Date toDate) throws fina2.FinaTypeException, RemoteException, EJBException;
	
	ReturnPK createReturn(Handle userHandle, Handle languageHandle, SchedulePK schedulePK, String versionCode) throws RemoteException, EJBException, FinaTypeException;

	void deleteReturn(ReturnPK returnPK, String versionCode) throws RemoteException, EJBException, FinaTypeException;

	Collection getReturnsRows(Handle userHandle, Handle languageHandle, Set<Integer> bankIdSet, String code, int status, Date fromDate, Date toDate, String type, String versionCode, int maxReturnsCount) throws fina2.FinaTypeException, RemoteException, EJBException;

	Collection<ValuesTableRow> getTableValuesRows(int langId ,String encoding, int returnPk, MDTNodePK nodePK, String versionCode,int quantity) throws RemoteException, EJBException;
	
	Collection getTableValuesRows(Handle languageHandle, ReturnPK pk, MDTNodePK nodePK, String versionCode) throws RemoteException, EJBException;

	Collection getReviewTableValuesRows(int langID,String encoding, int pk, MDTNodePK nodePK, String versionCode) throws RemoteException, EJBException;
	
	Collection getReviewTableValuesRows(Handle languageHandle, ReturnPK pk, MDTNodePK nodePK, String versionCode) throws RemoteException, EJBException;

	Collection getReviewTableFormatRows(Handle languageHandle, MDTNodePK nodePK) throws RemoteException, EJBException;

	void setTableValuesRows(ReturnPK pk, MDTNodePK nodePK, Collection rows, String versionCode) throws RemoteException, EJBException;

	void changeReturnStatus(Handle userHandle, Handle languageHandle, ReturnPK returnPK, int status, String note, String versionCode) throws RemoteException, EJBException;

	Collection getReturnStatuses(Handle languageHandle, ReturnPK pk, String versionCode) throws RemoteException, EJBException;

	Collection getAutoSchedulesRows(Handle userHandle, Handle languageHandle, Collection bankPK, Collection definitionPK, Collection periodPK) throws FinaTypeException, RemoteException, EJBException;

	Collection setAutoSchedulesRows(Handle languageHandle, Collection bankPK, Collection definitionPK, Collection periodPK, int doa) throws RemoteException, EJBException;

	Hashtable getReturnDependecies(ReturnDefinitionPK pk) throws RemoteException, EJBException;

	Collection getUsedInReturns(ReturnPK pk) throws RemoteException, EJBException;

	Collection getDependentReturns(ReturnPK pk) throws RemoteException, EJBException;

	Collection getReturnsStatuses(Handle languageHandle, String banksCode, String bankgroupsCode, String bankTypesCode, String returnsCode, String returnTypesCode, String versionCode, int pk) throws FinaTypeException, EJBException, RemoteException;

	void resetReturnVersion(ReturnPK returnPK, String versionCode) throws RemoteException, EJBException;

	public void copyPackage(Handle userHandle, String bankCode, Date endDate, String retunTypeCode, String sourVersionCode, String destVersionCode, String note) throws RemoteException, EJBException;

	public boolean returnExists(ReturnPK returnPK, String versionCode) throws RemoteException, EJBException;

	public boolean packageExists(String bankCode, Date endDate, String retunTypeCode, String versionCode) throws RemoteException, EJBException;

	public void updateReturnVersions(String returnIds) throws RemoteException, EJBException;

	public TableRow getReturnAdditionalData(ReturnPK returnPK, String versionCode, Handle languageHandle) throws RemoteException, EJBException;
}
