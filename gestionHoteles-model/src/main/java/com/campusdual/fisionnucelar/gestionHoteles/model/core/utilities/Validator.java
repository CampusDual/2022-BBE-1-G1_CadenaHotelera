package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
@Component
public class Validator {
	@Autowired
	private ServiceDao serviceDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	
	/**
	 * checks if the given EntityResults is empty
	 * 
	 * @param result an EntityResult
	 * @exception RecordNotFoundException sends a message to the user if the
	 *                                    resultant query has not results
	 */
	public void checkEmptyResult(EntityResult result) {
		if (result.isEmpty()) {
			throw new RecordNotFoundException("WITHOUT_RESULTS");
		}

	}
	
	/**
	 * Checks if the fields neccesary to make requests at some methods in this
	 * service has been provided by the user
	 * 
	 * @param attrMap the fields to be checked EmptyRequestException sends a message
	 *                to the user if the request provided is empty
	 */

	public void checkIfMapIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.isEmpty()) {
			throw new EmptyRequestException("EMPTY_REQUEST");
		}
	}
	
	/**
	 * Checks if the user is providing id_extras_hotel and quantity parameters to
	 * add an extra to the booking
	 * 
	 * @param attrMap id_extras_hotel and quantity
	 * @exception EmptyRequestException sends a message to the user if he is not
	 *                                  providing the two required parameters
	 */
	public void checkDataUpdateExtraPrice(Map<String, Object> attrMap) {
		if (attrMap.get("id_extras_hotel") == null || attrMap.get("quantity") == null) {
			throw new EmptyRequestException("ID_EXTRAS_AND_QUANTITY_REQUIRED");
		}
	}
	
	/**
	 * Search a concrete service. It throws an exception if it doesn't exists
	 * 
	 * @param keyMap The id of the services to search
	 * @exception RecordNotFoundException if doesn't find any result
	 * 
	 */
	public void checkIfServiceExists(Map<String, Object> keyMap) {
		if (keyMap.get("id_service") == null) {
			throw new RecordNotFoundException("ID_SERVICE_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_service");
		EntityResult existingServices = daoHelper.query(serviceDao, keyMap, fields);
		if (existingServices.isEmpty())
			throw new RecordNotFoundException("ERROR_SERVICE_NOT_FOUND");

	}
}
