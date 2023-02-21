package com.tokio.pa.cotizadorpaso3portlet73.util;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;

import javax.mail.internet.InternetAddress;

public class SendMailSuscriptorAgenteP3 {
	public void sendMail(String[] mails,  String folio, String link, String link2, File slip){
		try {
			String fromMail = "portal_agentes@tokiomarine.com.mx";
			MailMessage ms = new MailMessage();
			String body = "<!DOCTYPE html><html>  <head>    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />    <title>Propuesta de cotización ${folio}</title>  </head>  <body>    <header><h3>Propuesta de cotización</h3></header>    <section>      <p>        La cotización del folio ${folio} le ha sido asignada para su validación.      </p>      <p>        A partir de esta fecha usted cuenta con 30 días naturales para aceptar        la emisión, una vez concluido este periodo será necesario realizar una        nueva cotización.      </p>      <p>Link directo a modificacion: ${link}</p>          <p>Le enviamos un cordial saludo</p>    </section>    <footer>      <img        src='https://preview.ibb.co/i3vFWp/Firma_Correo_Tokio_Marine.png'        alt='Firma Correo Tokio Marine'        width='35%'      />    </footer>  </body></html>";
			body = StringUtil.replace(body,
					new String[] { "${folio}",  "${link}", "${link2}" },
					new String[] { folio, link, link2});
			
			InternetAddress fromAddress = new InternetAddress(fromMail);
			InternetAddress toAddress = null;
			InternetAddress[] bulkAddresses = new InternetAddress[mails.length];

			ms.setFrom(fromAddress);
			ms.setSubject("Propuesta de cotización  - " + folio );
			ms.setBody(body);
			ms.setHTMLFormat(true);
			ms.addFileAttachment(slip);

			for (int i = 0; i < mails.length; i++) {
				toAddress = new InternetAddress(mails[i]);
				bulkAddresses[i] = toAddress;
			}
			ms.setBulkAddresses(bulkAddresses);

			MailServiceUtil.sendEmail(ms);
			System.out.println("--------fin mail-------");
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
}
