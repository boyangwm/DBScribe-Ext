/*
 * ScheduleHome.java
 *
 * Created on November 6, 2001, 4:03 PM
 */

package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

import fina2.FinaTypeException;
import fina2.bank.BankPK;
import fina2.period.PeriodPK;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface ScheduleHome extends EJBHome {

    Schedule create(BankPK bankPK, ReturnDefinitionPK definitionPK,
            PeriodPK periodPK) throws FinaTypeException, EJBException,
            CreateException, RemoteException;

    Schedule findByPrimaryKey(SchedulePK pk) throws EJBException,
            FinderException, RemoteException;

}
