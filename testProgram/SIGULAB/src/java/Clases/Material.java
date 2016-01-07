/*
    Documento   : Material.java
    Creado      : 26/11/2013, 06:23:23 PM
    Creado por  : Acro Systems
                  Carla Barazarte
                  Samantha Campisi
                  Carlos Cruz
                  Alejandro Garbi
                  Ramón Marquez
                  Esteban Oliveros
 */

// Paquetes
package Clases;


/**
 * La clase Material se encarga de manejar la información referente a las
 sustancias, reactivos, materiales, entre otros productos, que se encuentran 
 en los laboratorios
 */
public class Material {

    private Integer codprod;
    private String nombre;
    private Integer cantidad;
    private String unidad;
    private String laboratorio;
    private String seccion;
    private Boolean visible;
    private String fecha;
    private String ubicacion;
    
    // Getter

    public Integer getCodprod() {
        return codprod;
    }
    
    public String getNombre() {
        return nombre;
    }

    public Integer getCantidad() {
        if (this.cantidad == null){
            return 0;
        }
        return cantidad;
    }
    
    public String getUnidad() {
        return unidad;
    }
        
    public String getLaboratorio() {
        return laboratorio;
    }
    
    public String getSeccion() {
        return seccion;
    }
    
    public Boolean isVisible() {
        return visible;
    }
    
    public String getFecha() {
        return fecha;
    }
    
    public String getUbicacion() {
        return ubicacion;
    }
    
    // Setter
    public void setCodprod(Integer codprod) {
        this.codprod = codprod;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre.toLowerCase();
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad.toLowerCase();
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio.toLowerCase();
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion.toLowerCase();
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    @Override
    public String toString() {
        return "Material{" 
                + "nombre=" + nombre 
                + ", laboratorio=" + laboratorio 
                + ", cantidad=" + cantidad 
                + ", unidad=" + unidad 
                + ", seccion=" + seccion + '}';
    }

}