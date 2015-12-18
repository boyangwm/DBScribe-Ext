/*
 * BankManagHome.java
 *
 * Created on April 22, 2002, 5:02 PM
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
public interface BankManagHome extends EJBHome {

    BankManag create() throws EJBException, CreateException, RemoteException;

    BankManag findByPrimaryKey(BankManagPK pk) throws EJBException,
            FinderException, RemoteException;

}
