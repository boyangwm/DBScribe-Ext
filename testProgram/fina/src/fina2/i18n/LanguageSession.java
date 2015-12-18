/*
 * LanguageSession.java
 *
 * Created on October 15, 2001, 6:43 PM
 */

package fina2.i18n;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

public interface LanguageSession extends EJBObject {

    public Collection getLanguagesRows(Handle userHandle, Handle languageHandle)
            throws RemoteException, EJBException;

    public Collection getLanguagesRowsEx(Handle userHandle,
            Handle languageHandle) throws EJBException, RemoteException;

    public Properties getLanguageBundle(Handle languageHandle)
            throws EJBException, RemoteException;

    public void setLanguageBundle(Handle languageHandle, Properties bundle)
            throws EJBException, RemoteException;
}
