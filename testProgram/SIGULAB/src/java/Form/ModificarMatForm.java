/*
Documento   : -
Creado por  : Acro Systems
Carla Barazarte
Samantha Campisi
Carlos Cruz
Alejandro Garbi
Ramón Marquez
Esteban Oliveros
*/
package Form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ModificarMatForm extends org.apache.struts.action.ActionForm {

    private Integer codprod;
    private String nombre;
    private Integer cantidad;
    private String unidad;
    private String ubicacion;

    public Integer getCodprod() {
        return codprod;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setCodprod(Integer codprod) {
        this.codprod = codprod;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public String toString() {
        return "ModificarMatForm{" + "codprod=" + codprod + ", nombre=" + nombre + ", cantidad=" + cantidad + ", unidad=" + unidad + ", ubicacion=" + ubicacion + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if ((nombre == null) || (nombre.trim().isEmpty())){
            if ((cantidad == null) || (cantidad == 0)){
                if ((unidad == null) || (unidad.trim().isEmpty())){
                    if ((ubicacion == null) || (ubicacion.trim().isEmpty())){
                    errors.add("modificarVacio", new ActionMessage("error.form.modificar.vacio"));
                    }
                }
            }
        }
        return errors;
    }


}