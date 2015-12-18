/*
 * BankGroupHome.java
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
 * @author  Administrator
 * @version
 */
public interface BankGroupHome extends EJBHome {

    BankGroup create() throws EJBException, CreateException, RemoteException;

    BankGroup findByPrimaryKey(BankGroupPK pk) throws EJBException,
            FinderException, RemoteException;

}
