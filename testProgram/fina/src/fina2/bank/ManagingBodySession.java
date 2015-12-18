/*
 * ManagingBodySession.java
 *
 * Created on 1 јпрель 2002 г., 9:59
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface ManagingBodySession extends EJBObject {

    Collection getManagingBodyRows(Handle userHandle, Handle languageHandle)
            throws FinaTypeException, RemoteException, EJBException;
}
