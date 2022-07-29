package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;

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

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServiceService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the service table
 * 
 * @since 07/07/2022
 * @version 1.0
 *
 */
@Service("ServiceService")
@Lazy
public class ServiceService implements IServiceService {
	@Autowired
	private ServiceDao serviceDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;
	private Validator validator;
	private Logger log;

	public ServiceService() {
		super();
		this.validator=new Validator();
		this.control = new Control();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a generic query over the services table
	 * 
	 * @since 07/07/2022
	 * @param The filters and the fields of the query
	 * @exception NoResultsException     when there are not matching results on the
	 *                                   services table
	 * @exception BadSqlGrammarException when it receives an incorrect type in the
	 *                                   params
	 * @return The columns from the services table especified in the params and a
	 *         message with the operation result
	 * 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult serviceQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.serviceDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a service. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the service table.
	 * 
	 * @since 08/07/2022
	 * @param The fields of the new register
	 * @exception DuplicateKeyException           when receives an existing Service
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields
	 * @exception EmptyRequestException           when the params are empty
	 * 
	 * @return The id of the new service and a message with the operation result
	 */

	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult serviceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			insertResult = this.daoHelper.insert(this.serviceDao, attrMap);
			if (insertResult.isEmpty())
				throw new EmptyRequestException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a service. Request : {}  ",attrMap, e);
			control.setErrorMessage(insertResult, "SERVICE_NAME_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a service. Request : {}  ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to insert a service. Request : {}  ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the service table.
	 * 
	 * @since 08/07/2022
	 * @param The fields to be updated
	 * @exception DuplicateKeyException   when trying to change the name of a
	 *                                    service for an existing one
	 * @exception EmptyRequestException   when the params are empty
	 * @exception RecordNotFoundException when receives a non existing service to
	 *                                    update
	 * 
	 * @return A message with the operation result
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult serviceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			checkIfServiceExists(keyMap);
			updateResult = this.daoHelper.update(this.serviceDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");

		} catch (DuplicateKeyException e) {
			log.error("unable to update a service. Request : {}  {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "SERVICE_NAME_ALREADY_EXISTS");
		} catch (RecordNotFoundException | EmptyRequestException e) {
			log.error("unable to update a service. Request : {}  {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	/**
	 * Search a concrete service. It throws an exception if it doesn't exists
	 * 
	 * @param keyMap The id of the services to search
	 * @exception RecordNotFoundException if doesn't find any result
	 * 
	 */
	private void checkIfServiceExists(Map<String, Object> keyMap) {
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
