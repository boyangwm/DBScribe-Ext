/*
 * PeriodTypeHome.java
 *
 * Created on October 29, 2001, 12:11 AM
 */

package fina2.period;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author  vasop
 * @version 
 */
public interface PeriodTypeHome extends EJBHome {

    PeriodType create() throws EJBException, CreateException, RemoteException;

    PeriodType findByPrimaryKey(PeriodTypePK pk) throws EJBException,
            FinderException, RemoteException;

}
