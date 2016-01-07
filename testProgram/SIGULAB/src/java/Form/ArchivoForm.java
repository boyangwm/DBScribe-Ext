package Form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import java.util.ArrayList;
import org.apache.struts.upload.FormFile;

public class ArchivoForm extends org.apache.struts.action.ActionForm {

    private FormFile archivo;
    private String tipo;
    private static final String CSV = "([^\\s]+(\\.(?i)(csv))$)";

    public FormFile getArchivo() {
        return archivo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setArchivo(FormFile archivo) {
        this.archivo = archivo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void destruirArchivo() {
        archivo.destroy();
        System.out.println("Archivo destruido");
    }

    @Override
    public String toString() {
        return "ArchivoInsForm{"
                + "archivo=" + archivo
                + ", tipo=" + tipo
                + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (this.archivo.getFileName().isEmpty()) {
            errors.add("archivoVacio", new ActionMessage("error.form.archivo.vacio"));
        } else {
            if (!this.archivo.getFileName().matches(CSV)) {
                errors.add("archivoTipo", new ActionMessage("error.form.archivo.tipo"));
            }
        }
        return errors;
    }

}
