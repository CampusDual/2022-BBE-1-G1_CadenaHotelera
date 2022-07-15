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

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraDao;
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

	public ExtraService() {
		super();
		this.control = new Control();
	}
	/**
	   * 
	   * Executes a generic query over the services table
	   * 
	   * @since 09/07/2022
	   * @param The filters and the fields of the query
	   * @return The columns from the extras table especified in the params and a
	   *         message with the operation result
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
			control.setErrorMessage(insertResult, "EXTRA_NAME_REQUIRED");
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
	 */
	@Override
	public EntityResult extraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
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
	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("ex_name") == null && attrMap.get("ex_description") == null) {
			throw new EmptyRequestException("ANY_FIELDS_REQUIRED");
		}
	}
}
