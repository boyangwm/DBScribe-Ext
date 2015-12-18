/*
 * BankRegionSessionHome.java
 *
 * Created on March 26, 2002, 4:20 AM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

/**
 *
 * @author  vasop
 * @version 
 */
public interface BankRegionSessionHome extends EJBHome {

    BankRegionSession create() throws CreateException, EJBException,
            RemoteException;

}
