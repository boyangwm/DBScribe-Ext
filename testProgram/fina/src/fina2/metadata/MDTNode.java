/*
 * MDTNode.java
 *
 * Created on October 19, 2001, 10:57 AM
 */

package fina2.metadata;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface MDTNode extends EJBObject {
    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException;

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;

    int getType() throws RemoteException, EJBException;

    void setType(int param) throws RemoteException, EJBException;

    int getDataType() throws RemoteException, EJBException;

    void setDataType(int param) throws RemoteException, EJBException;

    String getEquation() throws RemoteException, EJBException;

    void setEquation(String param) throws RemoteException, EJBException;

    int getEvalMethod() throws RemoteException, EJBException;

    void setEvalMethod(int param) throws RemoteException, EJBException;

    void setDisabled() throws RemoteException, EJBException;

    int getRequired() throws RemoteException, EJBException;

    void setRequired(int param) throws RemoteException, EJBException;

    boolean getInUsed() throws RemoteException, EJBException;
    
    public void setForceRemove(boolean remove) throws RemoteException;
}
