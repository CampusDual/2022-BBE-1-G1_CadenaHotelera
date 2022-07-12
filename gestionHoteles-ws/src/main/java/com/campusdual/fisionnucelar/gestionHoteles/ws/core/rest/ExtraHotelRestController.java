package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraHotelService;

import com.ontimize.jee.server.rest.ORestController;

/**
 * This class listens the incoming requests related with the services_hotel table
 *@since 12/07/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/extrahotel")
public class ExtraHotelRestController extends ORestController<IExtraHotelService>{

	 @Autowired
	 private IExtraHotelService extraHotelService;

	 @Override
	 public IExtraHotelService getService() {
	  return this.extraHotelService;
	 }
}
