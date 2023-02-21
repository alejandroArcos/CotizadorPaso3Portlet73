<portlet:resourceURL id="/paso3/rechazoCotizacion" var="txtRechazaCotizacionURL" />
<portlet:resourceURL id="/sendMailSuscriptorAgente" var="sendMailSuscriptorAgenteUrl" />
<portlet:resourceURL id="/sendMailAgenteSuscriptor" var="sendMailAgenteSuscriptorUrl" />

<style>
#conjuntoFile::-webkit-scrollbar {
	width: 20px;
	background-color: #F5F5F5;
}

#conjuntoFile::-webkit-scrollbar-track {
	-webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
	border-radius: 10px;
	background-color: #F5F5F5;
}

#conjuntoFile::-webkit-scrollbar-thumb {
	border-radius: 10px;
	-webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, .3);
	background-color: #555;
}
</style>

<!-- Modal Rechazar propuesta -->
<div class="modal" id="modalRechazarProp" tabindex="-1" role="dialog"
	aria-labelledby="modalRechazarPropLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg modal-dialog-centered"
		role="document">
		<div class="modal-content">
			<div class="modal-header">
				<h5 class="modal-title" id="modalRechazarPropLabel">
					<liferay-ui:message key="Paso3Portlet.modalRechazaPropuesta" />
				</h5>
				<button type="button" class="close" data-dismiss="modal"
					aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<!--Body-->
			<div class="modal-body">

				<div class="row">
					<div class="col-md-12" style="text-align: center;">
						<span id="textRechazo">
							<liferay-ui:message key="Paso3Portlet.modalTxtRechazo" />
						</span>
					</div>

					<div class="col-md-12">
						<div class="md-form form-group">
							<select name="selectMotRechazo" id="selectMotRechazo "
								class="mdb-select form-control-sel">
								<option value="-1" selected><liferay-ui:message key="Paso3Portlet.selectOpDefoult" /></option>
								<c:forEach items="${motivoRechazo}" var="option">
									<option value="${option.idCatalogoDetalle}">${option.valor}</option>
								</c:forEach>
							</select> <label for="selectMotRechazo"> <liferay-ui:message key="Paso3Portlet.modalMotivo" /> </label>
						</div>
					</div>
					<div class="col-12">
						<div class="md-form">
							<textarea id="comentariosRechazarProp"
								name="comentariosRechazarProp" class="md-textarea form-control"
								rows="3" maxlength="1000" style="text-transform: uppercase;"></textarea>
							<label for="comentariosDosSuscrip"> <liferay-ui:message key="Paso3Portlet.modalComentarios" /> </label>
						</div>
					</div>
				</div>
			</div>

			<!--Footer-->
			<div class="modal-footer justify-content-center">
				<div class="row">
					<div class="col-md-6">
						<button class="btn btn-blue waves-effect waves-light"
							data-dismiss="modal"> <liferay-ui:message key="Paso3Portlet.cancelar" /> </button>
					</div>
					<div class="col-md-6">
						<button class="btn btn-pink waves-effect waves-light"
							id="btnEnvRecha"> <liferay-ui:message key="Paso3Portlet.enviar" /> </button>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- END  Modal Rechazar propuesta -->

<!-- Modal Poliza Generada -->
<div class="modal" id="modalGenerarPoliza" tabindex="-1" role="dialog" aria-labelledby="cerrarPolizaLabel"
	aria-hidden="true">
	<div class="modal-dialog modal-lg modal-dialog-centered anchoModal" role="document">
		<div class="modal-content">
			<div class="modal-header green">
				<h5 class="modal-title text-white" id="cerrarPolizaLabel">
					<span id="titModalEmisionp3"></span>
				</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<!--Body -->
			<div class="modal-body">

				<div class="row">
					<div class="col-12">
						<table class="table altoTbPoliza tablaPoliza">
							<tbody>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaNumeroPoliza" /></b> <span
											id="txtModalPolizaNumeroPoliza"> </span>
									</td>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaCertificado" /></b> <span
											id="txtModalPolizaCertificado"> </span>
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaAsegurado" /></b> <span id="txtModalPolizaAsegurado">
										</span>
									</td>
								</tr>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaVigencia" /></b> <span id="txtModalPolizaVigenciaDe">
										</span> <b> al </b> <span id="txtModalPolizaVigenciaAl"> </span>
									</td>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaTotalUbicaciones" /></b> <span
											id="txtModalPolizaTotalUbicaciones"> </span>
									</td>
								</tr>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaMoneda" /></b> <span id="txtModalPolizaMoneda">
										</span>
									</td>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaFormaPago" /></b> <span id="txtModalPolizaFormaPago">
										</span>
									</td>
								</tr>
							</tbody>
						</table>
						<table class="table altoTbPoliza table-borderless colPoliza20 tablaPoliza">
							<tbody>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaPrimaNeta" /></b>
									</td>
									<td class="moneda celdaPoliza">$</td>
									<td class="colPoliza10">
										<span id="txtModalPolizaPrimaNeta"> </span>
									</td>
									<td></td>
									<td></td>
								</tr>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaRecargoPago" /></b>
									</td>
									<td class="moneda celdaPoliza">$</td>
									<td class="colPoliza10">
										<span id="txtModalPolizaRecargoPago"> </span>
									</td>
									<td></td>
									<td></td>
								</tr>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaGastosExpedicion" /></b>
									</td>
									<td class="moneda celdaPoliza">$</td>
									<td class="colPoliza10">
										<span id="txtModalPolizaGastosExpedicion"> </span>
									</td>
									<td></td>
									<td></td>
								</tr>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaIva" /></b>
									</td>
									<td class="moneda celdaPoliza">$</td>
									<td class="colPoliza10">
										<span id="txtModalPolizaIva"> </span>
									</td>
									<td></td>
									<td></td>
								</tr>
								<tr>
									<td>
										<b><liferay-ui:message key="Paso3Portlet.modalPolizaTotal" /></b>
									</td>
									<td class="moneda celdaPoliza">$</td>
									<td class="colPoliza10">
										<span id="txtModalPolizaTotal"> </span>
									</td>
									<td></td>
									<td></td>
								</tr>
							</tbody>
						</table>
					</div>
				<input type="hidden" id="txtModalPolizaAgente" class="d-none">
				</div>
				<div class="row">
					<div class="col-7">
						<div class="row">
							<div class="col-12">
								<div class="md-form">
									<input type="email" name="modalPolizaEnviarCorreo" id="modalPolizaEnviarCorreo" class="form-control">
									<label for="modalPolizaEnviarCorreo">
										<liferay-ui:message key="Paso3Portlet.modalPolizaEnviarCorreo" />
									</label>
								</div>
							</div>
							<div class="col-12">
								<button class="btn btn-pink waves-effect waves-light float-right" id="btnAgregaCorreoPoliza" disabled>
									<liferay-ui:message key="Paso3Portlet.modalPolizaBtnAgregarCorreo" />
								</button>
							</div>
							<div class="col-12 table-wrapper-scroll-y">
								<table class="altoTbArchivos table table-striped" id="tablaArchivosPoliza" style="width: 100%;">
									<thead>
										<tr>
											<th scope="col" class="th-sm">
												<div class="form-check">
													<input type="checkbox" class="form-check-input selectCheckImput" id="seleccionarTodosArchivos" checked>
													<label class="form-check-label" for="seleccionarTodosArchivos">
														<liferay-ui:message key="Paso3Portlet.modalPolizaChekTodos" />
													</label>
												</div>
											</th>
											<th scope="col" class="th-sm">
												<liferay-ui:message key="Paso3Portlet.modalPolizaArchivoTit" />
											</th>
											<th scope="col" class="th-sm">
												<liferay-ui:message key="Paso3Portlet.modalPolizaTipoTit" />
											</th>
										</tr>
									</thead>
									<tbody class="bodyArchivos">

									</tbody>
								</table>
							</div>
						</div>
					</div>
					<div class="col-5">
						<div class="alert alert-warning msjActivarBtnEnviar" role="alert" hidden="true">
							<liferay-ui:message key="Paso3Portlet.modalPolizaMsjEnviar" />
						</div>
						<ul id="listaCorreos" class="listaCorreos scrollbarLiMod scrollbarLiMod-primary">
						</ul>
					</div>
				</div>
				<div class="row">
					<div class="col-6">
						<button class="btn btn-pink waves-effect waves-light " id="btnDescargarArchivos">
							<liferay-ui:message key="Paso3Portlet.modalPolizaBtnDescargarArchivos" />
						</button>
						<button class="btn btn-pink waves-effect waves-light" id="polizaBtnEnviar">
							<liferay-ui:message key="Paso3Portlet.modalPolizaBtnEnviar" />
						</button>
					</div>
					<div class="col-6">
						<button class="btn btn-blue waves-effect waves-light float-right" id="polizaBtnAceptar" data-dismiss="modal">
							<liferay-ui:message key="Paso3Portlet.modalPolizaBtnAceptar" />
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- END Modal Poliza Generada -->

<!-- Modal subir archivos paso 2  -->
<div class="modal" id="fileModal" tabindex="-1" role="dialog"
	aria-labelledby="fileModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg" role="document">
		<div class="modal-content">
			<div class="fileErrorPopup"></div>
			<div class="modal-header">
				<h5 class="modal-title" id="fileModalLabel">
					<liferay-ui:message key="Paso3Portlet.modalRequiereInfoAdicional" />
				</h5>
			</div>
			<div class="modal-body">

				<div class="row">
					<div class="col-12">
						<span> <liferay-ui:message key="Paso3Portlet.modalArchivosPermitidos" /> </span>
						<div class="md-form">
							<div class="file-field">
								<div class="btn btn-blue btn-rounded btn-sm float-left">
									<span>
										<i class="fas fa-upload mr-2" aria-hidden="true"></i>
										<liferay-ui:message key="Paso3Portlet.modalSelArchivo" />
									</span>
									<input id="docAgenSusc" type="file" multiple="multiple" data-file_types="pdf|doc|docx|xls|xlsx">
								</div>
								<input id="infDocSuc" class="form-control" type="text"
									placeholder="Archivos" disabled>
							</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-12">
						<div class="md-form">
							<textarea id="comentariosDosSuscrip" name="comentariosDosSuscrip"
								class="md-textarea form-control" rows="3" maxlength="1000"
								style="text-transform: uppercase;"></textarea>
							<label for="comentariosDosSuscrip"> <liferay-ui:message key="Paso3Portlet.modalComentarios" /> </label>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button class="btn btn-pink waves-effect waves-light" id="btnSuscripEnvSus">
					<liferay-ui:message key="Paso3Portlet.modalEnviarSuscriptor" />
				</button>
				<button class="btn btn-pink waves-effect waves-light" id="btnSuscripEnvSus2" hidden>
					<liferay-ui:message key="Paso3Portlet.modalEnviarSuscriptor" />
				</button>
			</div>
		</div>
	</div>
</div>
<!-- end Modal subir archivos paso 2  -->

<!-- Modal Bloqueo Agentes -->
<div class="modal" id="modalBloqueoAgente" tabindex="-1" role="dialog" aria-labelledby="modalBloqueoAgenteLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg modal-dialog-centered" role="document">
		<div class="modal-content">
			<div class="modal-header orange">
				<h5 class="modal-title text-black-50" id="modalBloqueoAgenteLabel">
					<i class="fas fa-exclamation-triangle"></i> Restricción para Agente
				</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>

			<!--Body-->
			<div class="modal-body">
				<div class="row">
					<div class="col-12">
						El agente seleccionado para esta cotización debe actualizar su expediente de Agente por lo que no es posible emitir esta cotización.   Dudas o comentarios, favor de escribir a enterate@tokiomarine.com.mx.					</div>
					</div>
			</div>

			<!--Footer-->
			<div class="modal-footer justify-content-center">
				<div class="row">
					<div class="col-md-6">
						<button class="btn btn-pink waves-effect waves-light" data-dismiss="modal" id="btncpsusc">Entendido</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- END Modal Bloqueo Agentes -->

<input type="hidden" id="txtRechazaCotizacionURL" name="txtRechazaCotizacionURL" class="form-control" value="${txtRechazaCotizacionURL}">
<input type="hidden" id="txtSendMailSuscriptorAgente" value="${sendMailSuscriptorAgenteUrl}">
<input type="hidden" id="txtSendMailAgenteSuscriptor" value="${sendMailAgenteSuscriptorUrl}">