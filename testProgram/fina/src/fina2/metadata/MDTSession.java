/*
 * MDTSession.java
 *
 * Created on October 19, 2001, 12:37 PM
 */

package fina2.metadata;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.metadata.jaxb.MDTNodeData;
import fina2.ui.table.TableRow;
import fina2.ui.tree.Node;

public interface MDTSession extends EJBObject {

	void moveUp(MDTNodePK pk) throws RemoteException, EJBException;

	void moveDown(MDTNodePK pk) throws RemoteException, EJBException;

	void copyPaste(MDTNodePK pk, MDTNodePK newParentPk, boolean cut) throws RemoteException, EJBException;

	Node getTreeNodes(Handle userHandle, Handle languageHandle) throws RemoteException, EJBException, FinaTypeException;

	Collection getChildNodes(Handle userHandle, Handle languageHandle, MDTNodePK parentPK) throws RemoteException, EJBException, FinaTypeException;

	Collection getChNodes(MDTNodePK parentPK) throws RemoteException, EJBException;

	void setDependentNodes(MDTNodePK pk, Collection codes) throws RemoteException, EJBException;

	// Collection getDependentOnNodes(MDTNodePK pk) throws RemoteException,
	// EJBException;
	Collection getDependencies(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException;

	Collection getDependsOn(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException;

	Collection getUsedBy(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException;

	Collection getChildren(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException;

	Collection getDependendedReturnDefinition(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException;

	// Collection getUsedByNodes(MDTNodePK pk) throws RemoteException,
	// EJBException;

	Collection getComparisons(Handle userHandle, MDTNodePK pk) throws RemoteException, FinaTypeException, EJBException;

	void setComparisons(MDTNodePK pk, Collection rows) throws FinaTypeException, RemoteException, EJBException;

	void setComparison(TableRow row) throws RemoteException, EJBException;

	void removeComparison(TableRow row) throws RemoteException, EJBException;

	Collection getParentNodes(MDTNodePK childPK) throws RemoteException, FinaTypeException;

	int getNodeSequence(long nodeId) throws RemoteException;

	void setNodeSequence(long nodeId, int sequence) throws RemoteException;

	HashMap<String, String> getNodeCodeDescriptions() throws RemoteException;

	MDTNodeData getSelectedNodeData(MDTNodePK pk) throws RemoteException;

	List<MDTNodeData> getAllSubTreeWithParent(MDTNodePK pk) throws RemoteException;

	String exportMDT(List<MDTNodeData> data,Handle languageHandle) throws RemoteException;

	void importMDT(String cont, MDTNodePK parentPk,String encoding) throws RemoteException;

	boolean containsNode(List<MDTNodeData> nodes, String code) throws RemoteException;

	long getMaxNodeId() throws RemoteException;

	long getMaxSysStringId() throws RemoteException;

	long getMaxDepNodeId() throws RemoteException;

	long getMaxCompId() throws RemoteException;

	public long getMaxSequence(long id) throws RemoteException;

	long getLangId(Connection con, String code) throws RemoteException;
	
	boolean nodeExists(String code)throws RemoteException;
	
	boolean comparisonExists(long nodeId, String equation)throws RemoteException;
}
