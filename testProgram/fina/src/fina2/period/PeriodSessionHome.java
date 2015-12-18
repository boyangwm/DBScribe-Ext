/*
 * PeriodSessionHome.java
 *
 * Created on October 30, 2001, 3:15 AM
 */
package fina2.period;

/**
 *
 * @author  vasop
 */
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface PeriodSessionHome extends EJBHome {

    PeriodSession create() throws CreateException, EJBException,
            RemoteException;

}
