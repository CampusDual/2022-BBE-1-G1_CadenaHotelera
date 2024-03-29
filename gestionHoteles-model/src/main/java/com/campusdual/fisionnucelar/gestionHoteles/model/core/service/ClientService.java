package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.reflect.CatchClauseSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IClientService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ClientDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidDateException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidPhoneException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the clients table
 * 
 * @since 30/06/2022
 * @version 1.0
 *
 */
@Service("ClientService")
@Lazy
public class ClientService implements IClientService {

	@Autowired
	private ClientDao clientDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	@Autowired
	private BookingDao bookingDao;

	@Autowired
	private IBookingService bookingService;
	
	private Logger log;
	private Control control;
	
	private UserControl userControl;
	Validator dataValidator;

	public ClientService() {
		this.control = new Control();
		this.dataValidator=new Validator();
		this.userControl=new UserControl();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a generic query over the clients table
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the clients table especified in the params and a
	 *         message with the operation result
	 *@exception BadSqlGrammarException when it introduces a string instead of a numeric on id
     *@exception NoResultsException when the query doesn´t return results   
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult clientQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(clientDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve clients. Request : {} {}",keyMap,attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}
	
	/**
	 * 
	 * Adds a new register on the clients table. We assume that we are receiving the
	 * correct fields and they have been previously checked
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new register and a message with the operation result
	 * @exception InvalidEmailException when it introduces a email client that it is invalid
	 * @exception DuplicateKeyException when it introduces a email client that it exists
	 * @exception DataIntegrityViolationException when it doesn´t introduce a not null field 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult clientInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		dataValidator.checkIfMapIsEmpty(attrMap);
		attrMap.put("cl_entry_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		attrMap.put("cl_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.get("cl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("cl_email").toString());
			}
			if(attrMap.containsKey("cl_country_code") || attrMap.containsKey("cl_phone")){
				if(!attrMap.containsKey("cl_country_code") && attrMap.containsKey("cl_phone")) throw new AllFieldsRequiredException("CL_COUNTRY_CODE_REQUIRED");
				if(!attrMap.containsKey("cl_phone") && attrMap.containsKey("cl_country_code")) throw new AllFieldsRequiredException("CL_PHONE_REQUIRED");
				if(!control.checkIfPhoneNumberIsValid((int) attrMap.get("cl_country_code"), (String)attrMap.get("cl_phone"))) {
					throw new InvalidPhoneException("INVALID_PHONE");
				}
			}
			insertResult = this.daoHelper.insert(this.clientDao, attrMap);
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (InvalidEmailException | InvalidPhoneException | AllFieldsRequiredException e) {
			log.error("unable to insert a client. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a client. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a client. Request : {} ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		}catch (EmptyRequestException e) {
			log.error("unable to insert a client. Request : {} {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}catch (ClassCastException e) {
			log.error("unable to insert a client. Request : {} {} ",attrMap, e);
			control.setErrorMessage(insertResult,"INVALID_TYPE");
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the clients table. We assume that we are
	 * receiving the correct fields and they have been previously checked
	 * 
	 * @since 27/06/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 * @exception InvalidEmailException when it introduces a email client that it is invalid
	 * @exception DuplicateKeyException when it introduces a email client that it exists
	 * @exception RecordNotFoundException when it doesn´t introduce a not null field 
	 * @exception EmptyRequestException when it doesn´t introduce any field
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult clientUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			checkIfClientExists(keyMap);
			userControl.controlAccessClient((int) keyMap.get("id_client"));
			
			attrMap.put("cl_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
			
			if (attrMap.get("cl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("cl_email").toString());
			}
			
			if(attrMap.containsKey("cl_country_code") || attrMap.containsKey("cl_phone")) {
				if(!attrMap.containsKey("cl_country_code") && attrMap.containsKey("cl_phone")) throw new AllFieldsRequiredException("CL_COUNTRY_CODE_REQUIRED");
				if(!attrMap.containsKey("cl_phone") && attrMap.containsKey("cl_country_code")) throw new AllFieldsRequiredException("CL_PHONE_REQUIRED");
				if(!control.checkIfPhoneNumberIsValid((int) attrMap.get("cl_country_code"), (String)attrMap.get("cl_phone"))) {
					throw new InvalidPhoneException("INVALID_PHONE");
				}
			}

			updateResult = this.daoHelper.update(this.clientDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
			
		} catch (InvalidEmailException|NotAuthorizedException|AllFieldsRequiredException e) {
			log.error("unable to update a client. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to update a client. Request : {} ",attrMap, e);
			control.setErrorMessage(updateResult, "EMAIL_ALREADY_EXISTS");
		} catch (RecordNotFoundException e) {
			log.error("unable to update a client. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to update a client. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}catch (ClassCastException e) {
			log.error("unable to update a client. Request : {} {} ",attrMap, e);
			control.setErrorMessage(updateResult,"INVALID_TYPE");
		}
		return updateResult;
	}

	/**
	 * 
	 * Puts a leaving date on a client. If the client doesn't exists or has active
	 * reservations returns an error message
	 * 
	 * @since 05/07/2022
	 * @param The id of the client
	 * @return A message with the operation result
	 * @exception RecordNotFoundException when it doesn´t introduce a not null field 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult clientDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		dataValidator.checkIfMapIsEmpty(keyMap);
		Map<Object, Object> attrMap = new HashMap<>();
		attrMap.put("cl_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));

		EntityResult deleteResult = new EntityResultMapImpl();
		try {
			checkIfClientExists(keyMap);
			userControl.controlAccessClient((int) keyMap.get("id_client"));
			checkActiveReservations(keyMap);
			deleteResult = this.daoHelper.update(this.clientDao, attrMap, keyMap);
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		} catch (RecordNotFoundException|NotAuthorizedException e) {
			control.setErrorMessage(deleteResult, e.getMessage());
		}catch (EmptyRequestException e) {
			log.error("unable to insert a client. Request : {} {} ",keyMap, e);
			control.setErrorMessage(deleteResult, e.getMessage());
		}
		return deleteResult;
	}

	/**
	 * 
	 * Puts a leaving date on a client. If the client doesn't exists or has active
	 * reservations returns an error message
	 * 
	 * @since 05/07/2022
	 * @param The id of the client
	 * @return True if the client exists, false if it does't exists
	 */
	private boolean checkIfClientExists(Map<String, Object> keyMap) {
		if (keyMap.get("id_client") == null) {
			throw new RecordNotFoundException("ID_CLIENT_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_client");
		EntityResult existingClients = daoHelper.query(clientDao, keyMap, fields);
		if (existingClients.isEmpty())
			throw new RecordNotFoundException("ERROR_CLIENT_NOT_FOUND");
		return existingClients.isEmpty();
	}


	/**
	 * 
	 * Puts a leaving date on a client. If the client doesn't exists or has active
	 * reservations returns an error message
	 * 
	 * @since 05/07/2022
	 * @param The id of the client
	 * @return True if the client has active bookings, false if he hasn't
	 */
	private boolean checkActiveReservations(Map<String, Object> keyMap) {
		Map<String,Object> filter =new HashMap<>();
		filter.put("bk_client", keyMap.get("id_client"));
		List<String> fields = new ArrayList<>();
		fields.add("id_booking");
		EntityResult activeBookings = bookingService.clientactivebookingsQuery(filter, fields);
		if (!activeBookings.isEmpty())
			throw new RecordNotFoundException("ERROR_ACTIVE_BOOKINGS_FOUND");
		return activeBookings.isEmpty();
	}
}
