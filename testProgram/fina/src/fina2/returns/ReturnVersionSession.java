package fina2.returns;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface ReturnVersionSession extends EJBObject {

    public Collection getReturnVersions(Handle langHandle, Handle userHandle)
            throws RemoteException, EJBException, FinaTypeException;

    public Collection getReturnVersions(Handle langHandle, Handle userHandle,
            boolean amendOnly) throws FinaTypeException, RemoteException;

    public ReturnVersion createReturnVersion(ReturnVersion rv, Handle langHandle)
            throws RemoteException, EJBException, FinaTypeException;

    public void updateReturnVersion(ReturnVersion rv, Handle langHandle)
            throws RemoteException, EJBException, FinaTypeException;

    public void deleteReturnVersion(ReturnVersion rv) throws RemoteException,
            EJBException;
}
