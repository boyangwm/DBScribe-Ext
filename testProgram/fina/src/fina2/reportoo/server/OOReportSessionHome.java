/*
 * OOReportSessionHome.java
 *
 * Created on 6 ќкт€брь 2002 г., 13:08
 */

package fina2.reportoo.server;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public interface OOReportSessionHome extends EJBHome {

    OOReportSession create() throws CreateException, EJBException,
            RemoteException;
}
