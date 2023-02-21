<%@ include file="./init.jsp" %>
<%@ include file="./modales.jsp"%>

<portlet:resourceURL id="/getSlip" var="getSlip" />
<portlet:resourceURL id="/emisionData" var="getEmisionData" />
<portlet:resourceURL id="/emisionArt492/getemision" var="getEmisionArt492Url" />
<portlet:resourceURL id="/getSeccionComisionUrl" var="getSeccionComisionUrl" />
<portlet:resourceURL id="/saveCaratulaComision" var="saveCaratulaComision" />
<portlet:resourceURL id="/cotizadores/paso3/redirigePasoX" var="redirigeURL" />
<portlet:resourceURL id="/getDocsEmision" var="getDocsEmision" />
<portlet:resourceURL id="/generaEdicionBaja" var="generaEdicionBajaURL" />
<portlet:resourceURL id="/sendMailSuscriptorAgente" var="sendMailSuscriptorAgenteURL" />

<portlet:resourceURL id="/cotizadores/validaAgente" var="validaAgenteURL" cacheability="FULL" />

<fmt:setLocale value="es_MX" />


<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css?v=${versionC}">

<section class="site-wrapper">
	<section id="landing-agentes" class="upper-case-all">
		
		<div class="section-heading">
			<div class="container-fluid">
				<h4 class="title text-left">
<!-- 					<liferay-ui:message key="HabitacionPortlet.cotPaqFamiliar" /> -->
					${tituloCotizador}
				</h4>
			</div>
		</div>
	
		<div class="container-fluid">
		
			<div class="row">
				<div class="col-md-12">
					<ul class="stepper stepper-horizontal container-fluid">
						<li id="step1">
							<a href="javascript:void(0)">
								<span class="circle">1</span> <span class="label"><liferay-ui:message
										key="Paso3Portlet.datCotizacion" /></span>
							</a>
						</li>
						<li id="step2">
							<a href="javascript:void(0)">
								<span class="circle">2</span> <span class="label"><liferay-ui:message
										key="Paso3Portlet.datRiesgoModalidad" /></span>
							</a>
						</li>
						<li id="step3" class="active">
							<a href="javascript:void(0)">
								<span class="circle active_2">3</span> <span class="label active_2"><liferay-ui:message
										key="Paso3Portlet.genSlipSolEmision" /></span>
							</a>
						</li>
						<li id="step4">
							<a href="javascript:void(0)">
								<span class="circle">4</span> <span class="label"><liferay-ui:message key="Paso3Portlet.emision" /></span>
							</a>
						</li>
					</ul>
				</div>
			</div>
			
			<div style="position: relative;">
				<liferay-ui:error key="errorServicio" message="${caratulaResponse.msg}" />
				<liferay-ui:error key="errorConocido" message="${errorMsg}" />
			</div>
			
			<!-- paos 3 -->

			<div class="container-fluid" id="paso3">
				<div class="row divFolioP3">
					<div class="col-md-10">
						<span id="titPoliza" class="ml-5 font-weight-bold"></span>
					</div>
					<div class="col-md-2" style="text-align: right;">
						<div class="md-form form-group">
							<input id="idFolio3" type="text" name="idFolio3" class="form-control" value="${infCotizacion.folio} - ${infCotizacion.version}" disabled>
							<label for="idFolio3"> Folio: </label>
						</div>
					</div>
				</div>
				<div class="row" style="padding: 0 70px">
					<div id="divTbl" class="col-md-12">
						<table class="customTable" style="width: 100%;">
							<!-- <table class="table simple-data-table table-striped table-bordered" style="width:100%;"> -->
							<thead>
								<tr>
									<th>
										<liferay-ui:message key="Paso3Portlet.thEdi" />
									</th>
									<th>
										<liferay-ui:message key="Paso3Portlet.thSumAse" />
									</th>
									<th>
										<liferay-ui:message key="Paso3Portlet.thPrim" />
									</th>
									<th>
										<liferay-ui:message key="Paso3Portlet.thDedu" />
									</th>
								</tr>
							</thead>
							<tbody id="tabPaso3">
								<c:set var="bandera" value=""/>
								<c:forEach items="${caratulaResponse.datosCaratula}" var="opc">
									<c:if test="${bandera != opc.contenedor}"> 
										<c:set var="bandera" value="${opc.contenedor}"/>
									   <tr><th>${bandera}</td><td></td><td></td><td></th></tr>
									</c:if>
									<tr><td>${opc.titulo}</td><td class="number">${opc.sa}</td><td class="number">${opc.prima}</td><td>${opc.deducible}</td></tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
					
					<div id="divTblEndBj" class="col-md-12 d-none">
						<div class="row justify-content-center">
							<div id="titulosEndBj"></div>
							<div id="datosEndBj" class="table-wrapper-scroll-table">
								
							</div>
							<div id="totalEndBj"></div>
						</div>			
					</div>
				</div>
				<div class="row" style="padding: 0 70px;">
					<div class="col-md-3">
						
						<div id="divCederComision" class="row" ${ (Leg492 == 'factura') ? 'hidden' : '' }>
							<div class="col-sm-12">
								<!-- Grid row -->
								<div class="form-row align-items-center">
									<!-- Grid column -->
									<div class="col-11">
										<!-- Material input -->
										<div class="md-form">
											<input type="text" id="txtCederComision" class="form-control">
											<label id="titCederComision" for="txtCederComision">Cesión Art. 41</label>
										</div>
									</div>
									<!-- Grid column -->
	
									<!-- Grid column -->
									<div class="col-1">
										<!-- Material input -->
	
										<div class="md-form input-group mb-3">
											<div class="input-group-prepend">
												<span class="input-group-text md-addon">%</span>
											</div>
	
										</div>
									</div>
									<!-- Grid column -->
	
								</div>
								<!-- Grid row -->
							</div>
							<div class="col-sm-12">
								<button type="button" class="btn btn-pink waves-effect waves-light float-right btn-block" id="btnCederComision"
									name="btnCederComision">
									<liferay-ui:message key="Paso3Portlet.btnCederComision" />
								</button>
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<table class="table-borderless" style="width: 100%;">
							<tbody id="tabPaso3_2">
								<tr><td>Prima Neta:</td><td id="primaNeta" class="number"> <fmt:formatNumber value = "${caratulaResponse.primaNeta}" type = "currency"/> </td></tr>
								<tr><td>Recargo por Pago Fraccionado:</td><td class="number"> <fmt:formatNumber value = "${caratulaResponse.recargo}" type = "currency"/>  </td></tr>
								<tr><td>Gastos de Expedición:</td><td class="number">  <fmt:formatNumber value = "${caratulaResponse.gastos}" type = "currency"/> </td></tr>
								<tr><td>I.V.A.:</td><td class="number">  <fmt:formatNumber value = "${caratulaResponse.iva}" type = "currency"/> </td></tr>
							</tbody>
							<tfoot>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.primTotal" /></b>
									</td>
									<td class="number" id="valPrimTot">
										<b id="tabPaso3_3">
											<fmt:formatNumber value = "${caratulaResponse.total}" type = "currency"/>
										</b>
									</td>
								</tr>
							</tfoot>
						</table>
					</div>
					
					<div class="col-md-3" ${ (botonesCaratula == 1) ? 'hidden' : ''} ${ (Leg492 == 'factura') ? 'hidden' : '' }>
						<div class="row ${dBtns}" id="divPrimaObj">
							<div class="col-sm-12">
								<div class="md-form">
									<c:set var="txtTipoMon" value="${tipoMoneda == 2 ? '(Dólares)' : '(Pesos)'}" />
									<input type="text" id="txtPrimaObj" class="form-control ">
									<label id="titPrimaObj" for="txtPrimaObj">Prima Objetivo: ${txtTipoMon}</label>
								</div>
							</div>
							<div class="col-sm-12">
								<button type="button" class="btn btn-pink waves-effect waves-light float-right btn-block" id="btnRecalcularPrima"
									name="btnRecalcularPrima">
									<liferay-ui:message key="Paso3Portlet.btnRecalcularPrima" />
								</button>
							</div>
						</div>
					</div>
	
				</div>
	
				<div class="row" style="padding:70px">
					
				
					<div class="col-sm-12" ${ (Leg492 == 'factura') ? 'hidden' : '' }>
	
						<c:set var="visibleBtnEmision" value="${permisoFamiliar == false ? 'hidden' : ''}" />
						<button type="button" class="btn btn-pink waves-effect waves-light float-right d-none"
					 		id="btnEmitEndoso" name="btnEmitEndoso" 
					 		onclick="$('#btnFacturaSuscrip').trigger('click')" disabled>
							Emitir endoso
						</button>
						
						<%-- Estos 3 botones son para funcionalidad de suscripcion  --%>
						<button type="button" class="btn btn-pink waves-effect waves-light float-right" id="btnEnvCotiSusAgente" name="btnEnvCotiSusAgente" ${ (botonesCaratula == 2) ? 'disabled' : 'hidden' } >
							Enviar Cotización 
						</button>
						<button type="button" class="btn btn-blue waves-effect waves-light float-right" id="btnNoAcepPropuesta" name="btnNoAcepPropuesta" ${ (botonesCaratula == 1) ? '' : 'hidden'}>
							No aceptar propuesta
						</button>
						<button type="button" class="btn btn-pink waves-effect waves-light float-right" id="btnRecotizar" name="btnRecotizar" ${ (botonesCaratula == 1) ? '' : 'hidden'}>
							Recotizar
						</button>
						
<%-- 						<c:set var="disabledBtnEmision" value="${hiddTipMov == opc.idCatalogoDetalle ? 'selected' : ''}"/> --%>
						<button class="btn btn-pink waves-effect waves-light float-right" id="paso3_emision" ${ (caratulaResponse.estado == 340 || caratulaResponse.estado == 350 || caratulaResponse.estado == 351) ? '' : 'disabled' } ${ visibleBtnEmision } ${ (botonesCaratula == 2) ? 'hidden' : '' }>
							<liferay-ui:message key="Paso3Portlet.btnSolEmi" />
						</button>
						<button type="button" class="btn btn-pink waves-effect waves-light float-right" id="paso3_slip" ${ (botonesCaratula == 1) ? 'hidden' : ''}>
							<liferay-ui:message key="Paso3Portlet.btnGenSli" />
						</button>
						<button type="button" class="btn btn-blue waves-effect waves-light float-right" id="paso3_back">
							<liferay-ui:message key="Paso3Portlet.aReg" />
						</button>
						<button type="button" class="btn btn-blue waves-effect waves-light float-right d-none" id="renovacion_back">
							<liferay-ui:message key="Paso3Portlet.aReg" />
						</button>
						
						<button type="button" class="btn btn-blue waves-effect waves-light float-right d-none" id="paso1_back" onclick="regresaPaso1();	">
							<liferay-ui:message key="Paso3Portlet.aReg" />
						</button>
					</div>
					<div class="col-sm-12" ${ (Leg492 == 'factura') ? '' : 'hidden' }>
						<div class=" col-sm-6"></div>
						<div class="form-check form-check-inline col-sm-2">
							<input type="radio" class="form-check-input" id="chkfactauto" name="rdofactura" value="1" checked="checked">
							<label class="form-check-label" for="chkfactauto">Generar la factura automática</label>
						</div>
	
						<!-- Material inline 2 -->
						<div class="form-check form-check-inline col-sm-2">
							<input type="radio" class="form-check-input" id="chkfactmanual" name="rdofactura" value="2">
							<label class="form-check-label" for="chkfactmanual">Generar la factura manual</label>
						</div>
						<button type="button" class="btn btn-pink waves-effect waves-light float-right" id="btnFacturaSuscrip"
							name="btnFacturaSuscrip" >Emitir</button>
						
					</div>
				</div>
	
			</div>
			
		</div>
	</section>
</section>


<div id="divPdf" hidden="true">
	<a id="aPdf" hidden="true"></a>
</div>
<a id='dwnldLnk' style="display: none;" />
<form id="divInfoAuxiliar" hidden="true">
	<input type="hidden" id="getSlip" value="${getSlip}">
	<input type="hidden" id="txtJSGetEmisionData" value="${getEmisionData}">
	<input type="hidden" id="txtJSGetSeccionComisionUrl" value="${getSeccionComisionUrl}">
	<input type="hidden" id="getCaratulaComision" value="${saveCaratulaComision}">
	<input id="txtIdPerfilUser" type="text" name="txtIdPerfil" class="form-control" hidden="true" value="${ idPerfilUser }">
	<input id="txtMinPrima" type="text" name="txtMinPrima" class="form-control" hidden="true" value="${ minPrima }">
	<input id="txtTpoCambio" type="text" name="txtTpoCambio" class="form-control" hidden="true" value="${ tpoCambio }">
	<input type="hidden" id="txtJSGetDocsEmision" value="${getDocsEmision}">
	<input type="hidden" id="txtEmailUser" value="${mailUser}">
	<input type="hidden" id="dc_moneda" value="${tipoMoneda}">
	<input type="hidden" id="txtEmailAgente" value="${caratulaResponse.email}">
	<input type="hidden" id="txtAuxEnvDoc" name="txtAuxEnvDoc" class="form-control">
</form>
<script>
	var redirigeURL = '${redirigeURL}';
	var getEmisionArt492Url = '${getEmisionArt492Url}';
	var generaEdicionBajaURL = '${generaEdicionBajaURL}';
	var sendMailSuscriptorAgenteURL = '${sendMailSuscriptorAgenteURL}';
</script>

<script src="<%=request.getContextPath()%>/js/objetos.js?v=${versionC}"></script>
<script src="<%=request.getContextPath()%>/js/main.js?v=${versionC}"></script>

<script>
	var infCotiJson = ${infoCotJson};
	var tablaBajasEndoso = '${tablaBajasEndoso}';
	var infP1 = '${infP1}';
	
	var validaAgenteURL = '${validaAgenteURL}';
</script>