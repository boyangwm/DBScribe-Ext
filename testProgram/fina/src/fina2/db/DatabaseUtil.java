package fina2.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import fina2.util.CommonUtils;

public class DatabaseUtil {

    private static Logger log = Logger.getLogger(DatabaseUtil.class);

    private static String dbProductName;

    public static boolean isOracle() {
        return dbProductName.toLowerCase().indexOf("oracle") >= 0;
    }

    public static boolean isMssql() {
        return dbProductName.toLowerCase().indexOf("microsoft sql server") >= 0;
    }

    public static boolean isMysql() {
        return dbProductName.toLowerCase().indexOf("mysql") >= 0;
    }

    public static Connection getConnection() throws EJBException {
        try {
            InitialContext jndi = new InitialContext();
            Object ref = jndi.lookup("java:/Fina2DS");

            DataSource ds = (DataSource) PortableRemoteObject.narrow(ref,
                    DataSource.class);

            Connection conn = ds.getConnection();

            dbProductName = conn.getMetaData().getDatabaseProductName();

            return conn;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    public static byte[] getBlob(ResultSet rs, int columnIndex)
            throws IOException, SQLException {

        byte buff[] = null;
        if (!DatabaseUtil.isOracle()) {
            buff = rs.getBytes(columnIndex);
        } else {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            java.io.InputStream in = rs.getBinaryStream(columnIndex);
            CommonUtils.copy(in, st);
            buff = st.toByteArray();
            st.close();
        }
        return buff;
    }

    public static byte[] getBlob(ResultSet rs, String columnName)
            throws IOException, SQLException {
        byte buff[] = null;
        if (!DatabaseUtil.isOracle()) {
            buff = rs.getBytes(columnName);
        } else {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            java.io.InputStream in = rs.getBinaryStream(columnName);
            CommonUtils.copy(in, st);
            buff = st.toByteArray();
            st.close();
        }
        return buff;
    }

    public static void setBlob(PreparedStatement pstmt, byte[] buff,
            int columnIndex) throws SQLException {

        if (!DatabaseUtil.isOracle()) {
            pstmt.setBytes(columnIndex, buff);
        } else {
            pstmt.setBinaryStream(columnIndex, new ByteArrayInputStream(buff),
                    buff.length);
        }
    }

    public static void closeConnection(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (Throwable th) {
            log.error("Error during closing connection", th);
        }
    }

    public static void closeStatement(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (Throwable th) {
            log.error("Error during closing statement", th);
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Throwable th) {
            log.error("Error during closing result set", th);
        }
    }

    public static void close(ResultSet rs, Statement st, Connection con) {
        closeResultSet(rs);
        closeStatement(st);
        closeConnection(con);
    }
}
