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
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the clients table
 *@since 08/07/2022
 *@version 1.0 
 *
 */
@Service("ExtraService")
@Lazy
public class ExtraService implements IExtraService{
	@Autowired
	private ExtraDao extraDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	
	private Control control;
	private Validator dataValidator;
	private Logger log;

	public ExtraService() {
		super();
		this.control = new Control();
		this.dataValidator=new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
	}
	/**
     * 
     * Executes a generic query over the services table
     * 
     * @since 09/07/2022
     * @param The filters and the fields of the query
     * @return The columns from the extras table especified in the params and a
     *         message with the operation result
     *@exception BadSqlGrammarException when it introduces a string instead of a numeric on id
     *@exception NoResultsException when the query doesn´t return results       
     */
	@Override
	public EntityResult extraQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.extraDao, keyMap, attrList);
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
	   * Adds a new register on the extra table.
	   * 
	   * @since 09/07/2022
	   * @param The fields of the new register
	   * @return The id of the new extra and a message with the operation result
	   * @exception DuplicateKeyException when it introduces a extra that it exists
	   * @exception DataIntegrityViolationException when it doesn´t introduce a not null field 
	   * @exception AllFieldsRequiredException when it doesn´t introduce all not null field 
	   */
	@Override
	public EntityResult extraInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			insertResult= this.daoHelper.insert(this.extraDao, attrMap);
			if (insertResult.isEmpty())
				throw new AllFieldsRequiredException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		}catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "EXTRA_NAME_ALREADY_EXISTS");
		}catch (DataIntegrityViolationException e) {
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (AllFieldsRequiredException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		}
	return insertResult;
	}
	
	/**
	   * 
	   * Updates a existing register on the extra table.
	   * 
	   * @since 09/07/2022
	   * @param The fields to be updated
	   * @return A message with the operation result
	   * @exception DuplicateKeyException when it introduces a roomType that it exists in other register
	   * @exception RecordNotFoundException when it doesn´t introduce a not null field 
	   * @exception EmptyRequestException when it doesn´t introduce any field
	   */
	@Override
	public EntityResult extraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			checkIfExtraExists(keyMap);
			updateResult = this.daoHelper.update(this.extraDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
				
		}catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "EXTRA_NAME_ALREADY_EXISTS");
		}catch (RecordNotFoundException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}catch (EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}
	
	/**
	   * 
	   * In update, check the extra id exist in a extra table
	   * 
	   * @since 09/07/2022
	   * @param The field id_extra
	   * @return true if extra isn´t in a extra table or false if it is
	   * @exception RecordNotFoundException when it doesn´t introduce a not null field 
	   */
	private boolean checkIfExtraExists(Map<String, Object> keyMap) {
		if(keyMap.get("id_extra")==null) {
			throw new RecordNotFoundException("ID_EXTRA_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_extra");
		EntityResult existingExtras = daoHelper.query(extraDao, keyMap, fields);
		if(existingExtras.isEmpty()) throw new RecordNotFoundException("ERROR_EXTRA_NOT_FOUND");
		return existingExtras.isEmpty();
	}
	
}
