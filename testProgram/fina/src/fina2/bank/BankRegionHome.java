/*
 * BankRegionHome.java
 *
 * Created on March 25, 2002, 12:42 AM
 */

package fina2.bank;

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
@Deprecated
public interface BankRegionHome extends EJBHome {

    BankRegion create() throws EJBException, CreateException, RemoteException;

    BankRegion findByPrimaryKey(BankRegionPK pk) throws EJBException,
            FinderException, RemoteException;

}
