/*
    Documento   : Insumo.java
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
import java.sql.Timestamp;
import java.util.ArrayList;

public class Insumo extends org.apache.struts.action.ActionForm {

    private Long codigo;
    private String nombre;
    private String seccion;
    private String laboratorio;
    private String marca;
    private String modelo;
    private Boolean existe;
    private ArrayList<ItemInsumo> items = new ArrayList<ItemInsumo>();
    private Float total = 0f;

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Boolean isExiste() {
        return existe;
    }

    public void setExiste(Boolean existe) {
        this.existe = existe;
    }

    public ArrayList<ItemInsumo> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemInsumo> items) {
        this.items = items;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Insumo{" + "codigo=" + codigo + ", nombre=" + nombre + ", seccion=" + seccion + ", laboratorio=" + laboratorio + ", marca=" + marca + ", modelo=" + modelo + ", existe=" + existe + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ((this.nombre == null) || (this.nombre.length() < 1)){
            errors.add("nombreIns", new ActionMessage("error.nombre.requerido"));
        }

        return errors;
    }
}