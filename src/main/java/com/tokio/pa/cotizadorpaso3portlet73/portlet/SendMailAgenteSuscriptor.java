package com.tokio.pa.cotizadorpaso3portlet73.portlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.CorreosSuscripcionRequest;
import com.tokio.pa.cotizadorModularServices.Bean.CorreosSuscripcionResponse;
import com.tokio.pa.cotizadorModularServices.Bean.IdCarpetaResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Enum.TipoCotizacion;
import com.tokio.pa.cotizadorModularServices.Exception.CotizadorModularException;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;
import com.tokio.pa.cotizadorpaso3portlet73.constants.CotizadorPaso3Portlet73PortletKeys;
import com.tokio.pa.cotizadorpaso3portlet73.util.SendMailAgenteSuscriptorP2;

import java.io.File;
import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true, property = { 
				"javax.portlet.name=" + CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73, 
		"mvc.command.name=/sendMailAgenteSuscriptor" }, service = MVCResourceCommand.class
		)

public class SendMailAgenteSuscriptor extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso3 _ServicePaso3;
	@Reference
	CotizadorGenerico _ServiceGenerico;
	
	private static final Log _log = LogFactoryUtil.getLog(SendMailAgenteSuscriptor.class);
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		/************************** Validación metodo post **************************/
		if ( !resourceRequest.getMethod().equals("POST")  ){
			JsonObject requestError = new JsonObject();
			requestError.addProperty("code", 500);
			requestError.addProperty("msg", "Error en tipo de consulta");
			PrintWriter writer = resourceResponse.getWriter();
			writer.write(requestError.toString());
			return;
		}
		/************************** Validación metodo post **************************/
		
		if(enviaArchivos(resourceRequest)){
			enviaComentarios(resourceRequest);
			enviaMails(resourceRequest);
			
			int isRevire = ParamUtil.getInteger(resourceRequest, "isRevire");
			if(isRevire == 1){
				revire(resourceRequest);
			}			
		}else{
			PrintWriter writer = resourceResponse.getWriter();
			writer.write("{\"codigo\" : 2, \"error\" : \"Error al procesar su información\" }");
		}
	}

boolean enviaArchivos(ResourceRequest resourceRequest){
		
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		int folio = ParamUtil.getInteger(resourceRequest, "folio");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		String auxiliarDoc = HtmlUtil.unescape(ParamUtil.getString(resourceRequest, "auxiliarDoc"));
		int totArchivos = ParamUtil.getInteger(resourceRequest, "totArchivos");
		System.err.println("totArchivos: "+ totArchivos);
		
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();
		try {
			if(totArchivos > 0){
				System.out.println("Entre TRY archivos");
//				String jsonDocumentos = "";
				JsonArray jsonDocumentos = new JsonArray();
				JSONObject jsonObj;
				jsonObj = JSONFactoryUtil.createJSONObject(auxiliarDoc);
				UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
				
				IdCarpetaResponse carpeta = _ServiceGenerico.SeleccionaIdCarpeta(folio, cotizacion, version);
				
				if (carpeta.getCode() > 0){
					return false;
				}
				
				for(int i = 0; i < totArchivos; i++){
					String nombre = "file-"+i;
					File file = uploadRequest.getFile(nombre);
					
					JSONObject jsonObj2;
					jsonObj2 = JSONFactoryUtil.createJSONObject(jsonObj.getString(nombre));
					
					JsonObject curDocument = new JsonObject();
					curDocument.addProperty("nombre", jsonObj2.getString("nom"));
					curDocument.addProperty("extension", jsonObj2.getString("ext"));
					curDocument.addProperty("idCarpeta", carpeta.getIdCarpeta());
					curDocument.addProperty("idDocumento", 0); /*Numero cotizacion*/
					curDocument.addProperty("idCatalogoDetalle", 1413);
					curDocument.addProperty("documento", Base64.encode(FileUtils.readFileToByteArray(file)));
					
					jsonDocumentos.add(curDocument);

					
				}
				
				_ServiceGenerico.wsDocumentos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
						CotizadorModularServiceKey.TMX_CTE_TRANSACCION_POST,
						jsonDocumentos, 1, "COTIZACION_PA", cotizacion, "", p_usuario, CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("error en el archivo");
			return false;
		}
	}
	
	void enviaMails(ResourceRequest resourceRequest){
		String folio = ParamUtil.getString(resourceRequest, "folio");
		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		String version = ParamUtil.getString(resourceRequest, "version");
		String url = ParamUtil.getString(resourceRequest, "url");
		String comentarios = ParamUtil.getString(resourceRequest, "comentarios");
		int isRevire = ParamUtil.getInteger(resourceRequest, "isRevire");
		String tipoCotizacion = ParamUtil.getString(resourceRequest, "tipoCotizacion");
		try {
			String	mails = obtenMails(resourceRequest);
			String[] listMails = null ;
			if (Validator.isNotNull(mails)){
				listMails = mails.split(",");
			}
			String link= url + "?infoCotizacion=" + generaUrl(folio, cotizacion, version, ModoCotizacion.EDICION, tipoCotizacion);
			
			_log.info("Url agente -> suscriptor : " + link);

			System.out.println("url : " + link);
			if (Validator.isNotNull(listMails)){
				new SendMailAgenteSuscriptorP2().sendMail(listMails, comentarios, folio, link);				
			}
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	String obtenMails(ResourceRequest resourceRequest) throws CotizadorModularException{
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();
		CorreosSuscripcionRequest correo = new CorreosSuscripcionRequest();
		correo.setIdPerfil( (int) originalRequest.getSession().getAttribute("idPerfil"));
		correo.setP_cotizacion( ParamUtil.getInteger(resourceRequest, "cotizacion"));
		correo.setP_version(ParamUtil.getInteger(resourceRequest, "version"));
		correo.setP_pantalla( CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73);
		correo.setP_usuario(p_usuario);
		
		CorreosSuscripcionResponse respuesta = _ServicePaso3.enviaSuscripcion(correo);
		
		if (respuesta.getCode() == 0){
			if (Validator.isNotNull( respuesta.getEmails())){
				//return "";
				//return "jonfacundov@gmail.com";
				return respuesta.getEmails();
			}
		}
		return "";
	}
	
	void enviaComentarios(ResourceRequest resourceRequest) throws CotizadorModularException{
		String comentario = ParamUtil.getString(resourceRequest, "comentarios");
		comentario = Validator.isNull(comentario) ? "-- Sin comentarios --" : comentario;
		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String usuario = user.getScreenName();
		
		_ServicePaso3.guardarComentario(cotizacion, version, 1, 0, comentario, usuario, CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73);
		
	}
	
	void revire(ResourceRequest resourceRequest){
		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String usuario = themeDisplay.getUser().getScreenName();
		String pantalla = CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73;
		String comentario = ParamUtil.getString(resourceRequest, "comentarios");
		comentario = Validator.isNull(comentario) ? "-- Sin comentarios --" : comentario;
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));

		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

		try {
			_ServicePaso3.revire(cotizacion, version, comentario, usuario, pantalla, idPerfilUser);
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String generaUrl(String folio, String cotizacion, String version, ModoCotizacion modo, String tipoCotizacion){
		InfoCotizacion infCot = new InfoCotizacion();
		int intFolio = Integer.parseInt(folio);
		long longCotizacion = Long.parseLong(cotizacion);
		int intVersion = Integer.parseInt(version);
		infCot.setFolio(intFolio);
		infCot.setCotizacion(longCotizacion);
		infCot.setVersion(intVersion);
		infCot.setModo(modo);
		if(tipoCotizacion.equals("EMPRESARIAL")){
			infCot.setPantalla(CotizadorPaso3Portlet73PortletKeys.PANTALLA_EMPRESARIAL);
			infCot.setTipoCotizacion(TipoCotizacion.EMPRESARIAL);
		}else{
			infCot.setPantalla(CotizadorPaso3Portlet73PortletKeys.PANTALLA_FAMILIAR);
			infCot.setTipoCotizacion(TipoCotizacion.FAMILIAR);
		}
		return CotizadorModularUtil.encodeURL(infCot);
	}
}
