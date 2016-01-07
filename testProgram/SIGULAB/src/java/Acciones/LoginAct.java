/*
    Documento   : LoginAct.java
    Creado por  : Acro Systems
                  Carla Barazarte
                  Samantha Campisi
                  Carlos Cruz
                  Alejandro Garbi
                  Ramón Marquez
                  Esteban Oliveros
 */

package Acciones;

import Clases.Usuario;
import Form.LoginForm;
import DBMS.DBMS;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.WordUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class LoginAct extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ActionErrors errors = new ActionErrors();
        LoginForm login = (LoginForm) form;
        HttpSession session = request.getSession(true);
        Usuario usuario = DBMS.getInstance().ingresar(login);

        if ( usuario.getTipo() == null || (usuario.getTipo().length() < 1) ){
            errors.add("contrasena", new ActionMessage("error.act.login.missmatch"));
            saveErrors(request, errors);
            return mapping.findForward("failure");
        }

        if (validarTipo(usuario.getTipo())){
            session.setMaxInactiveInterval(0);
            session.setAttribute("usbid", usuario.getUsbid());
            session.setAttribute("tipo", usuario.getTipo());
            session.setAttribute("nombre", usuario.getNombre());
            if (!usuario.getTipo().matches("pregrado|postgrado|gsmdp")){
                session.setAttribute("trabaja", true);
                usuario.setLaboratorio(DBMS.getInstance().trabajaEnLaboratorio(usuario.getUsbid()));
                usuario.setSeccion(DBMS.getInstance().trabajaEnSeccion(usuario.getUsbid()));
                session.setAttribute("trabajaLab", usuario.getLaboratorio());
                session.setAttribute("trabajaSec", usuario.getSeccion());
                if (usuario.getTipo().matches("jefelab|jefeseccion|responsable")){
                    session.setAttribute("gestion", true);
                }
                session.setAttribute("nombreLab", WordUtils.capitalize(DBMS.getInstance().nombreLaboratorio(usuario.getLaboratorio())));
                session.setAttribute("nombreSec", WordUtils.capitalize(DBMS.getInstance().nombreSeccion(usuario.getSeccion())));
            }

            if (usuario.getTipo().matches("gsmdp")){
                session.setAttribute("gsmdp", true);
            }

            System.out.println("[LoginAct]Autenticación y validación correctas");
            System.out.println(usuario.toString());
            return mapping.findForward("success");
        } else {
            System.out.println("[LoginAct]Autenticación correcta. Falla validación de tipo "
                    + usuario.getTipo());
            errors.add("tipo", new ActionMessage("error.act.login.tipo"));
            return mapping.findForward("error");
        }
    }

    private static boolean validarTipo(String tipo){

        if (tipo.length() > 0) {
            if (tipo.equals("pregrado")) {return true;}
            else if (tipo.equals("postgrado")) {return true;}
            else if (tipo.equals("tecnico")) {return true;}
            else if (tipo.equals("profesor")) {return true;}
            else if (tipo.equals("jefelab")) {return true;}
            else if (tipo.equals("jefeseccion")) {return true;}
            else if (tipo.equals("responsable")) {return true;}
            else if (tipo.equals("gsmdp")) {return true;}
            else {return false;}
        } else {return false;}

    }

}
