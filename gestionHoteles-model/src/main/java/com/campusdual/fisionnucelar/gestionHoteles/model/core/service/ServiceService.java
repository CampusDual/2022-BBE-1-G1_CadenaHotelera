package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IServiceService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
/**
 * This class listens the incoming requests related with the clients table
 *@since 30/06/2022
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
		EntityResult searchResult = this.daoHelper.query(this.serviceDao, keyMap, attrList);		
		if (searchResult.getCode() == EntityResult.OPERATION_WRONG) {
			searchResult.setMessage("ERROR_WHILE_QUERY");
		}
		if(searchResult.isEmpty()) {
			searchResult.setMessage("THERE ARE NOT SERVICES");
		}
		return searchResult;
	}

	@Override
	public EntityResult serviceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		if(!checkIfServiceNameExists(attrMap)) {
			insertResult.setMessage("THERE ARE SERVICES WITH THIS NAME");
			insertResult.setCode(1);
		}else {
			insertResult= this.daoHelper.insert(this.serviceDao, attrMap);
			if (insertResult.getCode() == EntityResult.OPERATION_WRONG) {
				insertResult.setMessage("ERROR_WHILE_INSERTING");
			}else {
				insertResult.setMessage("SUCCESSFUL_INSERTION");
			}
		}return insertResult;
	}

	@Override
	public EntityResult serviceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		if(checkIfServiceExists(keyMap)) {
			updateResult.setMessage("ERROR_SERVICE_NOT_FOUND");
			updateResult.setCode(1);
		}else {
			updateResult = this.daoHelper.update(this.serviceDao, attrMap, keyMap);
			if (updateResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
				updateResult.setMessage("ERROR_WHILE_UPDATING");
			} else {
				updateResult.setMessage("SUCCESSFUL_UPDATE");
			}
		}
		return updateResult;
	}

//	@Override
//	public EntityResult serviceDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
//		Map<Object, Object> attrMap = new HashMap<>();
//		attrMap.put("sv_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
//		
//		EntityResult deleteResult = new EntityResultMapImpl();
//		
//		if (!checkIfServiceExists(keyMap)) {
//			deleteResult.setMessage("ERROR_SERVICE_NOT_FOUND");
//			deleteResult.setCode(1);
//		}else {				
//			deleteResult=this.daoHelper.update(this.serviceDao, attrMap, keyMap);
//			deleteResult.setMessage("SUCCESSFUL_DELETE");
//		}
//		return deleteResult;
//	}
	
	private boolean checkIfServiceExists(Map<String, Object> keyMap) {
		List<String> fields = new ArrayList<>();
		fields.add("id_service");
		EntityResult existingServices = daoHelper.query(serviceDao, keyMap, fields);	
		return existingServices.isEmpty();
	}
	private boolean checkIfServiceNameExists(Map<String, Object> keyMap) {
		List<String> fields = new ArrayList<>();
		fields.add("sv_name");
		EntityResult existingNameServices = daoHelper.query(serviceDao, keyMap, fields);	
		return existingNameServices.isEmpty();
	}
}
