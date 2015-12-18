/*
 * Schedule.java
 *
 * Created on November 6, 2001, 4:02 PM
 */

package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import fina2.bank.BankPK;
import fina2.period.PeriodPK;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public interface Schedule extends EJBObject {

	BankPK getBankPK() throws RemoteException, EJBException;

	void setBankPK(BankPK bankPK) throws RemoteException, EJBException;

	ReturnDefinitionPK getReturnDefinitionPK() throws RemoteException, EJBException;

	void setReturnDefinitionPK(ReturnDefinitionPK returnDefinitionPK) throws RemoteException, EJBException;

	PeriodPK getPeriodPK() throws RemoteException, EJBException;

	void setPeriodPK(PeriodPK periodPK) throws RemoteException, EJBException;

	int getDelay() throws RemoteException, EJBException;

	void setDelay(int d) throws RemoteException, EJBException;
	
	boolean canDelete(int id) throws RemoteException, EJBException;
}
