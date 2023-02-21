package com.tokio.pa.cotizadorpaso3portlet73.portlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorpaso3portlet73.constants.CotizadorPaso3Portlet73PortletKeys;

import java.io.PrintWriter;
import java.util.Base64;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = { "javax.portlet.name=" + CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73,
"mvc.command.name=/saveCaratulaComision" }, service = MVCResourceCommand.class)

public class SaveCaratulaComisionResourceCommand extends BaseMVCResourceCommand{

	@Reference
	CotizadorPaso3 _ServicePaso3;
	
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
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		String version = ParamUtil.getString(resourceRequest, "version");
		double p_primaObjetivo = ParamUtil.getDouble(resourceRequest, "comision");
		System.out.println("p_primaObjetivo :" + p_primaObjetivo);
		
		String p_usuario = user.getScreenName();
		String usuario = p_usuario;
//		String pantalla = CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73;
		String pantalla = "";
		
		String tipoCoti = ParamUtil.getString(resourceRequest, "tipoCoti");
		System.out.println("tipoCoti= " + tipoCoti);
		
		if (tipoCoti.toLowerCase().contains("familiar")) {
			pantalla = CotizadorPaso3Portlet73PortletKeys.PANTALLA_FAMILIAR;
		} else {
			pantalla = CotizadorPaso3Portlet73PortletKeys.PANTALLA_EMPRESARIAL;
		}
		
		CaratulaResponse envio = fDatosCaratula(cotizacion, version,p_primaObjetivo, usuario, pantalla);
		envio.setEmail(Base64.getEncoder().encodeToString(envio.getEmail().toString().getBytes()));
//		envio = _CotizadorService.getCaratula(int cotizacion, String version,String usuario,String pantalla);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(envio);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);
	}
	
	private CaratulaResponse fDatosCaratula(int cotizacion, String version,double p_primaObjetivo, String usuario,String pantalla) {
		try {
			return _ServicePaso3.GetCaratulaPrimaObjetivo(cotizacion, version, p_primaObjetivo, usuario, pantalla);
			/*return null;*/
		} catch (Exception e) {
			/* TODO Auto-generated catch block	*/
			return null;
		}
	}
	
}
