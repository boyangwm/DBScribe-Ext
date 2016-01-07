/*
    Documento   : LoginForm.java
    Creado por  : Acro Systems
                  Carla Barazarte
                  Samantha Campisi
                  Carlos Cruz
                  Alejandro Garbi
                  Ramón Marquez
                  Esteban Oliveros
 */

// Paquetes
package Form;

// Importaciones
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class LoginForm extends org.apache.struts.action.ActionForm {

    // Atributos
    private String usbid;
    private String contrasena;

    
    // Getters
    public String getUsbid() {
        return usbid;
    }

    public String getContrasena() {
        return contrasena;
    }

    // Setters
    public void setUsbid(String usbid) {
        this.usbid = usbid;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Override
    public String toString() {
        return "LoginForm{" 
                + "usbid=" + usbid 
                + ", contrasena=" + contrasena 
                + '}';
    }

    // Validaciones    
    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        if (getUsbid() == null || getUsbid().length() < 1) {
            errors.add("usbid", new ActionMessage("error.form.login.usbid.requerido"));
        }
        
        if (getContrasena() == null || getContrasena().length() < 1) {
            errors.add("contrasena", new ActionMessage("error.form.login.contrasena.requerido"));
        }

        return errors;
    }
    

}
