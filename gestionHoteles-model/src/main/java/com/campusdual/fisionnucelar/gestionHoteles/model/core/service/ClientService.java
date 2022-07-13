package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.reflect.CatchClauseSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IClientService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ClientDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidDateException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
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

	private Control control;

	public ClientService() {
		super();
		this.control = new Control();
	}

	/**
	 * 
	 * Executes a generic query over the clients table
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the clients table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult clientQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(clientDao, keyMap, attrList);
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
	 * Adds a new register on the clients table. We assume that we are receiving the
	 * correct fields and they have been previously checked
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new register and a message with the operation result
	 */
	@Override
	public EntityResult clientInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		attrMap.put("cl_entry_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		attrMap.put("cl_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.get("cl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("cl_email").toString());
			}
			insertResult = this.daoHelper.insert(this.clientDao, attrMap);
		} catch (InvalidEmailException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			control.setErrorMessage(insertResult, "DNI_NAME_AND_EMAIL_REQUIRED");
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
	 */
	@Override
	public EntityResult clientUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		attrMap.put("cl_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfClientExists(keyMap);
			checkIfDataIsEmpty(attrMap);

			if (attrMap.get("cl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("cl_email").toString());
			}

			updateResult = this.daoHelper.update(this.clientDao, attrMap, keyMap);
			if (updateResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
				updateResult.setMessage("ERROR_WHILE_UPDATING");
			} else {
				updateResult.setMessage("SUCCESSFUL_UPDATE");
			}
		} catch (InvalidEmailException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (RecordNotFoundException e) {
			control.setErrorMessage(updateResult, "CLIENT_DOESN'T_EXISTS");
		} catch (EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
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
	 */
	@Override
	public EntityResult clientDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		Map<Object, Object> attrMap = new HashMap<>();
		attrMap.put("cl_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));

		EntityResult deleteResult = new EntityResultMapImpl();
		try {
			checkIfClientExists(keyMap);
			checkActiveReservations(keyMap);
			deleteResult = this.daoHelper.update(this.clientDao, attrMap, keyMap);
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		} catch (RecordNotFoundException e) {
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

	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("cl_nif") == null && attrMap.get("cl_name") == null && attrMap.get("cl_phone") == null
				&& attrMap.get("cl_email") == null && attrMap.get("cl_entry_date") == null
				&& attrMap.get("cl_last_update") == null && attrMap.get("cl_leaving_date") == null) {
			throw new EmptyRequestException("EMPTY_REQUEST");
		}
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

		List<String> fields = new ArrayList<>();
		fields.add("id_booking");
		EntityResult activeBookings = bookingService.clientactivebookingsQuery(keyMap, fields);
		if (!activeBookings.isEmpty())
			throw new RecordNotFoundException("ERROR_ACTIVE_BOOKINGS_FOUND");
		return activeBookings.isEmpty();
	}
}
