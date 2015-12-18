/*
 * ReturnSessionHome.java
 *
 * Created on 31 ќкт€брь 2001 г., 21:54
 */

package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface ReturnSessionHome extends EJBHome {

    ReturnSession create() throws CreateException, EJBException,
            RemoteException;
}
