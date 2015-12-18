/*
 * Report.java
 *
 * Created on January 7, 2002, 3:32 PM
 */

package fina2.reportoo.server;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.i18n.LanguagePK;
import fina2.reportoo.ReportInfo;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public interface Report extends EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException,FinaTypeException;

    int getType() throws RemoteException, EJBException;

    ReportPK getParentPK() throws RemoteException, EJBException;

    void setType(int type) throws RemoteException, EJBException;

    ReportInfo getInfo() throws RemoteException, EJBException;

    void setInfo(ReportInfo info) throws RemoteException, EJBException;

    byte[] getTemplate() throws RemoteException, EJBException;

    byte[] getLangTemplate(LanguagePK langPK) throws RemoteException,
            EJBException;

    void setTemplate(LanguagePK langPK, byte[] temp) throws RemoteException,
            EJBException;
}
