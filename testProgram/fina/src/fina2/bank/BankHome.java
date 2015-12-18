/*
 * BankHome.java
 *
 * Created on October 22, 2001, 8:07 PM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface BankHome extends EJBHome {

    Bank create() throws EJBException, CreateException, RemoteException;

    Bank findByPrimaryKey(BankPK pk) throws EJBException, FinderException,
            RemoteException;

}
