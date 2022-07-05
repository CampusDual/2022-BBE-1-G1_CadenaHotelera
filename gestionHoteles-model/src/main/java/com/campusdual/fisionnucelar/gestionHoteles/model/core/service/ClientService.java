package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IClientService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ClientDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
/**
 * This class listens the incoming requests related with the clients table
 *@since 30/06/2022
 *@version 1.0 
 *
 */
@Service("ClientService")
@Lazy
public class ClientService implements IClientService {

	@Autowired
	private ClientDao clientDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

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
		EntityResult searchResult = this.daoHelper.query(this.clientDao, keyMap, attrList);
		if (searchResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
			searchResult.setMessage("ERROR_WHILE_SEARCHING");
		}
		return searchResult;
	}

	
	/**
	   * 
	   * Adds a new register on the clients table. We assume that we are receiving
	   * the correct fields and they have been previously checked
	   * 
	   * @since 27/06/2022
	   * @param The fields of the new register
	   * @return The id of the new register and a message with the operation result
	   */	
	@Override
	public EntityResult clientInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = this.daoHelper.insert(this.clientDao, attrMap);
		if (insertResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
			insertResult.setMessage("ERROR_WHILE_INSERTING");
		} else {
			insertResult.setMessage("SUCCESSFUL_INSERTION");
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
		EntityResult updateResult = this.daoHelper.update(this.clientDao, attrMap, keyMap);
		if (updateResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
			updateResult.setMessage("ERROR_WHILE_UPDATING");
		} else {
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		}
		return updateResult;
	}

	
	/**
	   * 
	   * Deletes a existing register on the clients table
	   * 
	   * @since 27/06/2022
	   * @param The id of the client
	   * @return A message with the operation result
	   */
	@Override
	public EntityResult clientDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {

		List<String> fields = new ArrayList<>();
		fields.add("id_client");
		EntityResult checkIfExists = daoHelper.query(clientDao, keyMap, fields);
		
		EntityResult deleteResult = this.daoHelper.delete(this.clientDao, keyMap);
		if (checkIfExists.isEmpty()) {
			deleteResult.setMessage("ERROR_CLIENT_NOT_FOUND");
			deleteResult.setCode(1);
		} else {
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		}
		return deleteResult;
	}
}
