package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

public interface BankCriterion extends EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException;

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;

    boolean isDefault() throws RemoteException, EJBException, FinaTypeException;

    void setDefault(boolean value) throws RemoteException, EJBException,
            FinaTypeException;
}
