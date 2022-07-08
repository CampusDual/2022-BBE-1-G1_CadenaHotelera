package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServiceService;
import com.ontimize.jee.server.rest.ORestController;


/**
 * This class listens the incoming requests related with the services table
 *@since 07/07/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/services")
public class ServiceRestController extends ORestController<IServiceService> {

	 @Autowired
	 private IServiceService serviceService;

	 @Override
	 public IServiceService getService() {
	  return this.serviceService;
	 }
}
