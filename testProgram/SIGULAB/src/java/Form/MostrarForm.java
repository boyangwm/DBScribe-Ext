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

package Form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class MostrarForm extends org.apache.struts.action.ActionForm {

    private Integer codprod;
    private String tipo;
    private Boolean visible;

    public Integer getCodprod() {
        return codprod;
    }

    public String getTipo() {
        return tipo;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setCodprod(Integer codprod) {
        this.codprod = codprod;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "MostrarForm{" + "codprod=" + codprod + ", tipo=" + tipo + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }


}
