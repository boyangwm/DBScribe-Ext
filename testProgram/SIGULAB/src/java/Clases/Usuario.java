/*
    Documento   : Usuario.java
    Creado      : 26/11/2013, 06:23:23 PM
    Creado por  : Acro Systems
                  Carla Barazarte
                  Samantha Campisi
                  Carlos Cruz
                  Alejandro Garbi
                  Ramón Marquez
                  Esteban Oliveros
 */

package Clases;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class Usuario extends org.apache.struts.action.ActionForm {

    private String usbid;
    private String contrasena;
    private String tipo;
    private String nombre;

    //-------- Para los trabajadores --------//
    private String laboratorio;
    private String seccion;
    //-----------------------------------//

    public String getUsbid() {
        return usbid;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setUsbid(String usbid) {
        this.usbid = usbid;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    @Override
    public String toString() {
        return "Usuario{"
                + "usbid=" + usbid
                + ", contrasena=" + contrasena
                + ", tipo=" + tipo
                + ", nombre=" + nombre
                + ", laboratorio=" + laboratorio
                + ", seccion=" + seccion
                + '}';
    }

}
