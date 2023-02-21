$(document).ready(function() {
	console.log("READY PASO 3");
	validaErrorCotizacion();
	compruebaBqOri();
	validaModo();
	setModaltitulo();
	setBase64();
});

function validaErrorCotizacion(){
	console.log("modo cotizacion: " + infCotiJson.modo);
	if(infCotiJson.modo == modo.ERROR.toString()){
		console.log("REDIRECCIONAR AL PASO 1");
		infCotiJson.modo = modo.EDICION;
		regresaPaso1();
	}
}

function setModaltitulo(){
	if(infCotiJson.modo.includes("ENDOSO")){
		$("#titModalEmisionp3").text("El endoso se ha generado satisfactoriamente");
	}else{
		$("#titModalEmisionp3").text("Póliza generada exitosamente");
	}
}

function regresaPaso1(){
	showLoader();
	actualizainfoCot();
	var strInfCotiJson = JSON.stringify(infCotiJson); 
	console.log("infCotiJson: ");
	console.log(strInfCotiJson);
	$.post( redirigeURL, {
		/*infoCot : JSON.stringify( infoCotJson ),*/
		infoCot : JSON.stringify(infCotiJson),
		paso : seleccionaVentana()
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			window.location.href = response.msg;
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
}
/*********************************************************************************************************************/
/*****************************************************PASO 3 POST*****************************************************/
$("#paso3_slip").click(function(e) {
    resetSession();
    showLoader();
    e.preventDefault();
    $.post($("#getSlip").val(), {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version
    }).done(function(data) {

        var respuestaJson = JSON.parse(data);
        if (respuestaJson.code == 0) {
            var buffer = new Uint8Array(respuestaJson.documento);
            var blob = new Blob([buffer], { type: "application/pdf" });
            $("#aPdf").attr("href", window.URL.createObjectURL(blob));
            var link = document.getElementById("aPdf");
            link.download = respuestaJson.nombre + ".pdf";
            link.click();
            validaBotonEmision(respuestaJson.estado);
            $("#btnEnvCotiSusAgente").prop("disabled", false);
            $("#btnEmitEndoso").prop("disabled", false);
            hideLoader();
        } else {
            /*agregaAlertError("Mensaje: " + respuestaJson.msg);*/
        	showMessageError(".navbar", "Mensaje: " + respuestaJson.msg, 1);
            hideLoader();
        }
    });
});

$('#btnFacturaSuscrip').click(function() {
    var idFac = ($('#chkfactauto').is(':checked')) ? 1 : 0;
    $('#txtBtnEmiteFactu').val(idFac);
    showLoader();

    $.post(getEmisionArt492Url, {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version,
        factura: idFac,
        cotizador: seleccionaVentana()
    }).done(function(data) {
        var response = jQuery.parseJSON(data);
        console.log('aquiiii');
        console.log(response);
        if (response.code == 0) {
            llenaInfoModalPoliza(response);
            $('#modalGenerarPoliza').modal({
                show: true
            });
        } else if (response.code == 4) {
            llenaInfoModalPoliza(response);
            $('#modalGenerarPoliza').modal({
                show: true
            });
            showMessageError('#modalGenerarPoliza', (response.msg), 0);
        } else {
            showMessageError('.navbar', (response.msg), 0);
        }
    }).always(function() {
        hideLoader();
    });
});

async function adjuntaArchivos() {

    var url = new URL(window.location.href);
    var data = new FormData();
    var auxiliarDoc = '{';

    $.each($('#docAgenSusc')[0].files, function(i, file) {
        data.append('file-' + i, file);
        var nomAux = file.name.split('.');
        if (i == 0) {
            auxiliarDoc += '\"file-' + i + '\" : {';
        } else {
            auxiliarDoc += ', \"file-' + i + '\" : {';
        }
        auxiliarDoc += '\"nom\" : \"' + nomAux[0] + '\",';
        auxiliarDoc += '\"ext\" : \"' + nomAux[1] + '\"}';
    });
    auxiliarDoc += '}';

    data.append('isRevire', $('#txtAuxEnvDoc').val());
    data.append('auxiliarDoc', auxiliarDoc);
    data.append('comentarios', $('#comentariosDosSuscrip').val());
    data.append('folio', infCotiJson.folio);
    data.append('cotizacion', infCotiJson.cotizacion);
    data.append('version', infCotiJson.version);
    data.append('modo', infCotiJson.modo);
    data.append('url', url.origin + url.pathname);
    data.append('totArchivos', $('#docAgenSusc')[0].files.length);
    data.append('tipoCotizacion', infCotiJson.tipoCotizacion);

    $.ajax({
        url: $('#txtSendMailAgenteSuscriptor').val(),
        data: data,
        processData: false,
        contentType: false,
        type: 'POST',
        success: function(data) {
            if (data != "") {
                var response = jQuery.parseJSON(data);
                if (response.codigo > 0) {
                    $('#fileModal').modal('hide');
                    hideLoader();
                    showMessageError('.navbar', response.error, 0);
                } else {
                	goToHome();
                }
            } else {
            	goToHome();
            }
        }
    });
}

$('#modalGenerarPoliza').on('hidden.bs.modal', function() {
    window.scrollTo(0, 0);
    goToHome();
    /*goToPage(seleccionaVentana());*/
    /*var url = new URL(window.location.href);
    window.location.href = url.origin + url.pathname;*/
});
/*****************************************************PASO 3 POST*****************************************************/
/*****************************************************PASO 3 EMISION**************************************************/
$("#paso3_emision").on('click', function(e) {
    consumeEmisionData();
});

$('#btnNoAcepPropuesta').click(function() {
    $('#modalRechazarProp').modal('show');
});

$('#btnEnvRecha').click(function(e) {
    showLoader();
    e.preventDefault();
    var errores = false;
    errores = (noSelect($('#mdlRechOp')) ? true : errores);
    errores = (valIsNullOrEmpty($('#comentariosRechazarProp').val().trim()) ? true : errores);
    if (errores) {
        showMessageError('#modalRechazarProp .modal-header', 'Los campos son obligatorios', 0);
        hideLoader();
    } else {
        showLoader();
        var url = new URL(window.location.href);
        var motivo = $('#modalRechazarProp .modal-body select').val();
        $.post($('#txtRechazaCotizacionURL').val(), {
            cotizacion: infCotiJson.cotizacion,
            version: infCotiJson.version,
            motivoRechazo: motivo,
            motivo: $('#comentariosRechazarProp').val()
        }).done(function() {
        	goToHome();
        });
    }
});
/*****************************************************PASO 3 EMISION**************************************************/
/*********************************************************************************************************************/
/***************************************************Funciones Botones*************************************************/
$('#btnRecotizar').click(function() {
    $('#btnSuscripEnvSus2').removeAttr('hidden');
    $('#btnSuscripEnvSus').prop('hidden', true);
    $('#fileModal').modal('show');
});

$('#btnSuscripEnvSus').click(function() {
    showLoader();
    $('#comentariosDosSuscrip').trigger('keyup');
    $('#txtAuxEnvDoc').val('0');
    adjuntaArchivos();
});

$('#btnSuscripEnvSus2').click(function() {
    showLoader();
    $('#comentariosDosSuscrip').trigger('keyup');

    if ($('#docAgenSusc')[0].files.length == 0 && $('#comentariosDosSuscrip').val().trim().length == 0) {
        showMessageError('#fileModal .modal-header', 'Agregar documentos y/o comentarios', 0);
        hideLoader();
    } else {
        $('#txtAuxEnvDoc').val('1');
        adjuntaArchivos();
    }
});

$('#btnCederComision').click(function(e) {
    try {
        showLoader();
        if (valIsNullOrEmpty($('#txtCederComision').val())) {
            showMessageError('.navbar', 'Sin comisiòn ', 0);
        } else {
            $.post($('#txtJSGetSeccionComisionUrl').val(), {
                seccomi: $('#txtCederComision').val(),
                tipoCoti : infCotiJson.tipoCotizacion.toString(),
                cotizacion: infCotiJson.cotizacion,
                version: infCotiJson.version
            }).done(function(data) {
                sessionExtend();
                var respuestaJson = JSON.parse(data);
                if (respuestaJson.code == 0) {
                    showMessageSuccess('.navbar', 'Información actualizada correctamente', 0);
                } else {
                    showMessageError('.navbar', respuestaJson.msg, 0);
                }
            }).fail(function() {
                showMessageError('.navbar', 'Error al consultar la información', 0);
                hideLoader();
            });
        }
        hideLoader();
    } catch (err) {

        hideLoader();
        showMessageError('.navbar', 'Error al consultar la información', 0);
    }
});

$('#btnRecalcularPrima').click(function(e) {
    try {
        showLoader();
        var primaNeta = parseFloat(valIsNullOrEmpty($('#primaNeta').text()) ?
            0 : $('#primaNeta').text().replace(/[$,]/g, ''));
        var primaObj = parseFloat(valIsNullOrEmpty($('#txtPrimaObj').val()) ?
            0 : $('#txtPrimaObj').val().replace(/[$,]/g, ''));
        var idPerfil = parseInt($('#txtIdPerfilUser').val());
        var minPrima = parseFloat($('#txtMinPrima').val());
        var cambioDoll = parseFloat($('#txtTpoCambio').val());
        /*var tipoMonSelect = parseInt($('#dc_moneda option:selected').val());   CAMBIAR POR NUEVA VARIABLE */
        var tipoMonSelect = parseInt($('#dc_moneda').val());
        if (tipoMonSelect != 1) {
            minPrima = minPrima / cambioDoll;
        }

        var nuevaCaratula = true;
        if (idPerfil != 1) {
            if (primaObj < primaNeta) {
                nuevaCaratula = false;
                showMessageError('.navbar', 'La prima capturada no puede ser menor a ' + generaFormatoNumerico(primaNeta, true, true, false), 0);
            }
        }
        if (primaObj < minPrima) {
            nuevaCaratula = false;
            showMessageError('.navbar', 'La prima minima para el Cotizador '+ infCotiJson.tipoCotizacion +' es ' + generaFormatoNumerico(minPrima, true, true, false), 1);
        }
        if (nuevaCaratula) {
            $.post($("#getCaratulaComision").val(), {
                cotizacion: infCotiJson.cotizacion,
                tipoCoti : infCotiJson.tipoCotizacion.toString(),
                version: infCotiJson.version,
                comision: primaObj
            }).done(function(data) {


                var respuestaJson = JSON.parse(data);
                if (respuestaJson.code == 0) {
                    $('#txtEmailAgente').val(respuestaJson.email);
                    var band = null;
                    $('#tabPaso3').html("");
                    $.each(respuestaJson.datosCaratula, function(k, valCaratula) {
                        if (!(valCaratula.contenedor == band)) {
                            band = valCaratula.contenedor;

                            $('#tabPaso3').append("<tr><th>" + band + "</td><td></td><td></td><td></th></tr>");
                        }
                        $('#tabPaso3').append("<tr><td>" + valCaratula.titulo + "</td><td class=\"number\">" + valCaratula.sa + "</td><td class=\"number\">" + valCaratula.prima + "</td><td>" + valCaratula.deducible + "</td></tr>");
                    });
                    $('#tabPaso3_2').html("<tr><td>Prima Neta:</td><td id='primaNeta' class=\"number\">" + setCoinFormat('' + respuestaJson.primaNeta) + "</td></tr>");
                    $('#tabPaso3_2').append("<tr><td>Recargo por Pago Fraccionado:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.recargo) + "</td></tr>");
                    $('#tabPaso3_2').append("<tr><td>Gastos de Expedición:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.gastos) + "</td></tr>");
                    $('#tabPaso3_2').append("<tr><td>I.V.A.:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.iva) + "</td></tr>");
                    $('#tabPaso3_3').html(setCoinFormat('' + respuestaJson.total));
                    var tipoMonSelect = parseInt($('#dc_moneda option:selected').val());
                    if (tipoMonSelect == 1) {
                        $('#titPrimaObj').text("Prima Objetivo (Pesos):");
                    } else {
                        $('#titPrimaObj').text("Prima Objetivo (Dolares):");
                    }
                    hideLoader();
                    showMessageSuccess('.navbar', 'Información actualizada correctamente', 0);
                } else {
                    agregaAlertError(respuestaJson.msg);

                    hideLoader();
                }
            });
        }
        $('#txtPrimaObj').val('');
        hideLoader();
    } catch (err) {

        hideLoader();
        showMessageError('.navbar', 'Error al consultar la información ' + primaNeta, 0);
    }
});

$("#paso3_back").click(function (){
	goToPage(enlace.PASO2);
});

function goToPage(page){
	showLoader();
	actualizainfoCot();
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infCotiJson ),
		paso : page
		/*paso : seleccionaVentana()*/
		/*paso : enlace.PASO2*/
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			window.location.href = response.msg;
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
}

function seleccionaVentana(){
	if(infCotiJson.tipoCotizacion == tipoCotizacion.EMPRESARIAL){
		return enlace.EMPRESARIAL;
	}
	return enlace.FAMILIAR;
}

function actualizainfoCot(){
	switch (infCotiJson.modo) {
		case modo.NUEVA:
			infCotiJson.modo = modo.EDICION;
			break;
		case modo.COPIA:
			infCotiJson.modo = modo.EDICION;
			break;
		case modo.ALTA_ENDOSO:
			infCotiJson.modo = modo.EDITAR_ALTA_ENDOSO;
			break;
		case modo.BAJA_ENDOSO:
			infCotiJson.modo = modo.EDITAR_BAJA_ENDOSO;
			break;
		default:
			break;
	}
}


$('#polizaBtnEnviar').click(function(e) {
    showLoader();
    var emailsList = $('#listaCorreos li');
    var emailsTot = "";
    $.each(emailsList, function(i, emlis) {
        if (i > 0) {
            emailsTot += ",";
        }
        emailsTot += $(emlis).attr('email');
    });
    recuperaDocumentosEmision(emailsTot);
});

function recuperaDocumentosEmision(emails) {
    $.post($('#txtJSGetDocsEmision').val(), {
        infoDocs: jsonDocumentosEmision(),
        listaEmails: emails,
        cliente: $('#txtModalPolizaAsegurado').text(),
        poliza: $('#txtModalPolizaNumeroPoliza').text(),
        totUbica: $('#txtModalPolizaTotalUbicaciones').text(),
        moneda: $('#txtModalPolizaMoneda').text(),
        certificado: $('#txtModalPolizaCertificado').text(),
        vigencia: $('#txtModalPolizaVigenciaAl').text() + ' al ' + $('#txtModalPolizaVigenciaAl').text(),
        formaPago: $('#txtModalPolizaFormaPago').text(),
        primaNeta: '$' + $('#txtModalPolizaPrimaNeta').text(),
        recargo: '$' + $('#txtModalPolizaRecargoPago').text(),
        gasto: '$' + $('#txtModalPolizaGastosExpedicion').text(),
        iva: '$' + $('#txtModalPolizaIva').text(),
        prima: '$' + $('#txtModalPolizaTotal').text(),
        folio: infCotiJson.folio,
        agente: $('#txtModalPolizaAgente').val()
    }).done(function(data) {
        sessionExtend();
        var respuestaJson = JSON.parse(data);
        if (respuestaJson.code >= 0) {
            if (emails == null) {
                $.each(respuestaJson.archivos, function(i, archivo) {
                	/*
                    fileAux = 'data:application/octet-stream;base64,' + archivo.documento
                    var dlnk = document.getElementById(archivo.nombre + archivo.extension);
                    dlnk.href = fileAux;
                    dlnk.download = archivo.nombre + '.' + archivo.extension;
                    dlnk.click();
                    */
                	if(detectIEEdge()){
    					fileAux = 'data:application/octet-stream;base64,'+archivo.documento
    					var dlnk = document.getElementById('dwnldLnk');
    					dlnk.href = fileAux;
    					dlnk.download = archivo.nombre+'.'+archivo.extension;
    					location.href=document.getElementById("dwnldLnk").href;
    					/*dlnk.click();*/
    				}else{
    					/*
    					 * downloadDocument('archivo base 64' , 'nombre.extension' );
    					 */
    					downloadDocument(archivo.documento, archivo.nombre+'.'+archivo.extension);
    				}
                });
            } else {
                showMessageSuccess('#modalGenerarPoliza', "Correo(s) enviado(s)", 0);
            }
        } else {
            showMessageError('#modalGenerarPoliza', respuestaJson.msg, 0);
        }
    }).fail(function() {
        showMessageError('#modalGenerarPoliza', "Error al consultar la informacion", 0);
    }).always(function() {
        hideLoader();
    });
}

$('#btnDescargarArchivos').click(function(e) {
    showLoader();
    recuperaDocumentosEmision(null);
});

$('#txtCederComision').on('keyup', function() {
    $(event.target).val(function(index, value) {
        var aux = value.replace(/\D/g, "")
        if (parseInt(aux) > 100) {
            showMessageError('.navbar', 'La comisión no pude superar el 100% ', 0);
            return '100';
        }
        return aux;
    });
});




$("#renovacion_back").click(function(){
	showLoader();
	var url = new URL(window.location.href);
	window.location.href = url.origin + url.pathname.replace("paso3", "renovacion-automatica");
	
})

$('#btnEnvCotiSusAgente').click(function() {
    showLoader();
    var url = new URL(window.location.href);
	var auxUrl = url.pathname.replace("/paso3", seleccionaVentana());
    
    $.post( sendMailSuscriptorAgenteURL , {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version,
        folio: infCotiJson.folio,
        url: url.origin + auxUrl,
        tipoCotizacion: infCotiJson.tipoCotizacion.toString(),
        email: $('#txtEmailAgente').val()
    }).done(function() {
    	goToHome();
    });
});


/***************************************************Funciones Botones*************************************************/
/*********************************************************************************************************************/
/**************************************************Funciones Genericas************************************************/

function validaBotonEmision(estado) {
	/*
    if (estado == 340 || estado == 350 || estado == 351) {
        $("#paso3_emision").attr("disabled", false);
    } else {
        $("#paso3_emision").attr("disabled", true);
    }
    */
    
    if (estado == 340 || estado == 350 || estado == 351) {
    	
    	$.post(validaAgenteURL, {
    		cotizacion: infCotiJson.cotizacion,
	    	codigoAgente: ''
	    }).done(function(data) {
	    	
	    	var response = JSON.parse(data);
	    	
	    	if(response.code != 0) {
	    		if(response.code == 3) {
	    			$("#modalBloqueoAgente").modal('show');
	    			$("#paso3_emision").prop("disabled", true);
	    		}
	    		else {
	    			showMessageError('.navbar', response.msg, 0);
	    		}
	    	}
	    	else {
	    		
	    		$("#paso3_emision").prop("disabled", false);
	    	}
	    });
    } else {
        $("#paso3_emision").prop("disabled", true);
    }
}

function resetSession() {
    try {
        Liferay.Session.extend();
    } catch (err) {

    }
}

function consumeEmisionData() {
    showLoader();
    $.post($('#txtJSGetEmisionData').val(), {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version
    }).done(function(data) {
        sessionExtend();
        var response = jQuery.parseJSON(data);
        if (response.code == 0) {
        	/*
            varAuxiliares.tipoPersona = isFisica_Moral(response.datosCliente.tipoPer);
            preCargaDatos(response);
            */
            goToPage(enlace.PASO4);
            /*hideLoader();*/
        } else {
            showMessageError('.navbar', 'Error al consultar la información', 0);
            hideLoader();
        }
        /*
        if (valIsNullOrEmpty($('#txtBtnEmiteFactu').val())) {
            validaPrimaMax492(0);
        }
        */
    }).fail(function(e) {
        showMessageError('.navbar', 'Error al consultar la información (data)', 0);
        hideLoader();
    });
}

function llenaInfoModalPoliza(json) {
    $('.listaCorreos li').remove();
    $('#txtModalPolizaNumeroPoliza').text(validaKeyJson(json, 'numeroPoliza'));
    $('#txtModalPolizaCertificado').text(validaKeyJson(json, 'certificado'));
    $('#txtModalPolizaAsegurado').text(validaKeyJson(json, 'asegurado'));
    $('#txtModalPolizaAgente').val(validaKeyJson(json, 'agente'));

    $('#txtModalPolizaVigenciaDe').text(stringToDate(validaKeyJson(json, 'vigencia.inicio')));
    $('#txtModalPolizaVigenciaAl').text(stringToDate(validaKeyJson(json, 'vigencia.fin')));
    $('#divDescargarArchivos').html();

    $('#txtModalPolizaTotalUbicaciones').text(validaKeyJson(json, 'totalUbicaciones'));
    $('#txtModalPolizaMoneda').text(validaKeyJson(json, 'moneda'));
    $('#txtModalPolizaMoneda').text(validaKeyJson(json, 'moneda'));
    $('#txtModalPolizaFormaPago').text(validaKeyJson(json, 'formaPago'));
    $('#txtModalPolizaPrimaNeta').text(validaKeyJson(json, 'primaNeta'));
    $('#txtModalPolizaRecargoPago').text(validaKeyJson(json, 'recargo'));
    $('#txtModalPolizaGastosExpedicion').text(validaKeyJson(json, 'gastos'));
    $('#txtModalPolizaIva').text(validaKeyJson(json, 'iva'));
    $('#txtModalPolizaTotal').text(validaKeyJson(json, 'total'));
    $('#tablaArchivosPoliza tbody').empty();
    if (!valIsNullOrEmpty($('#txtEmailUser').val())) {
        $('.modal .listaCorreos')
            .append(
                $('<li email="' +
                	Base64.decode( $('#txtEmailUser').val() ) +
                    '" ><button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    Base64.decode( $('#txtEmailUser').val() ) + '</li>'));
    }

    if (valIsNullOrEmpty(validaKeyJson(json, 'archivos'))) {
        $('.selectCheckImput').prop('checked', false);
        $('.selectCheckImput').prop("disabled", true);
        $('#btnDescargarArchivos').prop("disabled", true);
        $('#polizaBtnEnviar').prop("disabled", true);
    } else {
        $('.selectCheckImput').prop('checked', true);
        $('.selectCheckImput').prop("disabled", false);
        $('#btnDescargarArchivos').prop("disabled", false);
        validaBtnEnviar();
        $.each(json.archivos, function(i, a) {
            var chekbox = '<div class="form-check"> ' + '<input class="form-check-inpu chekArchivos" name="' + a.nombre +
                "-" + a.extension + '" idCarpeta="' + a.idCarpeta + '" idDocumento="' + a.idDocumento +
                '" idCatalogoDetalle="' + a.idCatalogoDetalle + '" type="checkbox" id="' + a.nombre + "-" +
                a.extension + '" checked>' + '<label for="' + a.nombre + "-" + a.extension + '"></label>' +
                '</div>';

            $('#tablaArchivosPoliza tbody').append(
                $('<tr> <td> ' + chekbox + ' </td> <td>  ' + a.nombre + "." + a.extension + ' </td> <td >  ' +
                    a.tipo + ' </td> </tr>'));

            $('#divDescargarArchivos').append($('<a id="' + a.nombre + a.extension + '" />'));

        });
    }
}

function validaKeyJson(json, cadena) {
    var infoJson = '';
    var res = cadena.split(".");
    var ant = null;
    json['name']
    $.each(res, function(key, val) {
        if (key == 0) {
            infoJson = (val in json) ? eval('json.' + val) : "";
            ant = eval('json.' + val);
        } else {
            if (valIsNullOrEmpty(ant)) {
                infoJson = "";
            } else {
                infoJson = (val in ant) ? eval('ant.' + val) : "";
                ant = eval('ant.' + val);
            }
        }
    });
    return infoJson;
}

function validaBtnEnviar() {
    if ($('.listaCorreos li').length > 0) {
        $('#polizaBtnEnviar').prop("disabled", false);
        $('.msjActivarBtnEnviar').prop('hidden', true);
    } else {
        $('#polizaBtnEnviar').prop("disabled", true);
        $('.msjActivarBtnEnviar').prop('hidden', false);
    }
}

$('#txtPrimaObj').on('keyup', function() {
    $(event.target).val(function(index, value) {
        var aux = value.replace(/[$,]/g, '');
        aux = aux.replace(/\D/g, "")
            .replace(/([0-9])([0-9]{2})$/, '$1.$2')
            .replace(/\B(?=(\d{3})+(?!\d)\.?)/g, ",");
        return '$' + aux;
    });
});

function setCoinFormat(num) {
	num = "" + num;
	if( num ==""){
		return num;
	}
	
	arraySplit = num.split(".");
	izq = arraySplit[0];
	der = "00";
	if ( num.includes(".") ) {
		der = arraySplit[1];
	}
	izq = izq.replace(/ /g, "");
	izq = izq.replace(/\$/g, "");
	izq = izq.replace(/,/g, "");

	var izqAux = "";
	var j = 0;
	for ( i = izq.length - 1; i >= 0; i-- ) {
		if ( j != 0 && j % 3 == 0 ) {
			izqAux += ",";
		}
		j++;
		izqAux += izq[i];
	}
	izq = "";
	for ( i = izqAux.length - 1; i >= 0; i-- ) {
		izq += izqAux[i];
	}
	der = der.substring(0, 2);
	if ( der.length < 2 ) {
		der += "0";
	}
	return "$" + izq + "." + der;
}

$("#modalPolizaEnviarCorreo").keyup(function(e) {
    if (valIsNullOrEmpty($(this).val())) {
        $('#btnAgregaCorreoPoliza').prop("disabled", true);
    } else {
        $('#btnAgregaCorreoPoliza').prop("disabled", false);
        eliminaErrorEmailEmision();
    }
    var code = (e.keyCode ? e.keyCode : e.which);
    if (code == 13) {
        $('#btnAgregaCorreoPoliza').trigger('click');
    }
});

function eliminaErrorEmailEmision() {
    $("#modalPolizaEnviarCorreo").removeClass('invalid');
    $("#modalPolizaEnviarCorreo").siblings('.alert-danger').remove();
}
$('#btnAgregaCorreoPoliza').click(function(e) {
    var correo = $('#modalPolizaEnviarCorreo').val();
    var error = chekEmail($('#modalPolizaEnviarCorreo'));
    if ((!error) && (!valIsNullOrEmpty(correo))) {
        $('.modal .listaCorreos').append(
            $('<li email="' + correo +
                '"><button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                correo + '</li>'));
        $('#modalPolizaEnviarCorreo').val('');
        $('#listaCorreos').scrollTo('li:last');
        $('#btnAgregaCorreoPoliza').prop("disabled", true);
    }
    validaBtnEnviar();
    if (!almenosUnArchivoSeleccionado()) {
        $('#polizaBtnEnviar').prop("disabled", true);
    }
});

function chekEmail(campos) {
    var errores = false;
    $.each(campos, function(index, value) {
        if (!valIsNullOrEmpty($(value).val())) {
            if (!validateEmail($(value).val())) {
                errores = true;
                $(value).addClass('invalid');
                $(value).parent().append(
                    "<div class=\"alert alert-danger\" role=\"alert\"> <span class=\"glyphicon glyphicon-ban-circle\"></span>" +
                    " " + $('#txtFormatoEmail').val() + "</div>");
            }
        }
    });
    return errores;
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

$('.modal .listaCorreos').on('click', '.close', function(e) {
    $(this).parent().remove();
    validaBtnEnviar();
});

function jsonDocumentosEmision() {
    var cheks = $('#modalGenerarPoliza .bodyArchivos input[type=checkbox]');
    var listaDocumentos = '';
    if ($('.selectCheckImput').is(':checked')) {
        $.each(cheks, function(i, chek) {
            var ids = $(chek).attr('id');
            if (i > 0) {
                listaDocumentos += ",";
            }
            listaDocumentos += '{"idCarpeta" : ' + $(chek).attr('idCarpeta') + ', "idDocumento" : ' +
                $(chek).attr('idDocumento') + ', "idCatalogoDetalle" : ' +
                $(chek).attr('idCatalogoDetalle') + ', "documento" : "", "nombre" : "", "extension" : "" }';
        });
    } else {
        $.each(cheks, function(i, chek) {
            if ($(chek).is(':checked')) {
                var ids = $(chek).attr('id');
                if (!valIsNullOrEmpty(listaDocumentos)) {
                    listaDocumentos += ",";
                }
                listaDocumentos += '{"idCarpeta" : ' + $(chek).attr('idCarpeta') + ', "idDocumento" : ' +
                    $(chek).attr('idDocumento') + ', "idCatalogoDetalle" : ' +
                    $(chek).attr('idCatalogoDetalle') +
                    ', "documento" : "", "nombre" : "", "extension" : "" }';
            }
        });
    }
    return listaDocumentos;
}

function almenosUnArchivoSeleccionado() {
    var seleccionado = false;
    $.each($('.modal .bodyArchivos .chekArchivos'), function(i, chek) {
        if ($(chek).is(':checked')) {
            seleccionado = true;
            return false;
        }
    });
    return seleccionado;
}

function compruebaBqOri(){	
	$.each($('.bqOri'), function(key, val) {
		if($(val).val() == ''){
			$(val).removeClass('bqOri');
		}
	});
}

function detectIEEdge() {
    var ua = window.navigator.userAgent;

    var msie = ua.indexOf('MSIE ');
    if (msie > 0) {
        // IE 10 or older => return version number
        console.log(parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10));
        return true;
    }

    var trident = ua.indexOf('Trident/');
    if (trident > 0) {
        // IE 11 => return version number
        var rv = ua.indexOf('rv:');
        console.log(parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10));
        return true;
    }

    var edge = ua.indexOf('Edge/');
    if (edge > 0) {
        // Edge => return version number
        console.log(parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10));
        return true;
    }

    // other browser
    return false;
}

function downloadDocument(strBase64, filename) {
    var url = "data:application/octet-stream;base64," + strBase64;
    var documento = null;
    /*.then(res => res.blob())*/
    fetch(url)
        .then(function(res) { return res.blob() })
        .then(function(blob) {
            downloadBlob(blob, filename);
        });
}

function downloadBlob(blob, filename) {
    if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, filename);
    } else {
        var elem = window.document.createElement('a');
        elem.href = window.URL.createObjectURL(blob);
        elem.download = filename;
        document.body.appendChild(elem);
        elem.click();
        document.body.removeChild(elem);
    }
}

function validaModo() {
	switch (infCotiJson.modo) {
		case modo.CONSULTA:
			$('#divCederComision').addClass('d-none');
			$('#divPrimaObj').addClass('d-none');
			$('#paso3_slip').attr('disabled', true);
			break;
		case modo.CONSULTAR_REVISION:
			$('#divCederComision').addClass('d-none');
			$('#divPrimaObj').addClass('d-none');
			break;
		case modo.BAJA_ENDOSO:
			
			$('#paso3_emision').addClass('d-none');
			$('#btnEmitEndoso').removeClass('d-none');
			
			generaTblsEndBaja();
			
			$('#paso3_back').addClass('d-none');
			$('#paso1_back').removeClass('d-none');
			agregaTipoMonedaPT();
			break;
		case modo.EDITAR_BAJA_ENDOSO:
			agregaTipoMonedaPT();
			$('#paso3_emision').addClass('d-none');
			$('#btnEmitEndoso').removeClass('d-none');
			
			generaTblsEndBaja();
			$('#paso3_back').addClass('d-none');
			$('#paso1_back').removeClass('d-none');
			break;
		case modo.ALTA_ENDOSO:
			agregaTipoMonedaPT();
			break;
		case modo.EDITAR_ALTA_ENDOSO:
			agregaTipoMonedaPT();
			break;
		case modo.RENOVACION_AUTOMATICA :
			generaPantallaRenovacion();
			break;
		case modo.CONSULTAR_RENOVACION_AUTOMATICA :
			generaPantallaRenovacion();
			$('#paso3_slip').attr('disabled', true);
			break;
		default:

			break;
	}
}

function generaPantallaRenovacion(){
	agregaTipoMonedaPT();
	$("#paso3_back").addClass("d-none");
	$("#renovacion_back").removeClass("d-none");
	$("#titPoliza").text("Renovación de Póliza " + infCotiJson.poliza);
}

function generaTblsEndBaja(){
	$('#divTbl').addClass('d-none');
	$('#divTblEndBj').removeClass('d-none');
	$("#titulosEndBj").append(tablaBajasEndoso);
	$("#datosEndBj").append(tablaBajasEndoso);
	$("#totalEndBj").append(tablaBajasEndoso);
	$("#titulosEndBj .tb2").addClass("d-none");
	$("#titulosEndBj .tb3").addClass("d-none");
	$("#datosEndBj .tb1").addClass("d-none");
	$("#datosEndBj .tb3").addClass("d-none");
	$("#totalEndBj .tb1").addClass("d-none");
	$("#totalEndBj .tb2").addClass("d-none");

}

function agregaTipoMonedaPT(){
	var p1json = JSON.parse(infP1);	
	$("#valPrimTot").addClass("pt_mon");
	if(p1json.monedaSeleccionada == "1"){
		$("#valPrimTot #tabPaso3_3").text($("#valPrimTot #tabPaso3_3").text() + 'MXN');
	}else{
		$("#valPrimTot #tabPaso3_3").text($("#valPrimTot #tabPaso3_3").text() + 'USD')
	}

}

function goToHome(){
	showLoader();
	var urlHome = window.location.origin + '/group/portal-agentes' + seleccionaVentana();
	var url = new URL(window.location.href);
	var aux = url.pathname.replace("/paso3", seleccionaVentana());
	window.location.href = url.origin + aux;
}


function noSelect(campo) {
    var errores = false;
    if ($(campo).val() == "-1") {
        errores = true;
        $(campo).siblings("input").addClass('invalid');
        $(campo).parent().append(
            "<div class=\"alert alert-danger\"> <span class=\"glyphicon glyphicon-ban-circle\"></span> " + " " +
            msj.es.campoRequerido + "</div>");
    }
    
    return errores;
}

function setBase64(){
	Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}};
}

/**************************************************Funciones Genericas************************************************/
/*********************************************************************************************************************/