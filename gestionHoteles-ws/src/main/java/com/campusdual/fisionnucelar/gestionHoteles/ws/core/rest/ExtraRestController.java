package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraService;
import com.ontimize.jee.server.rest.ORestController;


/**
 * This class listens the incoming requests related with the extra table
 *@since 08/07/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/extras")
public class ExtraRestController extends ORestController<IExtraService>{
	 @Autowired
	 private IExtraService extraService;

	 @Override
	 public IExtraService getService() {
	  return this.extraService;
	 }
}
