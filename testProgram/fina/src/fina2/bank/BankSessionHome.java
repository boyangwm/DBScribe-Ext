/*
 * BankSessionHome.java
 *
 * Created on October 19, 2001, 7:47 PM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface BankSessionHome extends EJBHome {

    BankSession create() throws CreateException, EJBException, RemoteException;

}
