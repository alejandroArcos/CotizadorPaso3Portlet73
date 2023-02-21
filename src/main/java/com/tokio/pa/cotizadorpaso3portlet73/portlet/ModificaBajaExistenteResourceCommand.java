/**
 * 
 */
package com.tokio.pa.cotizadorpaso3portlet73.portlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.tokio.pa.cotizadorpaso3portlet73.constants.CotizadorPaso3Portlet73PortletKeys;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;

import java.io.PrintWriter;

import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author jonathanfviverosmoreno
 *
 */
@Component(
		immediate = true, property = { 
				"javax.portlet.name=" + CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73,
					"mvc.command.name=/generaEdicionBaja" }, service = MVCResourceCommand.class)
public class ModificaBajaExistenteResourceCommand extends BaseMVCResourceCommand {
	
	
	/* (non-Javadoc)
	 * @see com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand#doServeResource(javax.portlet.ResourceRequest, javax.portlet.ResourceResponse)
	 */
	@Override
	protected void doServeResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {
		// TODO Auto-generated method stub
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

		final PortletSession psession = resourceRequest.getPortletSession();
		Gson gson = new Gson();
		
		
		String infoCot = ParamUtil.getString(resourceRequest, "infoCot");
		
		InfoCotizacion infCot = gson.fromJson(infoCot, InfoCotizacion.class);
		
		String nombreVarSession = "LIFERAY_SHARED_F=" + infCot.getFolio() +
				"_C=" + infCot.getCotizacion() +
				"_V=" + infCot.getVersion() +
				"_EDICION_BAJA";
		
		psession.setAttribute(nombreVarSession, true, PortletSession.APPLICATION_SCOPE);
		
	}

}
