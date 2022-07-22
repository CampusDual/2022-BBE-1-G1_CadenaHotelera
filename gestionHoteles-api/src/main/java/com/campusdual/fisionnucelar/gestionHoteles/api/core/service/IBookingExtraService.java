package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the operations over the booking_extra table
 *@since 20/07/2022
 *@version 1.0 
 *
 */
public interface IBookingExtraService {
	 // BOOKING_EXTRA
	 public EntityResult bookingextraQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult bookingextraInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	
	

	}


