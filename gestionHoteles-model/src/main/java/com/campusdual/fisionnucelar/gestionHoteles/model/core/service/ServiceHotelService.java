package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
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
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
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
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;
	private Validator validator;
	private Logger log;

	public ServiceHotelService() {
		super();
		this.control = new Control();
		this.validator = new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a generic query over the services_hotel table
	 * 
	 * @since 08/07/2022
	 * @param The filters and the fields of the query
	 * @exception NoResultsException     when there are not matching results on the
	 *                                   ServicesHotel table
	 * 
	 * @exception BadSqlGrammarException when it receives an incorrect type in the
	 *                                   params
	 * 
	 * @return The columns from the services-hotel table especified in the params
	 *         and a message with the operation result
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult servicehotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {		
			searchResult = daoHelper.query(serviceHotelDao, keyMap, attrList);
			control.controlAccess((int) searchResult.getRecordValues(0).get("svh_hotel"));
			control.checkResults(searchResult);
		} catch (NoResultsException|NotAuthorizedException e) {
			log.error("unable to retrieve a hotel service. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a hotel service. Request : {} {} ",keyMap,attrList, e);
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
	 * 
	 * @exception DuplicateKeyException           when receives an existing
	 *                                            HotelService
	 * 
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields or include a non
	 *                                            existing hotel or service
	 * 
	 * @exception EmptyRequestException           when the params are empty
	 * 
	 * @return The id of the new service_hotel and a message with the operation
	 *         result
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult servicehotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			control.controlAccess((int) attrMap.get("svh_hotel"));
			insertResult = this.daoHelper.insert(this.serviceHotelDao, attrMap);
			if (insertResult.isEmpty())
				throw new EmptyRequestException("FIELDS_REQUIRED");

			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a hotel service. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "DUPLICATED_SERVICES_IN_HOTEL");
		} catch (EmptyRequestException|NotAuthorizedException e) {
			log.error("unable to insert a hotel service. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a hotel service. Request : {} ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the services_hotel table.
	 * 
	 * @since 08/07/2022
	 * @param The fields to be updated
	 * @exception DuplicateKeyException           when trying to change a not null
	 *                                            field of an hotelService for an
	 *                                            existing one
	 * 
	 * @exception EmptyRequestException           when the params are empty
	 * 
	 * @exception RecordNotFoundException         when receives a non existing
	 *                                            hotelService to update
	 * 
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields or include a non
	 *                                            existing hotel or service
	 * 
	 * @exception IncorrectBooleanException       when receive an incorrect value
	 *                                            for the svh_active field (only 1
	 *                                            for true and 0 for false admitted)
	 * 
	 * @return A message with the operation result
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult servicehotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult hotelResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			checkIfServiceHotelExists(keyMap);
			hotelResult=daoHelper.query(serviceHotelDao, keyMap, Arrays.asList("svh_hotel"));
			control.controlAccess((int) hotelResult.getRecordValues(0).get("svh_hotel"));
			
			if (attrMap.containsKey("svh_active"))
				checkCorrectBoolean(attrMap);
			updateResult = this.daoHelper.update(this.serviceHotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		} catch (DuplicateKeyException e) {
			log.error("unable to update a hotel service. Request : {}  {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "DUPLICATED_SERVICES_IN_HOTEL");
		} catch (DataIntegrityViolationException|NotAuthorizedException e) {
			log.error("unable to update a hotel service. Request : {}  {} ",keyMap,attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		} catch (RecordNotFoundException | IncorrectBooleanException | EmptyRequestException e) {
			log.error("unable to update a hotel service. Request : {}  {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}

		return updateResult;

	}

	
	
	/**
	 * Search a concrete hotelService. It throws an exception if it doesn't exists
	 * @param keyMap The hotelService to search
	 * @exception RecordNotFoundException If it doesn't find any result
	 */
	private void checkIfServiceHotelExists(Map<String, Object> keyMap) {
		if (keyMap.get("id_services_hotel") == null) {
			throw new RecordNotFoundException("ID_SERVICES_HOTEL_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_services_hotel");
		EntityResult existingServicesHotel = daoHelper.query(serviceHotelDao, keyMap, fields);
		if (existingServicesHotel.isEmpty())
			throw new RecordNotFoundException("ERROR_SERVICE_IN_HOTEL_NOT_FOUND");
		
	}
		
	
	/**
	 * Checks if the user has introduced a valid value for the svh_active field
	 * 
	 * @param attrMap A numeric boolean to indicate if the serviceHotel is active (1) or not (0)
	 * @throws IncorrectBooleanException if the value is different than 1 or 0
	 */
	
	private void checkCorrectBoolean(Map<String, Object> attrMap) throws IncorrectBooleanException {
		if ((int) attrMap.get("svh_active") > 1 || (int) attrMap.get("svh_active") < 0) {
			throw new IncorrectBooleanException("SVH_ACTIVE_MUST_BE_1_OR_0");
		}
	}


}
//	private boolean checkIfHotelExists(Map<String, Object> attrMap) {
//		List<String> attrList = new ArrayList<>();
//		attrList.add("id_hotel");
//		Map<String, Object> keyMap = new HashMap<>();
//		keyMap.put("id_hotel", attrMap.get("svh_hotel"));
//		EntityResult existingHotel = daoHelper.query(hotelDao, keyMap, attrList);
//		if (existingHotel.isEmpty())
//			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
//		return existingHotel.isEmpty();
//	}
//
//	private boolean checkIfServiceExists(Map<String, Object> attrMap) {
//		List<String> attrList = new ArrayList<>();
//		attrList.add("id_service");
//		Map<String, Object> keyMap = new HashMap<>();
//		keyMap.put("id_service", attrMap.get("svh_service"));
//		EntityResult existingService = daoHelper.query(serviceDao, keyMap, attrList);
//		if (existingService.isEmpty())
//			throw new RecordNotFoundException("SERVICE_DOESN'T_EXISTS");
//		return existingService.isEmpty();
//	}
//}
