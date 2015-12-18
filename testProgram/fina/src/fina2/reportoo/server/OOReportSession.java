/*
 * OOReportSession.java
 *
 * Created on 5 ќкт€брь 2002 г., 18:02
 */

package fina2.reportoo.server;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.i18n.LanguagePK;
import fina2.reportoo.FinaFunction;
import fina2.reportoo.ReportInfo;
import fina2.ui.tree.Node;

public interface OOReportSession extends EJBObject {

	Collection getRootFolders(Handle langHandle) throws RemoteException, EJBException;

	Node getTreeNodes(Handle userHandle, Handle languageHandle) throws RemoteException, EJBException, FinaTypeException;

	public LinkedHashMap<ReportPK, ReportInfo> getInfos(Handle userHandle, ReportPK pk) throws RemoteException, EJBException;

	Hashtable getNames(Handle userHandle, Handle languageHandle, ReportPK pk) throws RemoteException, EJBException;

	void preCalculation(String reporId, LanguagePK langPK, ReportInfo reportInfo) throws EJBException, RemoteException;

	List calculateVCTValues(String reporId, Collection VCTIters) throws EJBException, RemoteException;

	FinaFunction[] calculate(String reporId, FinaFunction[] invFuncs) throws EJBException, RemoteException;;

	void postCalculation(String reporId) throws EJBException, RemoteException;

	public void setReportSequence(int reportId, int index) throws RemoteException;
}
