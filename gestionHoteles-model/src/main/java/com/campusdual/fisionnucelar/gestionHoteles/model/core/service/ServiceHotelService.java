package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServiceService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServicesHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceHotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.IncorrectBooleanException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the clients table
 * 
 * @since 08/07/2022
 * @version 1.0
 *
 */
@Service("ServiceHotelService")
@Lazy
public class ServiceHotelService implements IServicesHotelService {
	@Autowired
	private ServiceHotelDao serviceHotelDao;

	@Autowired
	private HotelDao hotelDao;

	@Autowired
	private ServiceDao serviceDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;

	public ServiceHotelService() {
		super();
		this.control = new Control();
	}

	/**
	 * 
	 * Executes a generic query over the services_hotel table
	 * 
	 * @since 08/07/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the services-hotel table especified in the params
	 *         and a message with the operation result
	 */
	@Override
	public EntityResult servicehotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(serviceHotelDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the services_hotel table.
	 * 
	 * @since 08/07/2022
	 * @param The fields of the new register
	 * @return The id of the new service_hotel and a message with the operation
	 *         result
	 */
	@Override
	public EntityResult servicehotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {

			if (attrMap.containsKey("svh_hotel")) {
				checkIfHotelExists(attrMap);
			}
			if (attrMap.containsKey("svh_service")) {
				checkIfServiceExists(attrMap);
			}

			insertResult = this.daoHelper.insert(this.serviceHotelDao, attrMap);
			if (insertResult.isEmpty())
				throw new AllFieldsRequiredException("FIELDS_REQUIRED");

			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "DUPLICATED_SERVICES_IN_HOTEL");
		} catch (RecordNotFoundException | AllFieldsRequiredException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DataIntegrityViolationException e) {
			control.setErrorMessage(insertResult, "SERVICE_AND_HOTEL_REQUIRED");
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the services_hotel table.
	 * 
	 * @since 08/07/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult servicehotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
			checkIfServiceHotelExists(keyMap);
			if (attrMap.containsKey("svh_hotel")) {
				checkIfHotelExists(attrMap);
			}
			if (attrMap.containsKey("svh_service")) {
				checkIfServiceExists(attrMap);
			}
			if (attrMap.containsKey("svh_active")) {
				checkCorrectBoolean(attrMap);
			}
			updateResult = this.daoHelper.update(this.serviceHotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "DUPLICATED_SERVICES_IN_HOTEL");
		} catch (RecordNotFoundException | IncorrectBooleanException | EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	private void checkCorrectBoolean(Map<String, Object> attrMap) throws IncorrectBooleanException {
		if ((int) attrMap.get("svh_active") > 1 || (int) attrMap.get("svh_active") < 0) {
			throw new IncorrectBooleanException("SVH_ACTIVE_MUST_BE_1_OR_0");
		}
	}

	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("svh_hotel") == null && attrMap.get("svh_service") == null
				&& attrMap.get("svh_active") == null) {
			throw new EmptyRequestException("ANY_FIELDS_REQUIRED");
		}
	}

	private boolean checkIfServiceHotelExists(Map<String, Object> keyMap) {
		if (keyMap.get("id_services_hotel") == null) {
			throw new RecordNotFoundException("ID_SERVICES_HOTEL_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_services_hotel");
		EntityResult existingServicesHotel = daoHelper.query(serviceHotelDao, keyMap, fields);
		if (existingServicesHotel.isEmpty())
			throw new RecordNotFoundException("ERROR_SERVICE_IN_HOTEL_NOT_FOUND");
		return existingServicesHotel.isEmpty();
	}

	private boolean checkIfHotelExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_hotel", attrMap.get("svh_hotel"));
		EntityResult existingHotel = daoHelper.query(hotelDao, keyMap, attrList);
		if (existingHotel.isEmpty())
			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
		return existingHotel.isEmpty();
	}

	private boolean checkIfServiceExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_service");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_service", attrMap.get("svh_service"));
		EntityResult existingService = daoHelper.query(serviceDao, keyMap, attrList);
		if (existingService.isEmpty())
			throw new RecordNotFoundException("SERVICE_DOESN'T_EXISTS");
		return existingService.isEmpty();
	}
}
