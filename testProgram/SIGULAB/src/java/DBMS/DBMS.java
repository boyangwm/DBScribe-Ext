package DBMS;

import Clases.*;
import Form.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.WordUtils;

public class DBMS {

    static private Connection conexion;

    protected DBMS() {
    }
    static private DBMS instance = null;

    static public DBMS getInstance() {
        if (null == DBMS.instance) {
            DBMS.instance = new DBMS();
        }
        conectar();
        return DBMS.instance;
    }

    public static boolean conectar() {
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/sigulab",
                    "postgres",
                    "postgres");
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // Usuarios

    public Usuario ingresar(LoginForm login) {

        PreparedStatement consulta = null;
        Usuario usuario = new Usuario();
        try {
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.usuario"
                    + " WHERE usbid = ? AND contrasena = ?");
            consulta.setString(1, login.getUsbid());
            consulta.setString(2, login.getContrasena());
            ResultSet rs = consulta.executeQuery();
            System.out.println("---------- ingresar --------");
            System.out.println(consulta.toString());
            if (rs.next()) {
                usuario.setUsbid(login.getUsbid());
                usuario.setNombre(rs.getString("nombre").trim());
                usuario.setTipo(rs.getString("tipo").trim());
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return usuario;
    }

    public String trabajaEnLaboratorio(String usbid){

        String laboratorio = new String();
        PreparedStatement consulta = null;
        try {
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.trabaja"
                    + " WHERE trabajador = ? ");
            consulta.setString(1, usbid.toLowerCase());
            ResultSet rs = consulta.executeQuery();
            System.out.println("---------- trabajaEnLaboratorio --------");
            System.out.println(consulta.toString());
            if (rs.next()) {
                laboratorio = rs.getString("laboratorio");
            }
        } catch (SQLException ex) {
            System.out.println("Error[trabajaEnLaboratorio]");
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return laboratorio;
    }

    public String trabajaEnSeccion(String usbid){

        String seccion = new String();
        PreparedStatement consulta = null;
        try {
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.trabaja"
                    + " WHERE trabajador = ? ");
            consulta.setString(1, usbid.toLowerCase());
            ResultSet rs = consulta.executeQuery();
            System.out.println("---------- trabajaEnSeccion --------");
            System.out.println(consulta.toString());
            if (rs.next()) {
                seccion = rs.getString("seccion");
            }
        } catch (SQLException ex) {
            System.out.println("Error[trabajaEnSeccion]");
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seccion;
    }

    // Nombres

    public String nombreLaboratorio(String codlab){
        String laboratorio = new String();
        PreparedStatement consulta = null;
        try {
            System.out.println("---------- nombreLaboratorio --------");
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.laboratorio"
                    + " WHERE codlab = ? ");
            consulta.setString(1, codlab.toLowerCase());
            ResultSet rs = consulta.executeQuery();
            System.out.println(consulta.toString());
            if (rs.next()) {
                laboratorio = WordUtils.capitalize(rs.getString("nombre"));
            }
        } catch (SQLException ex) {
            System.out.println("Error[trabajaEnLaboratorio]");
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return laboratorio;
    }

    public String nombreSeccion(String codsec){
        String seccion = new String();
        PreparedStatement consulta = null;
        try {
            System.out.println("---------- nombreSeccion --------");
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.seccion"
                    + " WHERE codsec = ? ");
            consulta.setString(1, codsec.toLowerCase());
            ResultSet rs = consulta.executeQuery();
            System.out.println(consulta.toString());
            if (rs.next()) {
                seccion = WordUtils.capitalize(rs.getString("nombre"));
            }
        } catch (SQLException ex) {
            System.out.println("Error[trabajaEnLaboratorio]");
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seccion;
    }

    // Insumo

    public HashMap gestionIns(String codlab, String codsec) {

        PreparedStatement consulta = null;
        HashMap<Long,Insumo> tabla = new HashMap<Long,Insumo>();
        Insumo insumo;
        ItemInsumo item;
        try {
            System.out.println("---------- gestionIns --------");
            consulta = conexion.prepareStatement("SELECT * "
                    + "FROM modulo2.insumo ins "
                    + "WHERE ins.laboratorio = ? "
                    + "AND ins.seccion = ? "
                    + "AND ins.existe ORDER BY nombre;");
            consulta.setString(1, codlab.toLowerCase());
            consulta.setString(2, codsec.toLowerCase());
            ResultSet rs = consulta.executeQuery();
            System.out.println(consulta.toString());
            while (rs.next()) {
                if (!tabla.containsKey(rs.getLong("codigo"))){
                    insumo = new Insumo();
                    insumo.setCodigo(rs.getLong("codigo"));
                    insumo.setNombre(rs.getString("nombre"));
                    insumo.setMarca(rs.getString("marca"));
                    insumo.setModelo(rs.getString("modelo"));
                    tabla.put(insumo.getCodigo(), insumo);
                }
            }

            System.out.println("---------- gestionIns --------");
            consulta = conexion.prepareStatement("SELECT * "
                    + "FROM modulo2.insumo ins, modulo2.item_insumo item "
                    + "WHERE ins.codigo = item.insumo "
                    + "AND ins.laboratorio = ? "
                    + "AND ins.seccion = ? "
                    + "AND item.existeitem AND ins.existe ORDER BY item.numero;");
            consulta.setString(1, codlab.toLowerCase());
            consulta.setString(2, codsec.toLowerCase());
            rs = consulta.executeQuery();
            System.out.println(consulta.toString());
            while (rs.next()) {
                item = new ItemInsumo();
                item.setNumero(rs.getInt("numero"));
                item.setId(rs.getLong("id"));
                item.setSerial(rs.getString("serial"));
                item.setEstado(rs.getString("estado"));
                item.setObservacion(rs.getString("observacion"));
                item.setVisible(rs.getBoolean("visible"));
                item.setFechaMod(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(rs.getTimestamp("fechaMod")));
                item.setFechaAgr(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(rs.getTimestamp("fechaAgr")));
                tabla.get(rs.getLong("codigo")).getItems().add(item);
            }
            System.out.println(consulta.toString());
            return tabla;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean agregar(Insumo insumo) {

        System.out.println(insumo.toString());
        PreparedStatement insertar = null;
        try {
            insertar = conexion.prepareStatement("INSERT INTO modulo2.insumo"
                    + " (nombre, seccion, laboratorio, marca, modelo) "
                    + " VALUES (?, ?, ?, ?, ?)");
            insertar.setString(1, insumo.getNombre());
            insertar.setString(2, insumo.getSeccion().toLowerCase());
            insertar.setString(3, insumo.getLaboratorio().toLowerCase());
            insertar.setString(4, insumo.getMarca());
            insertar.setString(5, insumo.getModelo());
            System.out.println("---------- agregar: Insumo --------");
            System.out.println(insertar.toString());
            Integer i = insertar.executeUpdate();
            return i > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean agregar(ItemInsumo insumo) {

        PreparedStatement insertar = null;
        PreparedStatement chequear = null;
        long time = System.currentTimeMillis();
        Timestamp fecha = new Timestamp(time);
        try {

            chequear = conexion.prepareStatement("SELECT * FROM modulo2.item_insumo "
                    + "WHERE numero = ? AND existeItem;");
            chequear.setInt(1, insumo.getNumero());
            ResultSet rs = chequear.executeQuery();
            if (rs.next()){
                return false;
            }
            insertar = conexion.prepareStatement("INSERT INTO modulo2.item_insumo"
                    + " (insumo, numero, serial, estado, observacion, fechaMod, fechaAgr) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)");
            insertar.setLong(1, insumo.getInsumo());
            insertar.setInt(2, insumo.getNumero());
            insertar.setString(3, insumo.getSerial());
            insertar.setString(4, insumo.getEstado());
            insertar.setString(5, insumo.getObservacion());
            insertar.setTimestamp(6, fecha);
            insertar.setTimestamp(7, fecha);
            System.out.println("---------- agregar: ItemInsumo --------");
            System.out.println(insertar.toString());
            Integer i = insertar.executeUpdate();
            return i > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean modificar(Insumo insumo) {

        PreparedStatement actualizar = null;
        try {
            actualizar = conexion.prepareStatement("UPDATE modulo2.insumo "
                    + "SET nombre = ? , marca = ?, modelo = ? "
                    + " WHERE codigo = ?");
            actualizar.setString(1, insumo.getNombre());
            actualizar.setString(2, insumo.getMarca());
            actualizar.setString(3, insumo.getModelo());
            actualizar.setLong(4, insumo.getCodigo());
            System.out.println("---------- modificar: Insumo --------");
            System.out.println(actualizar.toString());
            Integer i = actualizar.executeUpdate();
            return i > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean modificar(ItemInsumo insumo) {

        PreparedStatement actualizar = null;
        PreparedStatement chequear = null;
        long time = System.currentTimeMillis();
        Timestamp fecha = new Timestamp(time);
        try {

            chequear = conexion.prepareStatement("SELECT * FROM modulo2.item_insumo "
                    + "WHERE numero = ? AND NOT(id = ?);");
            chequear.setInt(1, insumo.getNumero());
            chequear.setLong(2, insumo.getId());
            ResultSet rs = chequear.executeQuery();
            if (rs.next()){
                return false;
            }
            actualizar = conexion.prepareStatement("UPDATE modulo2.item_insumo "
                    + "SET numero = ?, serial = ?, estado = ?, observacion = ?, fechaMod = ? "
                    + " WHERE id = ?");
            actualizar.setInt(1, insumo.getNumero());
            actualizar.setString(2, insumo.getSerial());
            actualizar.setString(3, insumo.getEstado());
            actualizar.setString(4, insumo.getObservacion());
            actualizar.setTimestamp(5, fecha);
            actualizar.setLong(6, insumo.getId());
            System.out.println("---------- modificar: ItemInsumo --------");
            System.out.println(actualizar.toString());
            Integer i = actualizar.executeUpdate();
            return i > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean eliminar(Insumo insumo) {

        PreparedStatement actualizar = null;
        ItemInsumo item;
        try {
            actualizar = conexion.prepareStatement("UPDATE modulo2.insumo "
                    + "SET existe = 'f' "
                    + " WHERE codigo = ?");
            actualizar.setLong(1, insumo.getCodigo());
            System.out.println("---------- eliminar: Insumo --------");
            System.out.println(actualizar.toString());
            Integer i = actualizar.executeUpdate();
            if ( i > 0 ){
                actualizar = conexion.prepareStatement("SELECT * FROM modulo2.item_insumo "
                        + "WHERE insumo = ?");
                actualizar.setLong(1, insumo.getCodigo());
                ResultSet rs = actualizar.executeQuery();
                while (rs.next()){
                    item = new ItemInsumo();
                    item.setId(rs.getLong("id"));
                    eliminar(item);
                }
                return true;
            }
            return false;
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean eliminar(ItemInsumo insumo) {

        PreparedStatement actualizar = null;
        long time = System.currentTimeMillis();
        Timestamp fecha = new Timestamp(time);
        try {
            actualizar = conexion.prepareStatement("UPDATE modulo2.item_insumo "
                    + "SET existeItem = 'f', fechaEli = ? "
                    + " WHERE id = ?");
            actualizar.setTimestamp(1, fecha);
            actualizar.setLong(2, insumo.getId());
            System.out.println("---------- eliminar: ItemInsumo --------");
            System.out.println(actualizar.toString());
            Integer i = actualizar.executeUpdate();
            return i > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void visualizar(ItemInsumo insumo) {

        PreparedStatement actualizar = null;
        try {
            actualizar = conexion.prepareStatement("UPDATE modulo2.item_insumo "
                    + "SET visible = NOT(visible) "
                    + " WHERE id = ?");
            actualizar.setLong(1, insumo.getId());
            System.out.println("---------- visibilizar: ItemInsumo --------");
            System.out.println(actualizar.toString());
            Integer i = actualizar.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap consultar(Insumo form){

        PreparedStatement consulta = null;
        HashMap<Long,Insumo> tabla = new HashMap<Long,Insumo>();
        ItemInsumo item;
        Insumo insumo;
        try {
            System.out.println("---------- consultaIns --------");
            consulta = conexion.prepareStatement("SELECT * "
                    + "FROM modulo2.insumo ins, modulo2.item_insumo item "
                    + "WHERE ins.codigo = item.insumo "
                    + "AND ins.laboratorio LIKE ? "
                    + "AND ins.seccion LIKE ? "
                    + "AND LOWER(ins.nombre) LIKE LOWER(?) "
                    + "AND LOWER(ins.marca) LIKE LOWER(?) "
                    + "AND LOWER(ins.modelo) LIKE LOWER(?) "
                    + "AND ins.existe AND item.existeItem AND item.visible "
                    + "ORDER BY ins.laboratorio, ins.seccion, ins.nombre;");
            System.out.println(consulta.toString());
            if (form.getLaboratorio().isEmpty()){
                consulta.setString(1, "%");
            } else {
                consulta.setString(1, form.getLaboratorio());
            }
            if (form.getSeccion().isEmpty()){
                consulta.setString(2, "%");
            } else {
                consulta.setString(2, form.getSeccion());
            }
            consulta.setString(3, "%"+form.getNombre()+"%");
            consulta.setString(4, "%"+form.getMarca()+"%");
            consulta.setString(5, "%"+form.getModelo()+"%");
            ResultSet rs = consulta.executeQuery();
            while (rs.next()) {
                if (!tabla.containsKey(rs.getLong("codigo"))){
                    insumo = new Insumo();
                    insumo.setCodigo(rs.getLong("codigo"));
                    insumo.setNombre(rs.getString("nombre"));
                    insumo.setMarca(rs.getString("marca"));
                    insumo.setModelo(rs.getString("modelo"));
                    insumo.setLaboratorio(rs.getString("laboratorio"));
                    insumo.setSeccion(rs.getString("seccion"));
                    tabla.put(insumo.getCodigo(), insumo);
                }
                item = new ItemInsumo();
                item.setId(rs.getLong("id"));
                tabla.get(rs.getLong("codigo")).getItems().add(item);
                tabla.get(rs.getLong("codigo")).setTotal(tabla.get(rs.getLong("codigo")).getTotal()+1f);
            }
            return tabla;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }


// Utilidades

    public ArrayList listarLaboratorios() {
        PreparedStatement consulta = null;
        ArrayList laboratorios = new ArrayList();
        try {
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.laboratorio ORDER BY nombre");
            ResultSet rs = consulta.executeQuery();
            System.out.println(consulta.toString());
            Laboratorio lab;
            while (rs.next()) {
                lab = new Laboratorio();
                lab.setNombre(WordUtils.capitalize(rs.getString("nombre")));
                lab.setCodigo(rs.getString("codlab").toUpperCase());
                lab.setJefe(rs.getString("jefe"));
                laboratorios.add(lab);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return laboratorios;
    }

    public ArrayList listarSecciones() {
        PreparedStatement consulta = null;
        ArrayList secciones = new ArrayList();
        try {
            consulta = conexion.prepareStatement("SELECT * FROM modulo2.seccion ORDER BY nombre");
            ResultSet rs = consulta.executeQuery();
            System.out.println(consulta.toString());
            Seccion sec;
            while (rs.next()) {
                sec = new Seccion();
                sec.setNombre(WordUtils.capitalize(rs.getString("nombre")));
                sec.setCodigo(rs.getString("codsec").toUpperCase());
                sec.setJefe(rs.getString("jefe"));
                sec.setLaboratorio(rs.getString("codlab").toUpperCase());
                secciones.add(sec);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return secciones;
    }
}