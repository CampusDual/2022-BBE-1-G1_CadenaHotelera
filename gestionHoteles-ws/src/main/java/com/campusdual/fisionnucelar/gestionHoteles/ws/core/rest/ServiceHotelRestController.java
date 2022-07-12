package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServicesHotelService;
import com.ontimize.jee.server.rest.ORestController;


/**
 * This class listens the incoming requests related with the services_hotel table
 *@since 08/07/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/servicehotel")
public class ServiceHotelRestController  extends ORestController<IServicesHotelService>{

	 @Autowired
	 private IServicesHotelService serviceHotelService;

	 @Override
	 public IServicesHotelService getService() {
	  return this.serviceHotelService;
	 }
}
