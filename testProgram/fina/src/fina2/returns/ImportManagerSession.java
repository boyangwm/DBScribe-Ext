package fina2.returns;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

public interface ImportManagerSession extends EJBObject {

    public Collection getImportedDocuments(Handle userHandle,
            Handle languageHandle, Set<Integer> bankIdSet, String code,
            int status, Date fromDate, Date toDate, Date importedAfter,
            String userId, String versionCode, int maxReturnsCount,
            boolean isFISelected)
            throws RemoteException;

    public Collection getImporterUsers(Handle userHandle, Handle languageHandle)
            throws RemoteException;

    public void deleteUploadedDocuments(Collection docIds)
            throws RemoteException;

    public Date uploadImportedDocuments(Handle userHandle,
            Handle languageHandle, LinkedList<byte[]> xmls) throws RemoteException;
    
    public List<Integer> importedUploadDocuments(Handle userHandle, Handle languageHandle, 
    		LinkedList<byte[]> xmls) throws RemoteException;

    public void resetStatuses() throws RemoteException;

    public LinkedList<ImportedReturn> getReturnsToImport() throws RemoteException;

    public void started(int id) throws RemoteException;

    public void rejected(int id, String message) throws RemoteException;

    public void queued(int id, String message) throws RemoteException;

    public void imported(int id, String message) throws RemoteException;
    
    public void declined(int id, String message) throws RemoteException;
    
    public void errors(int id, String message) throws RemoteException;
}
