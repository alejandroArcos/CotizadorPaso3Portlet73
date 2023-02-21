package com.tokio.pa.cotizadorpaso3portlet73.portlet;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaBajaDatosCaratula;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaBajaDatosGeneral;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaBajaUbicaciones;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaResponse;
import com.tokio.pa.cotizadorModularServices.Bean.CotizadorDataResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoAuxPaso1;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.ListaRegistro;
import com.tokio.pa.cotizadorModularServices.Bean.Registro;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Enum.TipoCotizacion;
import com.tokio.pa.cotizadorModularServices.Exception.CotizadorModularException;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso1;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;
import com.tokio.pa.cotizadorpaso3portlet73.constants.CotizadorPaso3Portlet73PortletKeys;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author urielfloresvaldovinos
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=CotizadorPaso3Portlet73",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + CotizadorPaso3Portlet73PortletKeys.COTIZADORPASO3PORTLET73,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.requires-namespaced-parameters=false",
		"com.liferay.portlet.private-request-attributes=false"
	},
	service = Portlet.class
)
public class CotizadorPaso3Portlet73Portlet extends MVCPortlet {
	
	@Reference
	CotizadorPaso3 _ServicePaso3;
	@Reference
	CotizadorGenerico _ServiceGenerico;
	@Reference
	CotizadorPaso1 _CMServicesP1;

	User user;

	InfoCotizacion infCotizacion = new InfoCotizacion();
	InfoAuxPaso1 infoPaso1 = new InfoAuxPaso1();

	double minPrima = 0;
	double tpoCambio = 0;

	String tblTitulos = "";
	String tblUbicaciones = "";
	String tblTotales = "";

	Gson gson = new Gson();
	CaratulaResponse caratulaResponse = new CaratulaResponse();

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest)
				+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(renderRequest));
		user = (User) renderRequest.getAttribute(WebKeys.USER);
		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

		llenaInfoCotizacion(renderRequest);
		recuperaInfoPaso1(renderRequest);
		validaModoCotizacion(renderRequest);

		String infoCotJson = CotizadorModularUtil.objtoJson(infCotizacion);
		
		llenaTipoCambio();
		llenaCatalogos(renderRequest);
		
		fLlenaDatosTabla(renderRequest);
		if (!isBajaEndoso()) {
			if(Validator.isNotNull(infoPaso1.getSubEstado())){
				if(!infoPaso1.getSubEstado().equals( CotizadorPaso3Portlet73PortletKeys.COTIZADO_SUSCRIPTOR)){		
					validaPrimaminima(renderRequest);	
				}
			}else{
				validaPrimaminima(renderRequest);
			}
			
		}

		
		renderRequest.setAttribute("minPrima", minPrima);
		renderRequest.setAttribute("tpoCambio", tpoCambio);
		renderRequest.setAttribute("infoCotJson", infoCotJson);
		renderRequest.setAttribute("idPerfilUser", idPerfilUser);
		renderRequest.setAttribute("infCotizacion", infCotizacion);
		
		
		renderRequest.setAttribute("mailUser", Base64.getEncoder().encodeToString(user.getEmailAddress().toString().getBytes()));
		caratulaResponse.setEmail(Base64.getEncoder().encodeToString(caratulaResponse.getEmail().toString().getBytes()));
		renderRequest.setAttribute("caratulaResponse", caratulaResponse);
		
		renderRequest.setAttribute("infP1", CotizadorModularUtil.objtoJson( infoPaso1 ));

		renderRequest.setAttribute("botonesCaratula",
				botonesCaratula(infoPaso1.getSubEstado(), idPerfilUser));

		super.render(renderRequest, renderResponse);
	}
	
	
	private void llenaTipoCambio(){
		try {
			tpoCambio = _ServicePaso3.getTipoCambio().getTipoCambio();
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void llenaCatalogos(RenderRequest renderRequest){
		ListaRegistro listaPrimaMinima = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_MIN_PRI,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());

		ListaRegistro listaMotivoRechazo = fGetCatalogos(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_MOTI_RECHAZO,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());

	

		Registro r = null;
		if(isEndoso()){
			if(infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)){
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPEE".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}else{
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPFE".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}			
		}else{
			if(infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)){
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPE".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}else{
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPF".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}
		
		}
		minPrima = Double.parseDouble(r.getValor());
		renderRequest.setAttribute("motivoRechazo", listaMotivoRechazo.getLista());
	}
	

	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla) {
		try {
			ListaRegistro list = _ServiceGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
					p_activo, p_usuario, p_pantalla);
			list.getLista().sort(Comparator.comparing(Registro::getDescripcion));
			return list;
			/* return null; */
		} catch (Exception e) {
			return null;
		}
	}

	private void fLlenaDatosTabla(RenderRequest renderRequest) {
		// caratulaResponse.setCode(3);
		if (caratulaResponse.getCode() == 0) {
			System.out.println("INFO CORRECTA");
		} else {
			System.out.println("INFO INCORRECTA");
			SessionErrors.add(renderRequest, "errorServicio");
		}
	}

	public int botonesCaratula(String subEstado, int idPerfil) {
		if (Validator.isNotNull(subEstado)) {
			if ((idPerfil < 4) && ((subEstado.trim()
					.equals(CotizadorPaso3Portlet73PortletKeys.COTIZADO_SUSCRIPTOR))
					|| (subEstado.trim().equals(CotizadorPaso3Portlet73PortletKeys.RECHAZO_VBA_482)))) {
				return 1;
			}
			if ((idPerfil > 3) && ((subEstado.trim().equals(CotizadorPaso3Portlet73PortletKeys.EDO_SUBGIRO))
					|| (subEstado.trim().equals(CotizadorPaso3Portlet73PortletKeys.EXEDE_LIMITES))
					|| (subEstado.trim().equals(CotizadorPaso3Portlet73PortletKeys.REVIRE_SUSCRIPTOR)))) {
				return 2;
			}
		}
		return 0;
	}

	private void fGetCaratula(RenderRequest renderRequest) {
		try {
			String cur_version = String.valueOf(infCotizacion.getVersion());
			caratulaResponse = _ServicePaso3.getCaratula((int) infCotizacion.getCotizacion(),
					cur_version, user.getScreenName(), infCotizacion.getPantalla());
			// if caratulaResponse.getPrimaNeta()
		} catch (Exception e) {
			// TODO: handle exception
			caratulaResponse = new CaratulaResponse();
		}
	}

	private void llenaInfoCotizacion(RenderRequest renderRequest) {

		try {
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(renderRequest));

			String inf = originalRequest.getParameter("infoCotizacion");

			String nombreCotizador = "";
			if (Validator.isNotNull(inf)) {
				infCotizacion = CotizadorModularUtil.decodeURL(inf);
			} else {
				infCotizacion = new InfoCotizacion();
			}
			
			
//			auxRenovacion();

			System.out.println("-----------------------------------------");
			System.err.println("inf: " + infCotizacion.toString());

			switch (infCotizacion.getTipoCotizacion()) {
				case FAMILIAR:
					infCotizacion.setPantalla("paqueteFamiliar");
					nombreCotizador = CotizadorPaso3Portlet73PortletKeys.TITULO_FAMILIAR;
					break;
				case EMPRESARIAL:
					infCotizacion.setPantalla("moduloEmpresarial");
					nombreCotizador = CotizadorPaso3Portlet73PortletKeys.TITULO_EMPRESARIAL;
					break;
				default:
					infCotizacion.setPantalla("");
					break;
			}
			renderRequest.setAttribute("tituloCotizador", nombreCotizador);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ llenaInfoCotizacion:");
			SessionErrors.add(renderRequest, "errorServicios");
			e.printStackTrace();
		}

	}

	private void validaModoCotizacion(RenderRequest renderRequest) {
		switch (infCotizacion.getModo()) {
			case FACTURA_492:
				renderRequest.setAttribute("Leg492", "factura");
				fGetCaratula(renderRequest);
				break;
			case ALTA_ENDOSO:
				renderRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(renderRequest);
				break;
			case EDITAR_ALTA_ENDOSO:
				renderRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(renderRequest);
				break;
			case BAJA_ENDOSO:
				renderRequest.setAttribute("dBtns", "d-none");
				caratulaBajaEnsos(renderRequest);
				break;
			case EDITAR_BAJA_ENDOSO:
				renderRequest.setAttribute("dBtns", "d-none");
				caratulaBajaEnsos(renderRequest);
				break;
			case RENOVACION_AUTOMATICA:
				renderRequest.setAttribute("dBtns", "d-none");
				actualizaInfoRenovacion();
				recuperaInfoPaso1(renderRequest);
				fGetCaratula(renderRequest);
				break;
			case EDITAR_RENOVACION_AUTOMATICA:
				renderRequest.setAttribute("dBtns", "d-none");
				infCotizacion.setModo(ModoCotizacion.RENOVACION_AUTOMATICA);
				fGetCaratula(renderRequest);
			case CONSULTAR_RENOVACION_AUTOMATICA:
				renderRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(renderRequest);
			default:
				fGetCaratula(renderRequest);
				
				break;
		}
			
	}
	
	private void actualizaInfoRenovacion(){
		try {
			SimpleResponse respuesta = _ServicePaso3.GuardarCotizacionRenovacion((int)infCotizacion.getCotizacion(), 
					infCotizacion.getVersion(), user.getScreenName(), infCotizacion.getPantalla());
			infCotizacion.setCotizacion(respuesta.getCotizacion());
			infCotizacion.setFolio(Long.parseLong((respuesta.getFolio())));
			infCotizacion.setVersion(respuesta.getVersion());
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void recuperaInfoPaso1(RenderRequest renderRequest) {
		try {
			final PortletSession psession = renderRequest.getPortletSession();
			
			Gson gson = new Gson();
			String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
					+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion()
					+ "_DATOSP1";
			

			String datosP1 = "";
			if(isRenovacion()){
				datosP1 = infop1Aux( renderRequest);
			}else{
				datosP1 =  (String) psession.getAttribute(auxNombre,
					PortletSession.APPLICATION_SCOPE);

			}
					
					
			

			if (Validator.isNotNull(datosP1)) {
				System.err.println("SESION RESPONSE: " + datosP1);

				infoPaso1 = gson.fromJson(datosP1, InfoAuxPaso1.class);
				renderRequest.setAttribute("tipoMoneda", infoPaso1.getMonedaSeleccionada());
			} else {
				System.err.println("CAMBIAR VALOR COTIZACION");
				infCotizacion.setModo(ModoCotizacion.ERROR);
			}

		} catch (Exception e) {
			// TODO: handle exception
			infoPaso1 = new InfoAuxPaso1();
			e.printStackTrace();
			SessionErrors.add(renderRequest, "errorConocido");
			renderRequest.setAttribute("errorMsg", "Error al cargar las ubicaciones");
			SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}

	private void validaPrimaminima(RenderRequest renderRequest) {
		System.out.println("caratulaResponse.getPrimaNeta(): " + caratulaResponse.getPrimaNeta());
		System.out.println("minPrima: " + minPrima);
		
		double auxPrimaMin = 0;

		String tipoMoneda = infoPaso1.getMonedaSeleccionada();
		if(tipoMoneda.equals("1")){
			auxPrimaMin = minPrima;
		}else{
			auxPrimaMin = minPrima / tpoCambio;
		}
		
		if (caratulaResponse.getPrimaNeta() < auxPrimaMin) {
			System.out.println("APLICAR PRIMA MINIMA");
			String cur_version = String.valueOf(infCotizacion.getVersion());

			try {
				caratulaResponse = _ServicePaso3.GetCaratulaPrimaObjetivo(
						(int) infCotizacion.getCotizacion(), cur_version, auxPrimaMin,
						user.getScreenName(), infCotizacion.getPantalla());
				SessionErrors.add(renderRequest, "errorConocido");
				renderRequest.setAttribute("errorMsg",
						"Se ha aplicado la prima mínima del producto");
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private boolean isEndoso() {
		switch (infCotizacion.getModo()) {
			case ALTA_ENDOSO:
				return true;
			case EDITAR_ALTA_ENDOSO:
				return true;
			case BAJA_ENDOSO:
				return true;
			case EDITAR_BAJA_ENDOSO:
				return true;
			default:
				return false;
		}

	}

	private boolean isBajaEndoso() {
		switch (infCotizacion.getModo()) {
			case BAJA_ENDOSO:
				return true;
			case EDITAR_BAJA_ENDOSO:
				return true;
			default:
				return false;
		}

	}

	void caratulaBajaEnsos(RenderRequest renderRequest) {

		try {
			/**
			 * Consumo el servicio
			 */
			CaratulaBajaDatosGeneral bs = _ServicePaso3.GetCaratulaEndoso(
					infCotizacion.getCotizacion() + "", infCotizacion.getVersion(),
					user.getScreenName(), infCotizacion.getPantalla());

			/**
			 * ordeno por ubicaciones
			 */
			for (CaratulaBajaDatosCaratula dc : bs.getDatosCaratula()) {
				dc.getUbicaciones()
						.sort(Comparator.comparing(CaratulaBajaUbicaciones::getUbicacion));
			}
			
			
			/**
			 * hacemos un map por contenedor llave principal
			 */
			Map<String, List<CaratulaBajaDatosCaratula>> datosCara = bs.getDatosCaratula().stream()
					.collect(Collectors.groupingBy(CaratulaBajaDatosCaratula::getContenedor));
			
			int totUbi = bs.getDatosCaratula().get(0).getUbicaciones().size();

	        Map<Integer, Double> totUbica = new HashMap<>(); 
	        
	        for (CaratulaBajaDatosCaratula dca : bs.getDatosCaratula()) {
				for (CaratulaBajaUbicaciones ubic : dca.getUbicaciones()) {
					if(!totUbica.containsKey(ubic.getUbicacion())){
						totUbica.put(ubic.getUbicacion(), 0.0);
					}
					String auxPrima = ubic.getPrima().replace("$", "").replace(",", "");
					try {
						double nv = totUbica.get(ubic.getUbicacion()) + Double.parseDouble(auxPrima);
						totUbica.put(ubic.getUbicacion(), nv);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			
	    
	        
	        for (Entry<Integer, Double> entry : totUbica.entrySet()) {
	            System.out.println(entry.getKey() + ":" + entry.getValue().toString());
	        }
	        
			String tablaBajasEndoso = "<table id=\"tblendbaja\" class=\"customTable w-100\" > <thead> <tr>";

			tablaBajasEndoso += generatitulos(bs.getDatosCaratula().get(0).getUbicaciones());

			for (Map.Entry<String, List<CaratulaBajaDatosCaratula>> entry : datosCara.entrySet()) {
				System.out.println(entry.getKey());
				tablaBajasEndoso += "<tr><td class=\"text-center font-weight-bold tb1\">" + entry.getKey() + "</td>";
				for (int i = 0; i <= totUbi; i++) {
					String adCls = (i < totUbi) ? "tb2" : "tb3";
					tablaBajasEndoso += "<td class=\" " + adCls + "\">&nbsp;</td>";
				}
				tablaBajasEndoso += "</tr>";
				tablaBajasEndoso += generaInfoTbl(entry.getValue());
				tablaBajasEndoso += "</tr>";
			}

			tablaBajasEndoso += "</table>";
			System.out.println(tablaBajasEndoso);
			renderRequest.setAttribute("tablaBajasEndoso", tablaBajasEndoso);
			
			System.out.println(bs.getPrimaNeta());
			caratulaResponse.setPrimaNeta((float) bs.getPrimaNeta());
			caratulaResponse.setRecargo((float) bs.getRecargo());
			caratulaResponse.setGastos((float) bs.getGastos());
			caratulaResponse.setIva((float) bs.getIva());
			caratulaResponse.setTotal((float) bs.getTotal());
			caratulaResponse.setEmail(bs.getEmail());
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	String generatitulos(List<CaratulaBajaUbicaciones> ubicaciones) {
		String titulos = "<th class=\"tb1\"> Prima a devolver </th>";
		for (CaratulaBajaUbicaciones caratulaBajaUbicaciones : ubicaciones) {
			titulos += "<th class=\"tb2 \"> Ubicación " + caratulaBajaUbicaciones.getUbicacion()
					+ "</th>";
		}
		titulos += "<th class=\"tb3\"> Totales </th> </tr> </thead><tbody>";
		return titulos;
	}

	String generaInfoTbl(List<CaratulaBajaDatosCaratula> dtsCaratula) {
		String datos = "";
		for (CaratulaBajaDatosCaratula cbdc : dtsCaratula) {
			datos += "<tr ><td class=\"tb1\">" + cbdc.getTitulo() + "</td>";
			float total = 0;
			for (CaratulaBajaUbicaciones ub : cbdc.getUbicaciones()) {
				datos += "<td class=\"tb2 \">" + ub.getPrima() + "</td>";
				String auxPrima = ub.getPrima().replace("$", "").replace(",", "");
				try {
					total += Float.parseFloat(auxPrima);
				} catch (NumberFormatException e) {
					// TODO: handle exception
				}

			}
			DecimalFormat formatter = new DecimalFormat("#,###.##");
			if (total < 0) {

				datos += "<td class=\"tb3\"> -$" + formatter.format((total * (-1))) + "</td></tr>";
			} else {
				datos += "<td class=\"tb3\"> $" + formatter.format(total) + "</td></tr>";
			}
		}
		return datos;
	}
	
	
	
	private boolean isRenovacion(){
		switch (infCotizacion.getModo()) {
			case RENOVACION_AUTOMATICA:
				return true;
			case EDITAR_RENOVACION_AUTOMATICA:
				return true;
			case CONSULTAR_RENOVACION_AUTOMATICA:
				return true;
		
			default:
				return false;
		}
	}
	
	
	private String infop1Aux(RenderRequest renderRequest){
		try {
			final PortletSession psession = renderRequest.getPortletSession();
			CotizadorDataResponse respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
					infCotizacion.getCotizacion(), infCotizacion.getVersion(),
					user.getScreenName(), infCotizacion.getPantalla());
			int moneda = respuesta.getDatosCotizacion().getMoneda();
			System.out.println("moneda :" + moneda);
			
			InfoAuxPaso1 in1 = new InfoAuxPaso1();
			in1.setMonedaSeleccionada(moneda + "");
			
			
			String nombreDatosGenerales = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() +
					"_C=" + infCotizacion.getCotizacion() +
					"_V=" + infCotizacion.getVersion() +
					"_DATOSP1";
			
			String paso1 = CotizadorModularUtil.objtoJson(in1);
			psession.setAttribute(nombreDatosGenerales, paso1, PortletSession.APPLICATION_SCOPE);
			return paso1;
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}