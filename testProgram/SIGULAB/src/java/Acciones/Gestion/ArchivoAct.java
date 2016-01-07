package Acciones.Gestion;

import Acciones.LoginAct;
import Clases.Insumo;
import Clases.Material;
import DBMS.DBMS;
import Form.ArchivoForm;
import Form.AgregarInsForm;
import Form.AgregarMatForm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import java.lang.Object;
import org.apache.commons.lang.WordUtils;
import java.util.ArrayList;

public class ArchivoAct extends org.apache.struts.action.Action {

    public static ArrayList agregadosIns;
    public static ArrayList agregadosMat;
    public static ArrayList erroneosIns;
    public static ArrayList erroneosMat;

    private String[] extraerDatos(ArchivoForm formulario) throws IOException{

        FormFile archivo = formulario.getArchivo();
        int tamanio = archivo.getFileSize();
        byte[] data = archivo.getFileData();
        String buffer = new String();
        // Transformando la data a un string
        for(int iter = 0; iter<tamanio; iter++){
            buffer = buffer + ((char)(data[iter]));
        }
        String[] saltoSplit = buffer.split("\n");
        //String[] saltoSplit = buffer.split("\r\n");
        return saltoSplit;
    }
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ActionErrors error = new ActionErrors();
        ArchivoForm formulario = (ArchivoForm) form;
        String codsec = (String) request.getSession().getAttribute("codsec");
        String codlab = (String) request.getSession().getAttribute("codlab");
        agregadosIns = new ArrayList();
        agregadosMat = new ArrayList();
        erroneosIns  = new ArrayList();
        erroneosMat  = new ArrayList();

        error = formulario.validate(mapping, request);
        
        if (!error.isEmpty()){
            saveErrors(request.getSession(), error);
            if (formulario.getTipo().equalsIgnoreCase("insumo")){
                return mapping.findForward("redirectIns");
            } else {
                return mapping.findForward("redirectMat");
            }
        }
            

        if (formulario.getTipo().equalsIgnoreCase("insumo")){

            String [] info = extraerDatos(formulario);
            // Ahora tenemos la informacion en una lista
            String[] campos = null;
            AgregarInsForm insumo;
            DBMS dbms = DBMS.getInstance();
            // Comenzamos en 1 ya que 0 es la fila de identificadores de columna
            // (Por defecto asi vienen los CSV)

            for(int iter=1 ; iter<info.length ; iter++){
                insumo = new AgregarInsForm();
                campos = info[iter].split(";");
                if ((campos.length > 0) && (campos.length < 4)){
                    insumo.setNombre(WordUtils.capitalize(campos[0]));
                    if (campos.length > 1){
                        insumo.setSerial(WordUtils.capitalize(campos[1]));
                    } else {
                        insumo.setSerial("");                    
                    }
                    if (campos.length > 2){
                        insumo.setEstado(WordUtils.capitalize(campos[2]));
                    } else {
                        insumo.setEstado("");
                    }
                    insumo.setCodlab(codlab);
                    insumo.setCodsec(codsec);
                    if (campos[0].length() <= 0){
                        erroneosIns.add(insumo);
                    } else {
                        if (dbms.agregarDatosIns(insumo)){
                            agregadosIns.add(insumo);
                        } else {
                            erroneosIns.add(insumo);
                        }
                    }
                }
            }
        } else if (formulario.getTipo().equalsIgnoreCase("material")){
            
            String [] info = extraerDatos(formulario);
            // Ahora tenemos la informacion en una lista
            String[] campos = null;
            AgregarMatForm material;
            DBMS dbms = DBMS.getInstance();
            // Comenzamos en 1 ya que 0 es la fila de identificadores de columna
            // (Por defecto asi vienen los CSV)
            for(int iter=1 ; iter<info.length ; iter++){
                material = new AgregarMatForm();
                campos = info[iter].split(";");
                if ((campos.length > 0) && (campos.length < 5)){
                    material.setNombre(WordUtils.capitalize(campos[0]));
                    if (campos.length > 1){
                        material.setCantidad(Integer.getInteger(campos[1]));
                    } else {
                        material.setCantidad(0);                    
                    }
                    if (campos.length > 2){
                        material.setUnidad(WordUtils.capitalize(campos[2]));
                    } else {
                        material.setUnidad("");
                    }
                    if (campos.length > 3){
                        material.setUbicacion(WordUtils.capitalize(campos[3]));
                    } else {
                        material.setUbicacion("");
                    }
                    material.setCodlab(codlab);
                    material.setCodsec(codsec);
                    if (campos[0].length() <= 0){
                        erroneosMat.add(material);
                    } else {
                        if (dbms.agregarDatosMat(material)){
                            agregadosMat.add(material);
                        } else {
                            erroneosMat.add(material);
                        }
                    }
                }
            }

        } else {
            return mapping.findForward("error");
        }
        formulario.destruirArchivo();
        return mapping.findForward("redireccionar");

    }
}
