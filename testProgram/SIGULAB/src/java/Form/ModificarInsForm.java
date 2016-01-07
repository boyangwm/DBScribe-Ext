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

public class ModificarInsForm extends org.apache.struts.action.ActionForm {

    private Integer codprod;
    private String nombre;
    private String serial;
    private String estado;

    public Integer getCodprod() {
        return codprod;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSerial() {
        return serial;
    }

    public String getEstado() {
        return estado;
    }

    public void setCodprod(Integer codprod) {
        this.codprod = codprod;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "ModificarInsForm{" + "nombre=" + nombre + ", serial=" + serial + ", estado=" + estado + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if ((nombre == null) || (nombre.trim().isEmpty())){
            if ((estado == null) || (estado.trim().isEmpty())){
                if ((serial == null) || (serial.trim().isEmpty())){
                    errors.add("modificarVacio", new ActionMessage("error.form.modificar.vacio"));
                }
            }
        }
        return errors;
    }


}