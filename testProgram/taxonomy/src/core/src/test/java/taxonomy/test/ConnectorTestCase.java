/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package taxonomy.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;
import junit.framework.TestCase;
import taxonomy.util.ORMTools;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 * Apr 18, 2012
 */
public class ConnectorTestCase extends TestCase {

	public void testSelect() throws Exception {
		Connection con = ORMTools.getConnection();
		ResultSet result = con.createStatement().executeQuery("Select count(id) from [NaturalObject]");
		while(result.next())
		{
			Assert.assertEquals(3718, result.getInt(1));
		}
		result.close();
		result = con.createStatement().executeQuery("Select * from [Family] where ID = 1");
		while(result.next()) {
			int count = 1;
			while(true) {
				try {
					System.out.println(result.getObject(count));
					count++;
				} catch (SQLException e) {
					break;
				}
			}
		}
	}
}
