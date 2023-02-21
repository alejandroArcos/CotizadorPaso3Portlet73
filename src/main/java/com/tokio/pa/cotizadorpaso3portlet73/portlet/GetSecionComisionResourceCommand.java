package com.tokio.pa.cotizadorpaso3portlet73.portlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Exception.CotizadorModularException;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorpaso3portlet73.constants.CotizadorPaso3Portlet73PortletKeys;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = { "javax.portlet.name=" + CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73,
"mvc.command.name=/getSeccionComisionUrl" }, service = MVCResourceCommand.class)

public class GetSecionComisionResourceCommand extends BaseMVCResourceCommand{
	
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	@Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
		try {
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

			double sc = ParamUtil.getDouble(resourceRequest, "seccomi");
			int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
			String version = ParamUtil.getString(resourceRequest, "version");
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String p_usuario = user.getScreenName();

			String pantalla = "";
			
			String tipoCoti = ParamUtil.getString(resourceRequest, "tipoCoti");
			System.out.println("tipoCoti= " + tipoCoti);
			
			if (tipoCoti.toLowerCase().contains("familiar")) {
				pantalla = CotizadorPaso3Portlet73PortletKeys.PANTALLA_FAMILIAR;
			} else {
				pantalla = CotizadorPaso3Portlet73PortletKeys.PANTALLA_EMPRESARIAL;
			}
			
			SimpleResponse respuesta = _ServicePaso3.getSecionComision(cotizacion, version, sc, p_usuario, pantalla);

			Gson gson = new Gson();
			String jsonString = gson.toJson(respuesta);
			PrintWriter writer = resourceResponse.getWriter();
			writer.write(jsonString);

		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			PrintWriter writer = resourceResponse.getWriter();
			String jsonString = "{\"code\" : \"5\", \"msg\" : \"Error al consultar la información\" }";
			writer.write(jsonString);
		}
	}
}
