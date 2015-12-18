/*
 * BankGroup.java
 *
 * Created on October 19, 2001, 7:31 PM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import fina2.FinaTypeException;

/**
 *
 * @author  Administrator
 * @version
 */
public interface BankGroup extends EJBObject {

    String getDescription(Handle langHandle) throws RemoteException,
            EJBException;

    void setDescription(Handle langHandle, String param)
            throws RemoteException, EJBException;

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;

    public void bankGroupAmend(Handle langHandle, String code, String desc)
            throws RemoteException, FinaTypeException;

    int getCriterionId() throws RemoteException, EJBException;

    void setCriterionId(int id) throws RemoteException, EJBException,
            FinaTypeException;
}
