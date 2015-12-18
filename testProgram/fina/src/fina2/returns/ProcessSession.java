package fina2.returns;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface ProcessSession extends EJBObject {

    String process(Handle userHandle,Handle langHandle, ReturnPK returnPK, boolean reprocess,
            String versionCode) throws FinaTypeException, RemoteException,
            EJBException;

    String importReturn(Handle userHandle, Handle languageHandle, byte[] xml,
            boolean forceReprocess, String versionCode) throws RemoteException,
            EJBException;

    Map<String,String> canProcess(Handle langHandle, SchedulePK schedulePK,
            String versionCode)
            throws RemoteException, EJBException;

    void prepareAutoProcess(long guid, int returnId, String versionCode)
            throws RemoteException, EJBException;

    Hashtable getUpdates(long guid, int returnId, long nodeId, int rowNumber,
            String value) throws RemoteException, EJBException;

    void insertRow(long guid, int tableID, int rowNumber, int returnID,
            Collection itemIds) throws RemoteException, EJBException;

    void removeRow(long guid, int tableID, int rowNumber)
            throws RemoteException, EJBException;

    void cleanup(long guid) throws RemoteException, EJBException;
}
