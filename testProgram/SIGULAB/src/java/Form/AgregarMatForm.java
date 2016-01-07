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

// Importaciones
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class AgregarMatForm extends org.apache.struts.action.ActionForm {

    private String nombre;
    private Integer cantidad;
    private String unidad;
    private String codlab;
    private String codsec;
    private String ubicacion;

    // GETTER
    public String getNombre() {
        return nombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getCodlab() {
        return codlab;
    }

    public String getCodsec() {
        return codsec;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    // SETTER
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public void setCodlab(String laboratorio) {
        this.codlab = laboratorio;
    }

    public void setCodsec(String seccion) {
        this.codsec = seccion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    // Validaciones
    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ((this.nombre == null) || (this.nombre.length() < 1)){
            errors.add("nombreMat", new ActionMessage("error.form.material.agregar.nombre"));
        }
        if ((this.unidad == null) || (this.unidad.length() < 1)){
            errors.add("unidadMat", new ActionMessage("error.form.material.agregar.unidad"));
        }
        if ((this.ubicacion == null) || (this.ubicacion.length() < 1)){
            errors.add("ubicacionMat", new ActionMessage("error.form.material.agregar.ubicacion"));
        }
        if (this.cantidad < 1){
            errors.add("cantidadMat", new ActionMessage("error.form.material.agregar.cantidad"));
        }
        return errors;
    }
}