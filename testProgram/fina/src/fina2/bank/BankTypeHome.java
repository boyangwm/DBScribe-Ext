/*
 * BankTypeHome.java
 *
 * Created on October 19, 2001, 7:32 PM
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
public interface BankTypeHome extends EJBHome {

    BankType create() throws EJBException, CreateException, RemoteException;

    BankType findByPrimaryKey(BankTypePK pk) throws EJBException,
            FinderException, RemoteException;

}
