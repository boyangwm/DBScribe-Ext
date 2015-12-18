/*
 * PeriodHome.java
 *
 * Created on October 31, 2001, 4:09 AM
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
public interface PeriodHome extends EJBHome {

    Period create() throws EJBException, CreateException, RemoteException;

    Period findByPrimaryKey(PeriodPK pk) throws EJBException, FinderException,
            RemoteException;

}
