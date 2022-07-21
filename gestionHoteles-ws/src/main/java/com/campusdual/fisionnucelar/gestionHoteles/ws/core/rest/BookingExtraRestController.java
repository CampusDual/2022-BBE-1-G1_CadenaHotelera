package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingExtraService;

import com.ontimize.jee.server.rest.ORestController;

/**
 * This class listens the incoming requests related with the bookings table
 *@since 30/06/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/bookingextra")
public class BookingExtraRestController extends ORestController<IBookingExtraService>{

	@Autowired
	private IBookingExtraService bookingextraservice;
	
	@Override
	public IBookingExtraService getService() {
		return this.bookingextraservice;
	}

}
