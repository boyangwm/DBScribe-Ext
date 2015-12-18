/*
 * BranchManag.java
 *
 * Created on April 23, 2002, 7:59 PM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 *
 * @author  vasop
 * @version 
 */
public interface BranchManag extends EJBObject {

    ManagingBodyPK getManagingBodyPK() throws RemoteException, EJBException;

    void setManagingBodyPK(ManagingBodyPK managingBodyPK)
            throws RemoteException, EJBException;

    String getName(Handle langHandle) throws RemoteException, EJBException;

    void setName(Handle langHandle, String nameStr) throws RemoteException,
            EJBException;

    String getLastName(Handle langHandle) throws RemoteException, EJBException;

    void setLasttName(Handle langHandle, String lastNameStr)
            throws RemoteException, EJBException;

    String getPost(Handle langHandle) throws RemoteException, EJBException;

    void setPost(Handle langHandle, String postStr) throws RemoteException,
            EJBException;

    String getPhone() throws RemoteException, EJBException;

    void setPhone(String phone) throws RemoteException, EJBException;

    String getDate(Handle langHandle) throws RemoteException, EJBException;

    void setDate(Handle langHandle, String dateOfAppointment)
            throws RemoteException, EJBException, java.text.ParseException;

    String getDateOfChange(Handle langHandle) throws RemoteException,
            EJBException;

    void setDateOfChange(Handle langHandle, String dateOfChange)
            throws RemoteException, EJBException, java.text.ParseException;

    String getRegistration1(Handle langHandle) throws RemoteException,
            EJBException;

    void setRegistration1(Handle langHandle, String registration1Str)
            throws RemoteException, EJBException;

    String getRegistration2(Handle langHandle) throws RemoteException,
            EJBException;

    void setRegistration2(Handle langHandle, String registration2Str)
            throws RemoteException, EJBException;

    String getRegistration3(Handle langHandle) throws RemoteException,
            EJBException;

    void setRegistration3(Handle langHandle, String registration3Str)
            throws RemoteException, EJBException;

    String getComments1(Handle langHandle) throws RemoteException, EJBException;

    void setComments1(Handle langHandle, String comments1Str)
            throws RemoteException, EJBException;

    String getComments2(Handle langHandle) throws RemoteException, EJBException;

    void setComments2(Handle langHandle, String comments2Str)
            throws RemoteException, EJBException;

    BranchPK getBranchPK() throws RemoteException, EJBException;

    void setBranchPK(BranchPK branchPK) throws RemoteException, EJBException;

}
