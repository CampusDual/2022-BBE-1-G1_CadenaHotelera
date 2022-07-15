package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServiceService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the service table
 *@since 07/07/2022
 *@version 1.0 
 *
 */
@Service("ServiceService")
@Lazy
public class ServiceService implements IServiceService{
	@Autowired
	private ServiceDao serviceDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	
	private Control control;

	public ServiceService() {
		super();
		this.control = new Control();
	}
	/**
	   * 
	   * Executes a generic query over the services table
	   * 
	   * @since 07/07/2022
	   * @param The filters and the fields of the query
	   * @return The columns from the services table especified in the params and a
	   *         message with the operation result
	   */
	@Override
	public EntityResult serviceQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {		
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.serviceDao, keyMap, attrList);
			control.checkResults(searchResult);			
		}catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
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
	 * @return The id of the new service and a message with the operation result
	 */
	@Override
	public EntityResult serviceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
			try {
				insertResult= this.daoHelper.insert(this.serviceDao, attrMap);
				if (insertResult.isEmpty())
					throw new AllFieldsRequiredException("FIELDS_REQUIRED");
				insertResult.setMessage("SUCESSFUL_INSERTION");
			}catch (DuplicateKeyException e) {
				control.setErrorMessage(insertResult, "SERVICE_NAME_ALREADY_EXISTS");
			}catch (DataIntegrityViolationException e) {
				control.setErrorMessage(insertResult, "SERVICE_NAME_REQUIRED");
			} catch (AllFieldsRequiredException e) {
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
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult serviceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
			checkIfServiceExists(keyMap);
			updateResult = this.daoHelper.update(this.serviceDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
				
		}catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "SERVICE_NAME_ALREADY_EXISTS");
		}catch (RecordNotFoundException|EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}
	
	private boolean checkIfServiceExists(Map<String, Object> keyMap) {
		if(keyMap.get("id_service")==null) {
			throw new RecordNotFoundException("ID_SERVICE_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_service");
		EntityResult existingServices = daoHelper.query(serviceDao, keyMap, fields);
		if(existingServices.isEmpty()) throw new RecordNotFoundException("ERROR_SERVICE_NOT_FOUND");
		return existingServices.isEmpty();
	}
	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("sv_name") == null && attrMap.get("sv_description") == null) {
			throw new EmptyRequestException("EMPTY_REQUEST");
		}
	}
}
