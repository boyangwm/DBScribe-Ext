/*
 * Licence.java
 *
 * Created on November 7, 2001, 5:43 PM
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
public interface Licence extends EJBObject {

    LicenceTypePK getTypePK() throws RemoteException, EJBException;

    void setTypePK(LicenceTypePK pk) throws RemoteException, EJBException;

    String getCode() throws RemoteException, EJBException;

    void setCode(String paramCode) throws RemoteException, EJBException,
            FinaTypeException;

    String getDate(Handle langHandle) throws RemoteException, EJBException;

    void setDate(Handle langHandle, String paramDate) throws RemoteException,
            EJBException, java.text.ParseException;

    String getDateOfChange(Handle langHandle) throws RemoteException,
            EJBException;

    void setDateOfChange(Handle langHandle, String paramDateOfChange)
            throws RemoteException, EJBException, java.text.ParseException;

    String getReason(Handle langHandle) throws RemoteException, EJBException;

    void setReason(Handle langHandle, String paramReason)
            throws RemoteException, EJBException;

    int getOperational() throws RemoteException, EJBException;

    void setOperational(int paramOperational) throws RemoteException,
            EJBException;

    BankPK getBankPK() throws RemoteException, EJBException;

    void setBankPK(BankPK bankPK) throws RemoteException, EJBException;

}
